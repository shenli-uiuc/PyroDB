import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

/**
 * Stress test the number of geometry translation the CPU may 
 * perform.
 */
public class CpuStressTest {
  private static Random rand = new Random();

  public static void printRanges(LinkedList<Range> ranges) {
    if (null == ranges) {
      System.out.println("Empty List");
      return;
    }
    System.out.println("Ranges number = " + ranges.size());
    for (Range r : ranges) {
      System.out.print("(" + r.getStart() + ", "
                           + r.getEnd() + "); ");
    }
    System.out.println();
  }


  public static Pair<Long, Long> testOneConf(GeoRequestParser grp,
      double xx, double yy, long testNum, long seed, long reqResolution) {
    rand.setSeed(seed);
    double ax, ay;
    double angle;
    GeoRequest gr;
    LinkedList<Range> ranges;
    Rectangle rect = null;
    long dur = 0, start, end;
    long rangeNum = 0;
    while (testNum-- > 0) {
      // generating a random rectangle
      ax = grp.getGeoEncoding().getGeoContext().getMaxX() * rand.nextDouble();
      ay = grp.getGeoEncoding().getGeoContext().getMaxY() * rand.nextDouble();
      angle = 2 * Math.PI * rand.nextDouble();
      rect = new Rectangle(ax, ay, xx, yy);
      gr = new RectangleGeoRequest(rect.rotate(angle),
                                   reqResolution);
      start = System.currentTimeMillis();
      ranges = grp.getScanRanges(gr);
      end = System.currentTimeMillis();
      dur += (end - start);
      if (null != ranges)
        rangeNum += ranges.size();
    }
    return new Pair<Long, Long>(dur, rangeNum);
  }

  private static long getMax(ArrayList<Long> arr) {
    long max = arr.get(0);
    for (int i = 0; i < arr.size(); ++i) {
      if (max < arr.get(i)) 
        max = arr.get(i);
    }
    return max;
  }

  public static void main(String args[]) {

    double setXX = Double.parseDouble(args[0]);
    double setYY = Double.parseDouble(args[1]);
    long maxResolution = Long.parseLong(args[2]);
    boolean cached = false;
    int cacheSize = 0;
    if (args.length > 4) {
      cached = Boolean.parseBoolean(args[3]);
      cacheSize = Integer.parseInt(args[4]);
    }

    // key 8 byte; range 8 * 3; next 8; prev 8
    int CACHE_ENTRY_MEM_SIZE = 8 * 6;
    // size of earth in meters
    long maxX = 40000000L;
    long maxY = 40000000L;
    long resolution = 0;

    long seed = 10;
    long testNum = 200;
    Pair<Long, Long> res = null;
    try {
      String name = null;
      if (cached) {
        name = "cpu_stress_cached.txt";
      } else {
        name = "cpu_stress.txt";
      }
      PrintWriter writer = 
        new PrintWriter(name, "UTF-8");

      for (resolution = 10; resolution <= maxResolution; ++resolution) {
        GeoContext gc = new GeoContext(maxResolution, maxX, maxY);
        //GeoEncoding ge = new MooreGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);

        GeoRequestParser mgrp = null;
        if (cached) {
          int maxEntries = cacheSize /  CACHE_ENTRY_MEM_SIZE;
          mgrp = new LruQuadTreeGeoRequestParser(mge, maxEntries);
          //warm-up
          testOneConf(mgrp, setXX, setYY, testNum, seed + 1, resolution);
          testOneConf(mgrp, setXX, setYY, testNum, seed + 2, resolution);
          testOneConf(mgrp, setXX, setYY, testNum, seed + 3, resolution);
          testOneConf(mgrp, setXX, setYY, testNum, seed + 4, resolution);
          testOneConf(mgrp, setXX, setYY, testNum, seed + 5, resolution);
        } else {
          mgrp = new QuadTreeGeoRequestParser(mge);
        }

        res = testOneConf(mgrp, setXX, setYY, testNum, seed, resolution);

        //write res to file
        writer.println(resolution + ", " + testNum + ", " 
            + res.first + ", " + res.second); 
      }

      writer.close();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

}
