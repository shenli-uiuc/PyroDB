public class Zcurve extends SpaceFillingCurve {
  
  @Override
  public long encode(long x, long y, long r) {
    return interleaveBits(x, y, r);
  }
}
