import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

public class RectangleScanNumberTest {
  public static final double PI = 3.1415926535;
  private static Random rand = new Random();;

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


  public static void  testOneConf(GeoRequestParser grp,
      double xx, double yy, long testNum, long seed, 
      ArrayList<Long> res, ArrayList<Long> area) {
    rand.setSeed(seed);
    double ax, ay;
    double angle;
    GeoRequest gr;
    LinkedList<Range> ranges;
    Rectangle rect = null;
    while (testNum-- > 0) {
      // generating a random rectangle
      ax = grp.getGeoEncoding().getGeoContext().getMaxX() * rand.nextDouble();
      ay = grp.getGeoEncoding().getGeoContext().getMaxY() * rand.nextDouble();
      angle = 2 * Math.PI * rand.nextDouble();
      rect = new Rectangle(ax, ay, xx, yy);
      gr = new RectangleGeoRequest(rect.rotate(angle));
      

      ranges = grp.getScanRanges(gr);
      if (null == ranges) {
        res.add(0L);
        area.add(0L);
      } else {
        res.add(new Long(ranges.size()));
        long tmpArea = 0;
        for (Range r: ranges) {
          tmpArea += (r.getEnd() - r.getStart() + 1);
        }
        area.add(tmpArea);
      }
    }
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
    long testNum = 50;

    try {
      PrintWriter writer = 
        new PrintWriter("rectangle.txt", "UTF-8");

      double requestArea = setXX * setYY;

      for (resolution = 10; resolution <= maxResolution; ++resolution) {
        double tileXLen = maxX / ((double)(1 << resolution));
        double tileYLen = maxY / ((double)(1 << resolution));
        GeoContext gc = new GeoContext(resolution, maxX, maxY);
        //GeoEncoding ge = new MooreGeoEncoding(gc);
        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);
        GeoEncoding sge = new StripGeoEncoding(gc);

        GeoRequestParser zgrp = new QuadTreeGeoRequestParser(zge);
        GeoRequestParser mgrp = new QuadTreeGeoRequestParser(mge);
        GeoRequestParser sgrp = new QuadTreeGeoRequestParser(sge);

        ArrayList<Long> zRes = new ArrayList<Long> ();
        ArrayList<Long> zArea = new ArrayList<Long> ();
        testOneConf(zgrp, setXX, setYY, testNum, seed, zRes, zArea);
        double zMaxArea = getMax(zArea) * tileXLen * tileYLen;

        ArrayList<Long> mRes = new ArrayList<Long> ();
        ArrayList<Long> mArea = new ArrayList<Long> ();
        testOneConf(mgrp, setXX, setYY, testNum, seed, mRes, mArea);
        double mMaxArea = getMax(mArea) * tileXLen * tileYLen;

        ArrayList<Long> sRes = new ArrayList<Long> ();
        ArrayList<Long> sArea = new ArrayList<Long> ();
        testOneConf(sgrp, setXX, setYY, testNum, seed, sRes, sArea);
        double sMaxArea = getMax(sArea) * tileXLen * tileYLen;

        //write res to file
        for (int i = 0 ; i < testNum; ++i) {
          writer.println(resolution + ", " + setXX + ", " + setYY + ", " 
              + zRes.get(i) + ", " + mRes.get(i) + ", " 
              + sRes.get(i) + ", "
              + (zMaxArea / requestArea) + ", " 
              + (mMaxArea / requestArea) + ", " 
              + (sMaxArea / requestArea)); 
        }
      }

      writer.close();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

}
