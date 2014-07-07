public class SubQuadTreeCache {
  public static final int CHILD_NUM = 3;
  private int cachedLevel;
  private long [] cache;
  public SubQuadTreeCache(int cachedLevel, 
      long p1, long p2, long x, long y, int r) {
    this.cachedLevel = cachedLevel;
    int entries = (1 << (cachedLevel << 1) - 1) / CHILD_NUM;
    cache = new long[entries];
  }

  private void initCache(long p1, long p2, long x, long y, int r) {
    int rr = r;
    int maxRR = r + cachedLevel;
    // enq (x, y, r)?
    // while queue is not empty, get (x, y, r), enq its children
    while (rr < maxRR) {

      ++rr;
    }
  }

  private void initRoot() {

  }
}
