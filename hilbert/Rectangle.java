public class Rectangle {

  public static final int ON_EDGE_RIGHT = 1;
  public static final int ON_EDGE_LEFT = -1;
  public static final int INTERSECT_EDGE = 0;

  private Point [] ps;
  private Edge [] es;
  private Edge ad;

  /**
   *  create an rectangle that is parallel to x-y coordinate
   */
  public Rectangle(double x, double y, double xlen, double ylen) {
    this(new Point(x, y), new Point(x + xlen, y),
        new Point(x + xlen, y + ylen), new Point(x, y + ylen));
  }

  public Rectangle(Point a, Point b, Point c, Point d) {
    reset(a, b, c, d);
  }

  /**
   * rotate point b around a for angle delta
   */
  private Point rotatePoint(Point a, Point b, double delta) {
    double xx = b.x - a.x;
    double yy = b.y - a.y;

    double newX = xx * Math.cos(delta) - yy * Math.sin(delta);
    double newY = xx * Math.sin(delta) + yy * Math.cos(delta);
    return new Point(newX, newY);
  }

  public int isCovered(double x, double y) {
    if ((x - ps[0].x) * es[0].xx + (y - ps[0].y) * es[0].yy < 0.0)
      return 0;
    if ((x - ps[1].x) * es[0].xx + (y - ps[1].y) * es[0].yy > 0.0)
      return 0;
    if ((x - ps[2].x) * ad.xx + (y - ps[0].y) * ad.yy < 0.0)
      return 0;
    if ((x - ps[3].x) * ad.xx + (y - ps[1].y) * ad.yy > 0.0)
      return 0;

    return 1;
  }

  /**
   * rotate the current rectangle counter clockwise 
   * with angle around point a
   */
  public Rectangle rotate(double angle) {
    for (int i = 1; i < ps.length; ++i) {
      rotatePoint(ps[0], ps[i], angle);
    }
    reset(ps);
    return this;
  }

  private void reset(Point a, Point b, Point c, Point d) {
    if (null == this.ps || null == this.es) {
      this.ps = new Point [4];
      this.es = new Edge [4];
    }
    ps[0] = a;
    ps[1] = b;
    ps[2] = c;
    ps[3] = d;
    reset(ps);
  }

  private void reset(Point ps[]) {
    this.ps = ps;
    es[0] = new Edge(ps[0], ps[1]);
    es[1] = new Edge(ps[1], ps[2]);
    es[2] = new Edge(ps[2], ps[3]);
    es[3] = new Edge(ps[3], ps[0]);
    ad = new Edge(ps[0], ps[3]);
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

    //System.out.println("checkSide on " + e.toString() + " = "  + rightCnt);
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

  public double getXDiff() {
    return Math.max(Math.abs(ps[0].x - ps[2].x), 
                    Math.abs(ps[1].x - ps[3].x));
  }

  public double getYDiff() {
    return Math.max(Math.abs(ps[0].y - ps[2].y),
                    Math.abs(ps[1].y - ps[3].y));
  }

  public String toString() {
    String res = "";
    for (Point p : ps) {
      res += (p.toString() + ", ");
    }
    return res;
  }
}

