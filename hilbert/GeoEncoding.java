import java.util.LinkedList;

/**
 * TODO: It is better to have both encode and decode functions
 * for all GeoEncoding subclasses.
 */
public abstract class GeoEncoding {
  protected GeoContext gc;

  public GeoEncoding(GeoContext gc) {
    this.gc = gc;
  }

  public GeoContext getGeoContext() {
    return this.gc;
  }

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
