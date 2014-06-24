public abstract class GeoEncoding {
  protected GeoContext gc;

  public GeoEncoding(GeoContext gc) {
    this.gc = gc;
  }

  public GeoContext getGeoContext() {
    return this.gc;
  }

  public abstract Range getTileRange(long x, long y, long r);
}
