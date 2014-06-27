import java.util.Arrays;
import java.util.LinkedList;

public class MooreGeoEncoding extends GeoEncoding {

  private long maxResolution;

  public MooreGeoEncoding(GeoContext gc) {
    super(gc);
    this.maxResolution = gc.getMaxResolution();
  }

  public long encode(long x, long y, long r) {
    return MooreCurve.staticEncode(x, y, r);
  }

  @Override
  public LinkedList<Range> getTileRange(long x, long y, long r) {
    if (this.maxResolution < r) {
      throw new IllegalStateException("Max allowed resolution is " 
          + this.maxResolution + ", got resolution " + r);
    }

    long nZeros = this.maxResolution - r;
    long mask = (1 << nZeros) - 1;
    long [] tmp = 
      new long [] { encode(x, y, this.maxResolution),
                encode(x | mask, y, this.maxResolution),
                encode(x, y | mask, this.maxResolution),
                encode(x | mask, y | mask, this.maxResolution)};
    Arrays.sort(tmp);

    LinkedList<Range> res = new LinkedList<Range> ();
    res.add(new Range(tmp[0], tmp[3]));
    return res;
  }

  /**
   *  for testing
   */
  private long encodeWithCheck(long x, long y, long r) {
    if (x < 0 || y < 0)
      return -1;
    return encode(x, y, r);
  }

  /**
   * for testing
   *
   * @retrurn   null    if it is the last tile
   */
  @Override
  public Point getNextTile(long x, long y, long r) {
    long nZeros = this.maxResolution - r;
    long unit = 1 << nZeros;
    long curMooreIndex = encode(x, y, r);
    long nextMooreIndex = curMooreIndex + (1 << (nZeros << 1));
    if (nextMooreIndex == encodeWithCheck(x + unit, y, r))
      return new Point(x + unit, y);
    if (nextMooreIndex == encodeWithCheck(x, y + unit, r))
      return new Point(x, y + unit);
    if (nextMooreIndex == encodeWithCheck(x - unit, y, r))
      return new Point(x - unit, y);
    if (nextMooreIndex == encodeWithCheck(x, y - unit, r))
      return new Point(x, y - unit);
    return null;
  }
}
