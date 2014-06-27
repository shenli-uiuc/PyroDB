
/**
 *
 * Rectangules are represented using three pointers: A, B, D, such
 * that AB and AD are neighboring edges. ABCD are named collowing 
 * counter-clockwise order. Hence, the rectangle itself locates
 * on the left side of all its edges.
 */
public class RectangleGeoRequest extends GeoRequest {

  private Rectangle rect;

  public RectangleGeoRequest(Rectangle rect) {
    this.rect = rect.clone();
  }

  @Override
  public String getName() {
    return "rect";
  }

  @Override
  public int isCovered(double x, double y) {
    return rect.isCovered(x, y);
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

  //for testing
  public static void main(String args []) {
    Point a = new Point(8, 0);
    Point b = new Point(11, 4);
    Point c = new Point(3, 10);
    Point d = new Point(0, 6);
    Rectangle rect = new Rectangle(a, b, c, d);
    RectangleGeoRequest rgr = new RectangleGeoRequest(rect);
    System.out.println(rgr.isCovered(0, 0, 2, 2) + ", Expecting " + GeoRequest.NO_COVER);
    System.out.println(rgr.isCovered(-1, -1, 200, 200) + ", Expecting " + GeoRequest.PARTIAL_COVER);
    System.out.println(rgr.isCovered(4.1, 3.1, 2, 3) + ", Expecting " + GeoRequest.FULL_COVER);
  }

}
