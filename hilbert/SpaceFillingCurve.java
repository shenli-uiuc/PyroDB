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

  protected static Pair<Long, Long> deInterleaveBits(long h, long r) {
    long even = 0;
    long odd = 0;
    long mask = 1;
    long i = 0;
    while ( ++i <= r) {
      even |= (h & mask);
      h >>>= 1;
      odd |= (h & mask);
      mask <<= 1;
    }

    mask = (1L << r) - 1;

    System.out.println(odd + ", " + even + ", " + r + ", " + mask);

    return new Pair<Long, Long>(odd, even);
  }

  /**
   * For testing
   */
  public static void main(String args[]) {
    long h = 10000000000L;
    long r = 32;
    Pair<Long, Long> res = deInterleaveBits(h, r);
    System.out.println("h: " + Long.toBinaryString(h));
    long odd = res.first;
    long even = res.second;
    System.out.println("o: " + Long.toBinaryString(odd));
    System.out.println("e: " + Long.toBinaryString(even));
    System.out.println("i: " + Long.toBinaryString(interleaveBits(odd, even, r)));
  }

}
