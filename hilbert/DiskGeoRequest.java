public class DiskGeoRequest extends GeoRequest {

  private double cx = 0;
  private double cy = 0;
  private double radius = 0;

  /**
   *  @param  x       x coordinate of its center
   *  @param  y       y coordinate of its center
   *  @param  radius  radius of the request disk
   */
  public DiskGeoRequest(double x, double y, double radius) {
    this.cx = x;
    this.cy = y;
    this.radius = radius;
  }

  @Override
  public String getName() {
    return "disk";
  }

  @Override
  public int isCovered(double x, double y) {
    double xDiff = x - this.cx;
    double yDiff = y - this.cy;
    if (xDiff * xDiff + yDiff * yDiff <= this.radius * this.radius )
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
    int coveredCnt = 0;
    coveredCnt += isCovered(x, y);
    coveredCnt += isCovered(x + xlen, y);
    coveredCnt += isCovered(x, y + ylen);
    coveredCnt += isCovered(x + xlen, y + ylen);
    if (coveredCnt >= 4)
      return FULL_COVER;
    else if (coveredCnt > 0)
      return PARTIAL_COVER;
    else {
      // check if the disk is contained in the rectangle
      if (cx > x && cx < x + xlen 
          && cy > y && cy < y + ylen) {
        return PARTIAL_COVER;
      }

      return NO_COVER;
    }
  }

}
