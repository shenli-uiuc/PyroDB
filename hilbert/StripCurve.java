public class StripCurve extends SpaceFillingCurve {

  public static long staticEncode(long x, long y, long r) {
    long mask = (1L << r) - 1;
    x = x & mask;
    y = y & mask;
    return (y << r) | x;
  }

  @Override
  public long encode(long x, long y, long r) {
    return staticEncode(x, y, r);
  }

  public static Pair<Long, Long> staticDecode(long h, long r) {
    long mask = (1L << r) - 1;
    long x = h & mask;
    long y = (h >>> r) & mask;
    return new Pair<Long, Long>(x, y);
  }
}
