import java.util.LinkedList;
/**
 * This class tests 
 * 1. a cdf of how many scan requests fall into the same block
 * 2. the radio of effective data over all data
 */
public class EffectiveRatioTest {

  public static boolean isOverlap(Block block, Range range) {
    if (block.getFirstKey() > range.getEnd()
        || block.getLastKey() < range.getStart())
      return false;
    return true;
  }

  public static long hitKeyNum(Block block, Range range) {
    if (block.getLastKey() < range.first)
      return 0;

    long cnt = 0;
    for (Pair<Long, Long> kv : block.data) {
      if (kv.first > range.getEnd())
        return cnt;
      if (kv.first > range.getStart() 
          && kv.first < range.getEnd())
        ++cnt;
    }
    return cnt;
  }

  public static void getStat(LinkedList<Block> blocks, 
                             LinkedList<Range> ranges,
                             LinkedList<Long> blockHitCnts,
                             LinkedList<Long> keyHitCnts) {
    long blockHitCnt = 0;
    long keyHitCnt = 0;
    long tmpKeyHitCnt = 0;
    long blockInd = 0;
    long rangeInd = 0;
    Block curBlock = null;
    Range curRange = null;
    while (blockInd < blocks.size() && rangeInd < ranges.size()) {
      curBlock = blocks.get(blockInd);
      curRange = ranges.get(rangeInd);
      tmpKeyHitCnt = hitKeyNum(curBlock, currange);
      if (tmpKeyHitCnt > 0) {
        keyHitCnt += tmpKeyHitCnt;
        ++blockHitCnt;
      }
      if (curBlock.getLastKey() > curRange.getEnd()) {
        ++rangeInd;
      } else {
        ++blockInd;
        blockHitCnts.add(blockHitCnt);
        keyHitCnts.add(keyHitCnt);
        blockHitCnt = 0;
        keyHitCnt = 0;
      }
    }
  }

  /**
   * @return effective ratio
   */
  public static double testOne(GeoContext gc, 
      GeoEncoding ge, GeoRequestParser grp, 
      long kvNum, long reqNum, long xx, long yy, 
      LinkedList<Long> blockCdf) {
    MockDataStore server = new MockDataStore(ge);
    MockClient client = new MockClient(gc, grp);
    client.connect(server);

    client.insertMultiRandKv(kvNum);
    server.flushAll();

    LinkedList<Block> blocks = null;
    RectangleGeoRequest rgr = null;
    LinkedList<Range> ranges = null;
    LinkedList<Long> blockHitCnts = null;
    LinkedList<Long> keyHitCnts = null;
    long totalKey = 0;
    long effectKey = 0;
    for (int i = 0 ; i < reqNum; ++i) {
      rgr = client.genRandRectGeoReq(xx, yy);
      ranges = grp.getScanRanges(rgr);
      blocks = fetchAll(ranges);
      blockHitCnts = new LinkedList<Long> ();
      keyHitCnts = new LinkedList<Long> ();
      getStat(blocks, ranges, blockHitCnts, keyHitCnts);

      blockCdf.addAll(blockHitCnts);
      for (Block block : blocks)
        totalKey += block.size();

      for (Long keyHitCnt : keyHitCnts)
        effectKey += keyHitCnt;
    }

    return effectKey / (double) totalKey;
  }

  public static void main(String args[]) {

    double setXX = Double.parseDouble(args[0]);
    double setYY = Double.parseDouble(args[1]);
    long maxResolution = Long.parseLong(args[2]);
    long kvNum = Long.parseLong(args[3]);
    long reqNum = Long.parseLong(args[4]);

    long maxX = 100000;
    long maxY = 100000;

    try {
      for (resolution = 10; resolution <= maxResolution; ++resolution) {
        GeoContext gc = new GeoContext(resolution, maxX, maxY);

        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoRequestParser grp = new QuadTreeGeoRequestParser(zge);

        MockDataStore zServer = new MockDataStore(zge);
        MockClient zClient = new MockClient(gc, grp);
        zClient.connect(zServer);

        LinkedList<Long> blockCdf = new LinkedList<Long> ();
        double effectRatio = 
          testOne(gc, zge, grp, kvNum, reqNum, setXX, setYY, blockCdf);

        //write to file
      }
    } catch (Exception ex) {
    }

  }
}
