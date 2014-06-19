
public class Hilbert {

  /**
   * Find the Hilbert order (=vertex index) for the given grid cell 
   * coordinates.
   * @param x cell column (from 0)
   * @param y cell row (from 0)
   * @param r resolution of Hilbert curve (grid will have Math.pow(2,r) 
   * rows and cols)
   * @return Hilbert order 
   */
  public static int encode(int x, int y, int r) {

    int mask = (1 << r) - 1;
    int hodd = 0;
    int heven = x ^ y;
    int notx = ~x & mask;
    int noty = ~y & mask;
    int temp = notx ^ y;

    int v0 = 0, v1 = 0;
    for (int k = 1; k < r; k++) {
      v1 = ((v1 & heven) | ((v0 ^ noty) & temp)) >> 1;
      v0 = ((v0 & (v1 ^ notx)) | (~v0 & (v1 ^ noty))) >> 1;
    }
    hodd = (~v0 & (v1 ^ x)) | (v0 & (v1 ^ noty));

    return interleaveBits(hodd, heven);
  }

  /**
   * Interleave the bits from two input integer values
   * @param odd integer holding bit values for odd bit positions
   * @param even integer holding bit values for even bit positions
   * @return the integer that results from interleaving the input bits
   *
   * @todo: I'm sure there's a more elegant way of doing this !
   */
  private static int interleaveBits(int odd, int even) {
    int val = 0;
    // Replaced this line with the improved code provided by Tuska
    // int n = Math.max(Integer.highestOneBit(odd), Integer.highestOneBit(even));
    int max = Math.max(odd, even);
    int n = 0;
    while (max > 0) {
      n++;
      max >>= 1;
    }

    for (int i = 0; i < n; i++) {
      int bitMask = 1 << i;
      int a = (even & bitMask) > 0 ? (1 << (2*i)) : 0;
      int b = (odd & bitMask) > 0 ? (1 << (2*i+1)) : 0;
      val += a + b;
    }

    return val;
  }
}

