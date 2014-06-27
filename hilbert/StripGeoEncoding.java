import java.util.Arrays;
import java.util.LinkedList;

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
  public LinkedList<Range> getTileRange(long x, long y, long r) {
    if (this.maxResolution < r) {
      throw new IllegalStateException("Max allowed resolution is " 
          + this.maxResolution + ", got resolution " + r);
    }

    long nZeros = this.maxResolution - r;
    long rows = 1L << nZeros;
    long mask = rows - 1;
    LinkedList<Range> res = new LinkedList<Range> ();
    for (int i = 0 ; i < rows; ++i) {
      res.add(new Range(encode(x, y | i, this.maxResolution),
                        encode(x | mask, y | i, this.maxResolution)));
    }
    return res;
  }

  @Override
  public Pair<Long, Long> getNextTile(long x, long y, long r) {
    long nZeros = this.maxResolution - r;
    long unit = 1L << nZeros;
    long h = encode(x, y, r) + unit;
   
     return  StripCurve.staticDecode(h, r);
  }
}
