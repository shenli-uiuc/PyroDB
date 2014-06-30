import java.util.Random;
import java.util.LinkedList;

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

  public RectangleGeoRequest genRandRectGeoReq(double xx, 
                                               double yy) {
    double ax = this.gc.getMaxX() * rand.nextDouble();
    double ay = this.gc.getMaxY() * rand.nextDouble();
    double angle = 2 * Math.PI * rand.nextDouble();
    Rectangle rect = new Rectangle(ax, ay, xx, yy);
    return new RectangleGeoRequest(rect.rotate(angle));
  }

  public LinkedList<Block> issueRandRectReq(double xx, double yy) {
    RectangleGeoRequest rgr = genRandRectGeoReq(xx, yy);
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

    LinkedList<Block> tmp = null;
    long curLastKey = 0;
    long startKey, endKey;
    for (int i = 0 ; i < ranges.size(); ++i) {
      startKey = ranges.get(i).first;
      endKey = ranges.get(i).second;
      if (curLastKey >= endKey)
        continue;
      startKey = curLastKey + 1;
      tmp = server.scan(startKey, endKey);
      if (null != tmp)
        res.addAll(tmp);
      if (null != res && res.size() > 0) {
        curLastKey = res.getLast().getLastKey();
      }
    }

    return res;
  }
}
