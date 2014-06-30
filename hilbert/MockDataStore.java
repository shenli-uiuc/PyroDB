import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Comparator;

/**
 * mocking a hbase data store to calculate block boundaries.
 */
public class MockDataStore {

  public static final long DEFAULT_MAX_BLOCK_SIZE = 64 * 1024;

  private long maxBlockSize = 0;

  //TODO: add a cache
  private ArrayList<Block> blocks = null;
  private Block curBlock = null;
  private PriorityQueue<Pair<Long, Long>> cache;

  /**
   * index[i] represent the starting key of the
   * ith block.
   */
  private ArrayList<Long> index = null; 

  private GeoEncoding ge = null;

  public MockDataStore(GeoEncoding ge) {
    this(DEFAULT_MAX_BLOCK_SIZE, ge);
  }

  public MockDataStore(long maxBlockSize, GeoEncoding ge) {
    this.maxBlockSize = maxBlockSize;
    this.blocks = new ArrayList<Block> ();
    this.curBlock = new Block();
    this.ge = ge;
    Comparator<Pair<Long, Long> > comparator = 
      new KeyComparator();
    this.cache = 
      new PriorityQueue<Pair<Long, Long> >(10, comparator);
  }

  class KeyComparator implements Comparator<Pair<Long, Long> > {
    @Override
    public int compare(Pair<Long, Long> x, Pair<Long, Long> y) {
      if (x.first < y.first)
        return -1;
      else if (x.first > y.first)
        return 1;
      else
        return 0;
    }
  }

  public void add(double x, double y, long kvlen) {
    this.cache.add(new Pair<Long, Long> (ge.encode(x, y), kvlen));
  }

  public void flushAll() {
    Pair<Long, Long> kv = null;
    index = new ArrayList<Long> ();
    long prevKey = 0;
    long curKey = 0;
    while(cache.size() > 0) {
      kv = cache.poll();
      if (curBlock.write(kv) >= maxBlockSize) {
        curKey = curBlock.getFirstKey();
        if (curKey < prevKey) {
          System.out.println("flushAll exception");
          throw new IllegalStateException("Current key " + curKey
            + " is smaller than previous key " + prevKey);
        }
        flushBlock();
        index.add(curKey);
        prevKey = curKey;
      }
    }
  }

  public ArrayList<Block> scan(long startKey, long endKey) {
    if (null == index) 
      return null;

    ArrayList<Block> res = new ArrayList<Block>();
    int startInd = Collections.binarySearch(index, startKey);
    int endInd = Collections.binarySearch(index, endKey);
    if (startInd < 0) {
      // set the startInd to the block with the largest start key
      // which is smaller than startKey
      startInd = -startInd - 2;
      if (startInd < 0) {
        //all inserted keys are smaller than startKey
        startInd = 0;
      }
    }

    if (endInd < 0) {
      endInd = -endInd - 2;
    }

    if (endInd < startInd) {
      // both startKey and endKey are smaller than all
      // inserted keys
      return null;
    }

    Block block = null;
    for (int i = startInd; i <= endInd; ++i) {
      block = blocks.get(i);
      if (block.getLastKey() >= startKey)
        res.add(blocks.get(i));
    }

    return res;
  }

  /**
   * flush Current block
   */
  public void flushBlock() {
    if (curBlock.data.size() <= 0) {
      return;
    }
    blocks.add(curBlock);
    curBlock = new Block();
  }
}
