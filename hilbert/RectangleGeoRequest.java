
/**
 *
 * Rectangules are represented using three pointers: A, B, D, such
 * that AB and AD are neighboring edges. ABCD are named collowing 
 * counter-clockwise order. Hence, the rectangle itself locates
 * on the left side of all its edges.
 */
public class RectangleGeoRequest extends GeoRequest {

  private Point a;
  private Point b;
  private Point c;
  private Point d;
  
  private Edge ab;
  private Edge bc;
  private Edge cd;
  private Edge da;
  private Edge ad;

  public RectangleGeoRequest(Point a, Point b, Point d) {
    
    this.a = a;
    this.b = b;
    this.d = d;

    c = new Point(b.x + ad.xx, b.y + ad.yy);
    
    ab = new Edge(a, b);
    bc = new Edge(b, c);
    cd = new Edge(c, d);
    da = new Edge(d, a);
    ad = new Edge(a, d);
  }

  @Override
  public int isCovered(double x, double y) {
    if ((x - a.x) * ab.xx + (y - a.y) * ab.yy < 0.0) return 0;
    if ((x - b.x) * ab.xx + (y - b.y) * ab.yy > 0.0) return 0;
    if ((x - a.x) * ad.xx + (y - a.y) * ad.yy < 0.0) return 0;
    if ((x - d.x) * ad.xx + (y - d.y) * ad.yy > 0.0) return 0;

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


  private int isRecOnRight(Edge e, 
      double x, double y, double xlen, double ylen) {
    int sideCnt = 0;
    sideCnt += e.isOnRight(x, y);
    sideCnt += e.isOnRight(x + xlen, y);
    sideCnt += e.isOnRight(x, y + ylen);
    sideCnt += e.isOnRight(x + xlen, y + ylen);
    if (sideCnt >= 4)
      return 1;
    else
      return 0;
  }

  /**
   *  Test if the given rectangle is covered by this GeoRequest
   *  @return   0   not intersected at all
   *            1   overlaps not fully covered
   *            2   fully covered
   */
  @Override
  public int isCovered(double x, double y, double xlen, double ylen) {
    // examine the 4 edges of the request rectangle to see if we can
    // find a separating edge such that all 4 points of the tile locates
    // on the RIGHT side of the edge. Then, we can guantee that there is
    // noe intersection at all. But the failure in finding such edge does
    // not mean the rectangle and tile intersects. That's why we need 
    // another check. 
      
    Rectangle tile = new Rectangle(x, y, xlen, ylen);
    int sideCnt = 0;
    sideCnt += isRecOnRight(ab, x, y, xlen, ylen);
    // examine if all 4 edges of the tile
  }

  class Rectangle {
    private Point [] ps;
    private Edge [] es;
    private Point a;
    private Point b;
    private Point c;
    private Point d;

    private Edge ab;
    private Edge bc;
    private Edge cd;
    private Edge da;

    public class Rectangle(double x, double y, double xlen, double ylen) {
      this(new Point(x, y), new Point(x + xlen, y),
            new Point(x + xlen, y + ylen), new Point(x, y + ylen));
    }

    public class Rectangle(Point a, Point b, Point c, Point d) {
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
    
    public int isOnEdgeRight(Edge e) {
      for (int i = 0 ; i < ps.length; ++i)
        if (!e.isOnRight(ps[i])) return 0;
      return 1;
    }

    public int isIntersect(Rectangle r) {
      int i = 0;
      for (i = 0; i < es.length; ++i) {
        if (r.isOnEdgeRight(this.es[i]) > 0)
          return 1;
        if (this.isOnEdgeRight(r.es[i]) > 0)
          return 1;
      }
      return 0;
    }
  }

}
