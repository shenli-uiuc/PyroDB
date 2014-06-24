import java.util.Arrays;

public class StripGeoEncoding extends GeoEncoding {

  private long maxResolution;

  public StripGeoEncoding(GeoContext gc) {
    super(gc);
    this.maxResolution = gc.getMaxResolution();
  }

  public long encode(long x, long y, long r) {
    return StripCurve.staticEncode(x, y, r);
  }

  @Override
  public Range getTileRange(long x, long y, long r) {
    if (this.maxResolution < r) {
      throw new IllegalStateException("Max allowed resolution is " 
          + this.maxResolution + ", got resolution " + r);
    }

    long nZeros = this.maxResolution - r;
    long mask = (1 << nZeros) - 1;
    return new Range(encode(x, y, this.maxResolution), 
                     encode(x | mask, y | mask, this.maxResolution)); 
  }
}
