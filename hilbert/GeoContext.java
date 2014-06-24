/**
 * helper data type to encapsulate geo encoding conf info
 */
public class GeoContext {
  private long maxResolution;
  private double maxX;
  private double maxY;

  public GeoContext(long maxResolution, double maxX, double maxY) {
    this.maxResolution = maxResolution;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public long getMaxResolution() {
    return maxResolution;
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }
}
