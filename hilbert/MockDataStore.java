import java.util.ArrayList;

/**
 * mocking a hbase data store to calculate block boundaries.
 */
public class MockDataStore {

  public static final long DEFAULT_BLOCK_SIZE = 64 * 1024;

  private long blockSize = 0;

  private ArrayList<Block> blocks = null;
  private Block curBlock = null;

  private long [] index = null; 

  private GeoEncoding ge = null;

  public MockDataStore(GeoEncoding ge) {
    this(DEFAULT_BLOCK_SIZE, ge);
  }

  public MockDataStore(long blockSize, GeoEncoding ge) {
    this.blockSize = blockSize;
    this.blocks = new ArrayList<Block> ();
    this.curBlock = new Block();
    this.ge = ge;
  }

  public void add(double x, double y, long kvlen) {
    curBlock.write(ge.encode(x, y), kvlen); 
  }

  public void flushAll() {
  }

  /**
   * flush Current block
   */
  public void flushBlock() {
    blocks.add(curBlock);
    curBlock = new Block();
  }
}
