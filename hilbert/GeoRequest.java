public abstract class GeoRequest {

  public static final int NO_COVER = 0;
  public static final int PARTIAL_COVER = 1;
  public static final int FULL_COVER = 2;

  public abstract String getName();

  /**
   *  Test if the given point is covered by this GeoRequest
   */ 
  public abstract int isCovered(double x, double y);

  /**
   *  Test if the given rectangle is covered by this GeoRequest.
   *  @return   0   not intersected at all
   *            1   intersected but not fully covered
   *            2   fully covered
   */
  public abstract int isCovered(double x, double y, 
                                double xlen, double ylen);
}
