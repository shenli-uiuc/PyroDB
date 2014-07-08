import java.util.Queue;
import java.util.LinkedList;

public class SubQuadTreeCache {
  private long [] cache;
  private GeoEncoding ge;
  private int left;
  private int right;

  class Tuple {
    // helper to wrap tree node info
    long x;
    long y;
    int id;
    int right;

    public Tuple(long x, long y, int id, int right) {
      this.x = x;
      this.y = y;
      this.id = id;
      this.right = right;
    }
  }

  public SubQuadTreeCache(long p0, long p1, long x, long y, 
      int left, int right, boolean isRoot) {
    this.left = left;
    this.right = right;
    int entries = ((1 << ((left - right + 1) << 1)) - 1) / 3;
    cache = new long[entries];
    long mask = ~((1L << (left + 1)) - 1);
    initCache(p0, p1, x & mask, y & mask, isRoot);
  }

  private void initCache(long p0, long p1, 
      long x, long y, boolean isRoot) {
    Queue<Tuple> q = new LinkedList<Tuple>();
    // TODO: should it be left + 1
    q.add(new Tuple(x, y, 0, left));
    Tuple cur = null;
    int childID = 0;
    long unit = 0;
    int nextRight = 0;
    // in quad tree, for node with id i, its first child's id is
    // (i << 2) + 1;
    //
    // 4 children:
    // (x, y), (x | unit, y), (x, y | unit), (x | unit, y | unit)
    while (q.size() > 0) {
      cur = q.poll();
      if (false) {
        cache[cur.id] = 
          MooreCurve.staticEncode(cur.x >> cur.right, cur.y >> cur.right,
              left - cur.right + 1) << (cur.right << 1);
      } else {
        cache[cur.id] = 
          MooreCurve.encodeLowerBits(cur.x >> cur.right, cur.y >> cur.right,
              left- cur.right + 1, 
              p0 >> cur.right, p1 >> cur.right) << (cur.right << 1);
      }
      if (cur.right <= right ) {
        continue;
      }
      childID = (cur.id << 2) + 1;
      nextRight = cur.right - 1;
      unit = 1 << nextRight;
      q.add(new Tuple(cur.x, cur.y, 
                      childID++, nextRight));
      q.add(new Tuple(cur.x | unit, cur.y, 
                      childID++, nextRight));
      q.add(new Tuple(cur.x, cur.y | unit,
                      childID++, nextRight));
      q.add(new Tuple(cur.x | unit, cur.y | unit,
                      childID, nextRight));
      
    }
  }

  public static void main(String args[]) {
    int left = 4;
    int right = 0;
    SubQuadTreeCache treeCache = 
      new SubQuadTreeCache(0, 0, 0, 0, left, right, true);

    long newLineCnt = 0;
    long lineLen = 1;
    long cnt = 0;
    for (int i = 0 ; i < treeCache.cache.length; ++i) {
      if (i >= newLineCnt) {
        if (newLineCnt > 0)
          lineLen <<= 1;
        System.out.println("\n================================");
        newLineCnt = (newLineCnt << 2) + 1;
        cnt = 0;
      }
      System.out.print(treeCache.cache[i] + "\t");
      ++cnt;
      if (cnt >= lineLen) {
        System.out.println("\n");
        cnt = 0;
      }
    }

    int offset = 0;
    for (int r = 1; r <= left - right; ++r) {
      offset = (offset << 2) + 1;
      for (long i = 0 ; i < (1 << r); ++i) {
        for (long j = 0 ; j < (1 << r); ++j) {
          int index = (int)SpaceFillingCurve.interleaveBits(i, j, r);
          System.out.print(treeCache.cache[index + offset] + "\t");
        }
        System.out.println("\n");
      }
      System.out.println("\n");
    }
  }
}
