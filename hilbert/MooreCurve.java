
public class MooreCurve extends SpaceFillingCurve {
  private static final long [] R1_MAP = {0, 3, 
                                        1, 2};
  private static final long [] R2_MAP = { 1,  0, 15, 14,
                                         2,  3, 12, 13,
                                         5,  4, 11, 10,
                                         6,  7,  8,  9};

  public static final long [] codeMap = {3, 2, 0, 1,
                                        3, 0, 2, 1,
                                        1, 0, 2, 3,
                                        1, 2, 0, 3};

  public static final int [] orientationMap = {1, 0, 3, 0,
                                               0, 2, 1, 1,
                                               2, 1, 2, 3,
                                               3, 3, 0, 2};

  public static final int [] childXMap = {1, 1, 0, 0,
                                          0, 1, 1, 0,
                                          0, 0, 1, 1,
                                          1, 0, 0, 1};

  public static final int [] childYMap = {0, 1, 1, 0,
                                          1, 1, 0, 0,
                                          1, 0, 0, 1,
                                          0, 0, 1, 1};

  private static long catLowerBits(long x, long y, long r) {
    long mask = (1L << r) - 1;

    return (x & mask) | ((y & mask) << r);
  }

  @Override
  public long encode(long x, long y, long r) {
    return staticEncode(x, y, r);
  }

  public static int getCurOrientation(long i0, long i1, long i2, long i3) {
    if (i0 - i1 == i3 - i2) {
      if (i2 > i0) {
        /**
         * 2 3
         * 1 0
         */
        return 3;
      } else {
        /**
         * 0 1
         * 3 2
         */
        return 1;
      }
    } else {
      if (i0 > i1) {
        /**
         * 2 1
         * 3 0
         */
        return 0;
      } else {
        /**
         * 0 3
         * 1 2
         */
        return 2;
      }
    }
  }

  public static int getLowerOrientation(long x, long y, long r,
      long pd0, long pd1) {
    long mask = (1L << (r)) - 1;
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
    return (int)((d1 << 1) | (d0 & 1) & 3);
  }

  public static int getOrientation(long x, long y, long r) {
    if (r <= 0) {
      return 0;
    }

    long p0 = 1;
    long p1 = 1;
    if ((x & (1 << (r-1))) > 0) {
      p1 = 0;
    }

    if (r <= 1) {
      return (int)((p1 << 1) + p0);
    }
    return getLowerOrientation(x, y, r-1, p0, p1);
  }

  public static long staticEncode(long x, long y, long r) {
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
    long firstBitMask = 1L << (r - 1);
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
    long mask = (1L << (r)) - 1;
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

  
  public static void main(String args[]) {
    MooreCurve moore = new MooreCurve();
    for (int r = 2; r < 5; ++r) {
      for (long i = 0 ; i < (1L << r); ++i) {
        for (long j = 0 ; j < (1L << r); ++j) {
          System.out.print("\t" + moore.encode(j, i, r) + ",");
          //System.out.print("\t" + encodeLowerBits(j, i, r, 0, 0) +  ", ");
          //System.out.println();
        }
        System.out.println();
      }
    }
  }
}

