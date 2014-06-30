import java.util.ArrayList;
import java.util.Arrays;

/**
 * mocking a hbase data store to calculate block boundaries.
 */
public class MockDataStore {

  public static final long DEFAULT_MAX_BLOCK_SIZE = 64 * 1024;

  private long maxBlockSize = 0;

  //TODO: add a cache
  private ArrayList<Block> blocks = null;
  private Block curBlock = null;

  /**
   * index[i] represent the starting key of the
   * ith block.
   */
  private long [] index = null; 

  private GeoEncoding ge = null;

  public MockDataStore(GeoEncoding ge) {
    this(DEFAULT_MAX_BLOCK_SIZE, ge);
  }

  public MockDataStore(long maxBlockSize, GeoEncoding ge) {
    this.maxBlockSize = maxBlockSize;
    this.blocks = new ArrayList<Block> ();
    this.curBlock = new Block();
    this.ge = ge;
  }

  public void add(double x, double y, long kvlen) {
    curBlock.write(ge.encode(x, y), kvlen);
    if (curBlock.size() >= maxBlockSize) 
      flushBlock();
  }

  public void flushAll() {
    flushBlock();
    // calculate index for blocks
    index = new long[blocks.size()];
    long prevKey = 0;
    long curKey = 0;
    for (int i = 0; i < blocks.size(); ++i) {
      curKey = blocks.get(i).data.get(0).first; 
      if (curKey < prevKey) {
        System.out.println("flushAll exception");
        throw new IllegalStateException("Current key " + curKey
            + " is smaller than previous key " + prevKey);
      }
      index[i] = curKey;
      prevKey = curKey;
    }
  }

  public ArrayList<Block> scan(long startKey, long endKey) {
    if (null == index) 
      return null;

    ArrayList<Block> res = new ArrayList<Block>();
    int startInd = Arrays.binarySearch(index, startKey);
    int endInd = Arrays.binarySearch(index, endKey);
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

    for (int i = startInd; i <= endInd; ++i) {
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
