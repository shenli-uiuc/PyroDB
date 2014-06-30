import java.util.Random;
import java.util.LinkedList;
import java.util.ArrayList;

public class MockClient {
  public static long DEFAULT_KV_LEN = 1000;
  private long kvLen = 0;
  private GeoContext gc = null;
  private Random rand = null; 
  private GeoRequestParser grp = null;
  private MockDataStore server = null;

  public MockClient(GeoContext gc, GeoRequestParser grp) {
    this.gc = gc;
    this.grp = grp;
    this.rand = new Random();
    this.kvLen = DEFAULT_KV_LEN;
  }

  public void setKvLen(long kvLen) {
    this.kvLen = kvLen;
  }

  public void connect(MockDataStore server) {
    this.server = server;
  }

  public void insertOneRandomKV() {
    if (null == server) {
      throw new IllegalStateException("Server not connected yet");
    }
    double x = this.gc.getMaxX() * rand.nextDouble();
    double y = this.gc.getMaxY() * rand.nextDouble();
    server.add(x, y, kvLen);
  }

  public void insertMultiRandKv(long num) {
    for (int i = 0; i < num; ++i) {
      insertOneRandomKV();
    }
  }

  public void setRandSeed(long seed) {
    rand.setSeed(seed);
  }

  public RectangleGeoRequest genRandRectGeoReq(double xx, 
                                               double yy,
                                               long resolution) {
    double ax = this.gc.getMaxX() * rand.nextDouble();
    double ay = this.gc.getMaxY() * rand.nextDouble();
    double angle = 2 * Math.PI * rand.nextDouble();
    Rectangle rect = new Rectangle(ax, ay, xx, yy);
    System.out.println(rect.toString());
    return new RectangleGeoRequest(rect.rotate(angle), resolution);
  }

  public LinkedList<Block> issueRandRectReq(double xx, 
                                            double yy, 
                                            long resolution) {
    RectangleGeoRequest rgr = genRandRectGeoReq(xx, yy, resolution);
    LinkedList<Range> ranges = grp.getScanRanges(rgr);
    return fetchAll(ranges);
  }

  public LinkedList<Block> fetchAll(LinkedList<Range> ranges) {
    if (null == server) {
      throw new IllegalStateException("haven't connected to the server yet");
    }

    LinkedList<Block> res = new LinkedList<Block> ();

    if (null == ranges) {
      return res;
    }

    ArrayList<Block> tmp = null;
    long curLastKey = 0;
    long startKey, endKey;
    for (int i = 0 ; i < ranges.size(); ++i) {
      startKey = ranges.get(i).getStart();
      endKey = ranges.get(i).getEnd();
      if (curLastKey >= endKey)
        continue;
      if (startKey < curLastKey)
        startKey = curLastKey + 1;
      tmp = server.scan(startKey, endKey);
      if (null != tmp && tmp.size() > 0) {
        if (res.size() <= 0
            || res.get(res.size() - 1).getLastKey()
            <  tmp.get(tmp.size() - 1).getLastKey())
          res.addAll(tmp);
      }
      if (null != res && res.size() > 0) {
        curLastKey = res.getLast().getLastKey();
      }
    }

    return res;
  }

  public static void printBlocksAndRanges(LinkedList<Block> blocks, 
                                          LinkedList<Range> ranges) {
    System.out.println("blocks " + (null == blocks ? 0 : blocks.size()));
    for (Block block : blocks) {
      System.out.print("(" + block.getFirstKey() + ", " 
          + block.getLastKey() + "), " + block.data.size() + "; ");
    }
    System.out.println();
    System.out.println("ranges " + (null == ranges ? 0 : ranges.size()));
    for (Range range : ranges) {
      System.out.print("(" + range.getStart() + ", " 
          + range.getEnd() + "); ");
    }
    System.out.println();
  }

  // for testing
  public static void main(String args[]) {
    GeoContext gc = new GeoContext(10, 1000, 1000);
    GeoEncoding zge = new ZGeoEncoding(gc);
    GeoRequestParser grp = new QuadTreeGeoRequestParser(zge);

    MockDataStore server = new MockDataStore(zge);
    MockClient client = new MockClient(gc, grp);
    client.connect(server);
    server.add(100, 100, 20000);
    server.add(101, 101, 20000);
    server.add(105, 95, 20000);
    server.add(106, 109, 20000);
    server.add( 60,  60, 40000);
    server.add( 50,  50, 30000);
    server.flushAll();
    Rectangle rect = new Rectangle(90, 90, 20, 20);
    RectangleGeoRequest rgr = new RectangleGeoRequest(rect, 10);
    LinkedList<Range> ranges = grp.getScanRanges(rgr);
    LinkedList<Block> blocks = client.fetchAll(ranges);
    printBlocksAndRanges(blocks, ranges);   
  }
}
