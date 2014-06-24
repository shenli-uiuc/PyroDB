import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

public class ScanNumberTest {

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


  public static ArrayList<Long> testFixedRadius(GeoRequestParser grp,
      long radius, long testNum, long seed) {
    rand.setSeed(seed);
    ArrayList<Long> res = new ArrayList<Long> ();
    double cx, cy;
    GeoRequest gr;
    LinkedList<Range> ranges;
    while (testNum-- > 0) {
      cx = grp.getGeoEncoding().getGeoContext().getMaxX() * rand.nextDouble();
      cy = grp.getGeoEncoding().getGeoContext().getMaxY() * rand.nextDouble();
      gr = new DiskGeoRequest(cx, cy, radius);
      ranges = grp.getScanRanges(gr);
      if (null == ranges) {
        res.add(0L);
      } else {
        res.add(new Long(ranges.size()));
      }
    }
    return res;
  }

  public static void main(String args[]) {

    // size of earth in meters
    long maxX = 40000000L;
    long maxY = 40000000L;
    long resolution = 10;

    long seed = 0;
    long testNum = 50;
    long radius = 5000;

    try {
      // fixed radius test
      PrintWriter writer = new PrintWriter("fix_radius_" + radius + ".txt", "UTF-8");

      for (resolution = 10; resolution <= 25; ++resolution) {
        GeoContext gc = new GeoContext(resolution, maxX, maxY);
        //GeoEncoding ge = new MooreGeoEncoding(gc);
        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);
        GeoEncoding sge = new StripGeoEncoding(gc);

        GeoRequestParser zgrp = new QuadTreeGeoRequestParser(zge);
        GeoRequestParser mgrp = new QuadTreeGeoRequestParser(mge);
        GeoRequestParser sgrp = new QuadTreeGeoRequestParser(sge);

        ArrayList<Long> zRes = testFixedRadius(zgrp, radius, testNum, seed);
        ArrayList<Long> mRes = testFixedRadius(mgrp, radius, testNum, seed);
        ArrayList<Long> sRes = testFixedRadius(sgrp, radius, testNum, seed);

        //write res to file
        for (int i = 0 ; i < testNum; ++i) {
          writer.println(resolution + ", " 
              + zRes.get(i) + ", " 
              + mRes.get(i) + ", "
              + sRes.get(i)); 
        }
      }

      writer.close();

      // fixed radius test

      resolution = 25;
      writer = new PrintWriter("fix_resolution_" 
          + resolution + ".txt", "UTF-8");

      long step = 200;
      long maxRadius = 10000;
      for (radius = step; radius <= maxRadius; radius += step) {
        GeoContext gc = new GeoContext(resolution, maxX, maxY);
        //GeoEncoding ge = new MooreGeoEncoding(gc);
        GeoEncoding zge = new ZGeoEncoding(gc);
        GeoEncoding mge = new MooreGeoEncoding(gc);
        GeoEncoding sge = new StripGeoEncoding(gc);

        GeoRequestParser zgrp = new QuadTreeGeoRequestParser(zge);
        GeoRequestParser mgrp = new QuadTreeGeoRequestParser(mge);
        GeoRequestParser sgrp = new QuadTreeGeoRequestParser(sge);

        ArrayList<Long> zRes = testFixedRadius(zgrp, radius, testNum, seed);
        ArrayList<Long> mRes = testFixedRadius(mgrp, radius, testNum, seed);
        ArrayList<Long> sRes = testFixedRadius(sgrp, radius, testNum, seed);

        //write res to file
        for (int i = 0 ; i < testNum; ++i) {
          writer.println(resolution + ", " 
              + zRes.get(i) + ", " 
              + mRes.get(i) + ", "
              + sRes.get(i)); 
        }
      }

      writer.close();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

}
