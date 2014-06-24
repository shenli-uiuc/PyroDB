public class StripCurve extends SpaceFillingCurve {

  public static long staticEncode(long x, long y, long r) {
    long mask = (1 << r) - 1;
    x = x & mask;
    y = y & mask;
    return (y << r) | x;
  }

  @Override
  public long encode(long x, long y, long r) {
    return staticEncode(x, y, r);
  }
}
