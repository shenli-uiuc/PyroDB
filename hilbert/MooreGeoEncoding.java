import java.util.Arrays;

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
  public Range getTileRange(long x, long y, long r) {
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
    return new Range(tmp[0], tmp[3]); 
  }
}
