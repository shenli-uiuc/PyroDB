import java.util.LinkedList;
import java.io.PrintWriter;

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
    System.out.println(block.getFirstKey() + ", " + block.getLastKey()
        + ", " + range.getStart() + ", " + range.getEnd());
    if (block.getLastKey() < range.getStart())
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
    int blockInd = 0;
    int rangeInd = 0;
    Block curBlock = null;
    Range curRange = null;
    while (blockInd < blocks.size() && rangeInd < ranges.size()) {
      curBlock = blocks.get(blockInd);
      curRange = ranges.get(rangeInd);
      tmpKeyHitCnt = hitKeyNum(curBlock, curRange);
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
      long kvNum, long reqNum, double xx, double yy, 
      LinkedList<Long> blockCdf) {
    System.out.println("In testOne " + reqNum);
    MockDataStore server = new MockDataStore(ge);
    MockClient client = new MockClient(gc, grp);
    client.connect(server);

    client.insertMultiRandKv(kvNum);
    server.flushAll();

    System.out.println("After Insertion");

    LinkedList<Block> blocks = null;
    RectangleGeoRequest rgr = null;
    LinkedList<Range> ranges = null;
    LinkedList<Long> blockHitCnts = null;
    LinkedList<Long> keyHitCnts = null;
    long totalKey = 0;
    long effectKey = 0;

    System.out.println("Before loop");
    for (int i = 0 ; i < reqNum; ++i) {
      System.out.println("In Loop");
      rgr = client.genRandRectGeoReq(xx, yy);
      ranges = grp.getScanRanges(rgr);
      blocks = client.fetchAll(ranges);
      MockClient.printBlocksAndRanges(blocks, ranges);
      blockHitCnts = new LinkedList<Long> ();
      keyHitCnts = new LinkedList<Long> ();
      getStat(blocks, ranges, blockHitCnts, keyHitCnts);

      blockCdf.addAll(blockHitCnts);
      for (Block block : blocks)
        totalKey += block.data.size();

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

    long maxX = 1000;
    long maxY = 1000;

    try {
      PrintWriter effectWriter = 
        new PrintWriter("rect_effect_" + kvNum + ".txt");

      for (long resolution = 12; resolution <= maxResolution; ++resolution) {
        GeoContext gc = new GeoContext(resolution, maxX, maxY);

        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoRequestParser grp = new QuadTreeGeoRequestParser(zge);

        MockDataStore zServer = new MockDataStore(zge);
        MockClient zClient = new MockClient(gc, grp);
        zClient.connect(zServer);

        LinkedList<Long> zCdf = new LinkedList<Long> ();
        LinkedList<Long> tmpZCdf = new LinkedList<Long> ();
        double zRatio = 
          testOne(gc, zge, grp, kvNum, reqNum, setXX, setYY, tmpZCdf);
        zCdf.addAll(tmpZCdf);

        //write effective ratio
        effectWriter.write(resolution + ", " + setXX + ", " + setYY + ", "
            + zRatio + "\n");


        //Write cdf
        PrintWriter zCdfWriter = 
          new PrintWriter("rect_stat_zcdf_" + kvNum + "_" + resolution + ".txt");
        for (Long cnt : zCdf) {
          zCdfWriter.write(cnt + "\n");
        }
        zCdfWriter.close();

      }
      effectWriter.close();
    } catch (Exception ex) {
    }

  }
}
