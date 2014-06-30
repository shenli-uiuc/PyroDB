import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.IOException;

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
    //System.out.println(block.getFirstKey() + ", " + block.getLastKey()
    //    + ", " + range.getStart() + ", " + range.getEnd());
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
                             LinkedList<Range> hiRanges,
                             LinkedList<Long> blockHitCnts,
                             LinkedList<Long> keyHitCnts) {
    long blockHitCnt = 0;
    int blockInd = 0;
    int rangeInd = 0;
    Block curBlock = null;
    Range curRange = null;
    while (blockInd < blocks.size() && rangeInd < ranges.size()) {
      curBlock = blocks.get(blockInd);
      curRange = ranges.get(rangeInd);
      if (isOverlap(curBlock, curRange)) {
        ++blockHitCnt;
      }
      if (curBlock.getLastKey() > curRange.getEnd()) {
        ++rangeInd;
      } else {
        ++blockInd;
        blockHitCnts.add(blockHitCnt);
        blockHitCnt = 0;
      }
    }
    blockInd = 0;
    rangeInd = 0;
    long keyHitCnt = 0;
    long tmpKeyHitCnt = 0;
    while (blockInd < blocks.size() && rangeInd < hiRanges.size()) {
      curBlock = blocks.get(blockInd);
      curRange = hiRanges.get(rangeInd);
      tmpKeyHitCnt = hitKeyNum(curBlock, curRange);
      if (tmpKeyHitCnt > 0) {
        keyHitCnt += tmpKeyHitCnt;
      }
      if (curBlock.getLastKey() > curRange.getEnd()) {
        ++rangeInd;
      } else {
        ++blockInd;
        keyHitCnts.add(keyHitCnt);
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
      long reqResolution, long hiResolution, 
      LinkedList<Long> blockCdf) {
    MockDataStore server = new MockDataStore(ge);
    MockClient client = new MockClient(gc, grp);
    client.connect(server);

    client.setRandSeed(0);
    client.insertMultiRandKv(kvNum);
    server.flushAll();


    LinkedList<Block> blocks = null;
    RectangleGeoRequest rgr = null;
    RectangleGeoRequest hiRgr = null;
    LinkedList<Range> ranges = null;
    LinkedList<Range> hiRanges = null;
    LinkedList<Long> blockHitCnts = null;
    LinkedList<Long> keyHitCnts = null;
    long totalKey = 0;
    long effectKey = 0;

    client.setRandSeed(0);
    //TODO: generate request outside of this method
    for (int i = 0 ; i < reqNum; ++i) {
      rgr = client.genRandRectGeoReq(xx, yy, reqResolution);
      ranges = grp.getScanRanges(rgr);
      rgr.setResolution(hiResolution);
      hiRanges = grp.getScanRanges(rgr);
      blocks = client.fetchAll(ranges);
      //MockClient.printBlocksAndRanges(blocks, ranges);
      blockHitCnts = new LinkedList<Long> ();
      keyHitCnts = new LinkedList<Long> ();
      getStat(blocks, ranges, hiRanges, blockHitCnts, keyHitCnts);

      // TODO: make blockCdf a hashtable
      blockCdf.addAll(blockHitCnts);
      for (Block block : blocks)
        totalKey += block.data.size();

      for (Long keyHitCnt : keyHitCnts)
        effectKey += keyHitCnt;
    }

    return effectKey / (double) totalKey;
  }

  private static void writeCdf(long kvNum, 
      long resolution, String name, LinkedList<Long> cdf) throws IOException {
    PrintWriter writer = 
      new PrintWriter("rect_stat_" + name + "_" + kvNum + "_" + resolution + ".txt");
    for (Long cnt : cdf) {
      writer.write(cnt + "\n");
    }
    writer.close();
  }

  public static void main(String args[]) {

    double setXX = Double.parseDouble(args[0]);
    double setYY = Double.parseDouble(args[1]);
    long maxResolution = Long.parseLong(args[2]);
    long kvNum = Long.parseLong(args[3]);
    long reqNum = Long.parseLong(args[4]);

    long maxX = 5000;
    long maxY = 5000;

    long hiResolution = maxResolution + 1;
    try {
      PrintWriter effectWriter = 
        new PrintWriter("rect_effect_" + kvNum + ".txt");

      LinkedList<Range> maxRanges = null;
      for (long resolution = maxResolution; resolution >= 7; --resolution) {
        GeoContext gc = new GeoContext(hiResolution, maxX, maxY);

        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);
        GeoEncoding sge = new StripGeoEncoding(gc);

        GeoRequestParser zgrp = new QuadTreeGeoRequestParser(zge);
        GeoRequestParser mgrp = new QuadTreeGeoRequestParser(mge);
        GeoRequestParser sgrp = new QuadTreeGeoRequestParser(sge);

        LinkedList<Long> zCdf = new LinkedList<Long> ();
        LinkedList<Long> mCdf = new LinkedList<Long> ();
        LinkedList<Long> sCdf = new LinkedList<Long> ();

        double zRatio = 
          testOne(gc, zge, zgrp, kvNum, reqNum, 
                  setXX, setYY, resolution, hiResolution, zCdf);
        double mRatio =
          testOne(gc, mge, mgrp, kvNum, reqNum,
                  setXX, setYY, resolution, hiResolution, mCdf);
        double sRatio =
          testOne(gc, sge, sgrp, kvNum, reqNum,
                  setXX, setYY, resolution, hiResolution, sCdf);

        //write effective ratio
        effectWriter.write(resolution + ", " + setXX + ", " + setYY + ", "
            + zRatio + ", " + mRatio + ", " + sRatio + "\n");


        //Write cdf
        writeCdf(kvNum, resolution, "zcdf", zCdf);
        writeCdf(kvNum, resolution, "mcdf", mCdf);
        writeCdf(kvNum, resolution, "scdf", sCdf);
      }
      effectWriter.close();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
    }

  }
}
