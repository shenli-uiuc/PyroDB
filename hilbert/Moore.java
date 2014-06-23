
public class Moore {
  private static final long [] R1_MAP = {0, 3, 
                                        1, 2};
  private static final long [] R2_MAP = { 1,  0, 15, 14,
                                         2,  3, 12, 13,
                                         5,  4, 11, 10,
                                         6,  7,  8,  9};

  private static long catLowerBits(long x, long y, long r) {
    long mask = (1 << r) - 1;

    return (x & mask) | ((y & mask) << r);
  }

  public static long encode(long x, long y, long r) {
    if (r <= 1) {
      return R1_MAP[(int)catLowerBits(x, y, 1)];
    }
    
    long first4Bits = 
      R2_MAP[(int)catLowerBits(x >>> (r - 2), y >>> (r - 2), 2)];

    if (r <= 2) {
      return first4Bits;
    }
    /* 0: -----
     *    |   |
     *    |   |
     *
     * 1: -----
     *    |
     *    -----
     *
     * 2: |   |
     *    |   |
     *    -----
     *
     * 3: -----
     *        |
     *    -----
     */
    //calculate d0 and d1 for r = 2;
    long firstBitMask = 1 << (r - 1);
    // set d0 to 1, using firstBitMask to avoid 
    // another 2 arithmetic operations
    long d0 = firstBitMask >>> 1;
    long d1 = ((~x) & firstBitMask) >>> 1;
    
    // d0 and d1 represent the direction indicated by the higher 2 bits
    // from x and y
    return (first4Bits << ((r - 2) << 1)) | 
      encodeLowerBits(x, y, r - 2, d0, d1);
  }

  /*
   * d0 and d1 are already set for the r + 1 bit from right;
   */
  public static long encodeLowerBits(long x, long y, long r, 
                                    long pd0, long pd1) {
    long mask = (1L << (r + 1)) - 1;
    long hodd = 0;
    long notx = ~x;
    long noty = ~y;
    long heven = notx ^ y;
    long xorxy = x ^ y;

    long d0 = pd0;
    long d1 = pd1;
    for (int k = 0; k < r; ++k) {
      // heven equals to notx ^ y
      d1 = (((d1 & heven) | (xorxy & (d0 ^ x))) >>> 1) & mask | pd1;
      d0 = (((~d0 & (d1 ^ noty)) | (d0 & (d1 ^ x))) >>> 1) & mask | pd0;
    }

    hodd = ((~d0 & (d1 ^ notx)) | (d0 & (d1 ^ noty))) & mask;


    return interleaveBits(hodd, heven, r) & (mask | (mask << r));
  }

  private static long interleaveBits(long odd, long even, long r) {
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

  public static void main(String args[]) {
    for (int r = 1; r < 5; ++r) {
      for (long i = 0 ; i < (1 << r); ++i) {
        for (long j = 0 ; j < (1 << r); ++j) {
          System.out.print("\t" + encode(j, i, r) + ",");
          //System.out.print("\t" + encodeLowerBits(j, i, r, 0, 0) +  ", ");
          //System.out.println();
        }
        System.out.println();
      }
    }
  }
}

