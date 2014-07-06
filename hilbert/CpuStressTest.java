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
    // size of earth in meters
    long maxX = 40000000L;
    long maxY = 40000000L;
    long resolution = 0;

    long seed = 0;
    long testNum = 200;
    Pair<Long, Long> res = null;
    try {
      PrintWriter writer = 
        new PrintWriter("cpu_stress.txt", "UTF-8");

      for (resolution = 10; resolution <= maxResolution; ++resolution) {
        GeoContext gc = new GeoContext(maxResolution, maxX, maxY);
        //GeoEncoding ge = new MooreGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);

        GeoRequestParser mgrp = new QuadTreeGeoRequestParser(mge);

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
