public abstract class SpaceFillingCurve {
  public abstract  long encode(long x, long y, long r);

  protected static long interleaveBits(long odd, long even, long r) {
    long h = 0;
    long mask = 1;
    odd <<= 1;
    while (r > 0) {
      --r;
      h |= (even & mask);
      mask <<= 1;
      even <<= 1;
      h |= (odd & mask);
      mask <<= 1;
      odd <<= 1;
    }
    return h;
  }

}
