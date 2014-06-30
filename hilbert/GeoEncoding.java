import java.util.LinkedList;

/**
 * TODO: It is better to have both encode and decode functions
 * for all GeoEncoding subclasses.
 */
public abstract class GeoEncoding {
  protected GeoContext gc;
  // the length of the smallest tile
  protected double unitX;
  protected double unitY;

  public GeoEncoding(GeoContext gc) {
    this.gc = gc;
    this.unitX = gc.getMaxX() / (1L << gc.getMaxResolution());
    this.unitY = gc.getMaxY() / (1L << gc.getMaxResolution());
  }

  public GeoContext getGeoContext() {
    return this.gc;
  }

  /**
   *  TODO: for now, this method simply cast double into long.
   *  later we may need to add a nonce to the end.
   */
  public long encode(double x, double y) {
    // translate location into tile
    return encode((long)(x / unitX), (long)(y / unitY));
  }

  public abstract long encode(long x, long y);

  /**
   *  @param  r   resolution of encoding, only the last r bits of
   *              x and y are considered
   */
  public abstract LinkedList<Range> getTileRange(long x, long y, long r);

  /**
   * For testing purpose.
   * get the next x and y index given the current x, y and resolution.
   */
  public abstract Pair<Long, Long> getNextTile(long x, long y, long r);
}
