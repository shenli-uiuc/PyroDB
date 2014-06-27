public class ZCurve extends SpaceFillingCurve {

  public static long staticEncode(long x, long y, long r) {
    return interleaveBits(x, y, r);
  }

  @Override
  public long encode(long x, long y, long r) {
    return interleaveBits(x, y, r);
  }

  public static Pair<Long, Long> staticDecode(long h, long r) {
    return deInterleaveBits(h, r);
  }

}
