
/**
 *
 * Rectangules are represented using three pointers: A, B, D, such
 * that AB and AD are neighboring edges. ABCD are named collowing 
 * counter-clockwise order. Hence, the rectangle itself locates
 * on the left side of all its edges.
 */
public class RectangleGeoRequest extends GeoRequest {

  private Rectangle rect;
  private Edge ad;

  public RectangleGeoRequest(Rectangle rect) {
    this.rect = rect.clone();
    ad = new Edge(rect.es[3].b, rect.es[3].a);
  }

  @Override
  public String getName() {
    return "rect";
  }

  @Override
  public int isCovered(double x, double y) {
    if ((x - rect.ps[0].x) * rect.es[0].xx 
        + (y - rect.ps[0].y) * rect.es[0].yy < 0.0) 
      return 0;
    if ((x - rect.ps[1].x) * rect.es[0].xx 
        + (y - rect.ps[1].y) * rect.es[0].yy > 0.0) 
      return 0;
    if ((x - rect.ps[2].x) * ad.xx + (y - rect.ps[0].y) * ad.yy < 0.0) 
      return 0;
    if ((x - rect.ps[3].x) * ad.xx + (y - rect.ps[1].y) * ad.yy > 0.0) 
      return 0;

    return 1;
  }

  public boolean isFullyCovered(double x, double y, 
                                double xlen, double ylen) {
    int coverCnt = 0;
    coverCnt += isCovered(x, y);
    coverCnt += isCovered(x + xlen, y);
    coverCnt += isCovered(x, y + ylen);
    coverCnt += isCovered(x + xlen, y + ylen);
    
    return coverCnt >= 4;
  }


  /**
   *  Test if the given rectangle is covered by this GeoRequest
   *  @return   0   not intersected at all
   *            1   overlaps not fully covered
   *            2   fully covered
   */
  @Override
  public int isCovered(double x, double y, double xlen, double ylen) {
    return rect.isIntersect(new Rectangle(x, y, xlen, ylen));
  }

  public class Rectangle {

    public static final int ON_EDGE_RIGHT = 1;
    public static final int ON_EDGE_LEFT = -1;
    public static final int INTERSECT_EDGE = 0;

    private Point [] ps;
    private Edge [] es;

    public Rectangle(double x, double y, double xlen, double ylen) {
      this(new Point(x, y), new Point(x + xlen, y),
            new Point(x + xlen, y + ylen), new Point(x, y + ylen));
    }

    public Rectangle(Point a, Point b, Point c, Point d) {
      reset(a, b, c, d);
    }

    public void reset(Point a, Point b, Point c, Point d) {
      if (null == this.ps || null == this.es) {
        this.ps = new Point [4];
        this.es = new Edge [4];
      }
      ps[0] = a;
      ps[1] = b;
      ps[2] = c;
      ps[3] = d;
      es[0] = new Edge(a, b);
      es[1] = new Edge(b, c);
      es[2] = new Edge(c, d);
      es[3] = new Edge(d, a);
    }
    
    /**
     *  @return   1     all on right side
     *            0     intersect
     *            -1    all on left side
     */
    public int checkSide(Edge e) {
      int rightCnt = 0;
      for (int i = 0 ; i < ps.length; ++i)
        if (e.isOnRight(ps[i])) ++rightCnt;
      if (rightCnt >= 4)
        return ON_EDGE_RIGHT;
      else if (rightCnt <= 0)
        return ON_EDGE_LEFT;
      else
        return INTERSECT_EDGE;
    }

    /**
     * @return    0   not intersected
     *            1   intersected but not fully covered
     *            2   the given rectangle is fully covered by this rectangle
     */
    public int isIntersect(Rectangle r) {
      int i = 0;
      int leftCnt = 0;
      int res = 0;
      for (i = 0; i < es.length; ++i) {
        res = r.checkSide(this.es[i]);
        if (res == ON_EDGE_RIGHT)
          return GeoRequest.NO_COVER;
        else if (res == ON_EDGE_LEFT)
          ++leftCnt;
      }
      // the given rectangle is on the left side of all 4 edges
      if (leftCnt >= 4) {
        return GeoRequest.FULL_COVER;
      }

      for (i = 0 ; i < r.es.length; ++i)
        if (this.checkSide(r.es[i]) == ON_EDGE_RIGHT)
          return GeoRequest.NO_COVER;
      return GeoRequest.PARTIAL_COVER;
    }

    public Rectangle clone() {
      Point [] clonePs = new Point[4];
      int i = 0;
      for (i = 0 ; i < ps.length; ++i) {
        clonePs[i] = new Point(ps[i].x, ps[i].y);
      }
      return new Rectangle(clonePs[0], clonePs[1], clonePs[2], clonePs[3]);
    }
  }

}
