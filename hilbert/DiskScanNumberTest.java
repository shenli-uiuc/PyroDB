import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;
import java.io.PrintWriter;

public class DiskScanNumberTest {
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


  public static void  testFixedRadius(GeoRequestParser grp,
      long radius, long testNum, long seed, 
      ArrayList<Long> res, ArrayList<Long> area) {
    rand.setSeed(seed);
    double cx, cy;
    GeoRequest gr;
    LinkedList<Range> ranges;
    while (testNum-- > 0) {
      cx = grp.getGeoEncoding().getGeoContext().getMaxX() * rand.nextDouble();
      cy = grp.getGeoEncoding().getGeoContext().getMaxY() * rand.nextDouble();
      gr = new DiskGeoRequest(cx, cy, radius, 
          grp.getGeoEncoding().getGeoContext().getMaxResolution());

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

    long setRadius = Long.parseLong(args[0]);
    long setResolution = Long.parseLong(args[1]);
    // size of earth in meters
    long maxX = 40000000L;
    long maxY = 40000000L;
    long resolution = 10;

    long seed = 0;
    long testNum = 50;
    long radius = setRadius;

    try {
      // fixed radius test
      PrintWriter writer = 
        new PrintWriter("fix_radius_" + radius + ".txt", "UTF-8");

      double diskArea = radius * radius * PI;

      for (resolution = 10; resolution <= 25; ++resolution) {
        long stripRangeNum = 
          (long)Math.ceil(radius / (maxY / ((double)(1L << resolution))));
        double tileXLen = maxX / ((double)(1L << resolution));
        double tileYLen = maxY / ((double)(1L << resolution));
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
        testFixedRadius(zgrp, radius, testNum, seed, zRes, zArea);
        double zMaxArea = getMax(zArea) * tileXLen * tileYLen;

        ArrayList<Long> mRes = new ArrayList<Long> ();
        ArrayList<Long> mArea = new ArrayList<Long> ();
        testFixedRadius(mgrp, radius, testNum, seed, mRes, mArea);
        double mMaxArea = getMax(mArea) * tileXLen * tileYLen;

        //ArrayList<Long> sRes = new ArrayList<Long> ();
        //ArrayList<Long> sArea = new ArrayList<Long> ();
        //testFixedRadius(sgrp, radius, testNum, seed, sRes, sArea);

        //write res to file
        for (int i = 0 ; i < testNum; ++i) {
          writer.println(resolution + ", " + radius + ", " 
              + zRes.get(i) + ", " + mRes.get(i) + ", " 
              + stripRangeNum + ", "
              + (zMaxArea / diskArea) + ", " 
              + (mMaxArea / diskArea) + ", " 
              + (mMaxArea / diskArea)); 
        }
      }

      writer.close();

      // fixed radius test

      resolution = setResolution;
      writer = new PrintWriter("fix_resolution_" 
          + resolution + ".txt", "UTF-8");

      long step = 200;
      long maxRadius = 10000;
      for (radius = step; radius <= maxRadius; radius += step) {
        long stripRangeNum = 
          (long) Math.ceil(radius / (maxY / ((double)(1L << resolution))));
        double tileXLen = maxX / ((double)(1L << resolution));
        double tileYLen = maxY / ((double)(1L << resolution));
        diskArea = radius * radius * PI;
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
        testFixedRadius(zgrp, radius, testNum, seed, zRes, zArea);
        double zMaxArea = getMax(zArea) * tileXLen * tileYLen;

        ArrayList<Long> mRes = new ArrayList<Long> ();
        ArrayList<Long> mArea = new ArrayList<Long> ();
        testFixedRadius(mgrp, radius, testNum, seed, mRes, mArea);
        double mMaxArea = getMax(mArea) * tileXLen * tileYLen;

        //ArrayList<Long> sRes = new ArrayList<Long> ();
        //ArrayList<Long> sArea = new ArrayList<Long> ();
        //testFixedRadius(sgrp, radius, testNum, seed, sRes, sArea);

        //write res to file
        for (int i = 0 ; i < testNum; ++i) {
          writer.println(resolution + ", " + radius + ", "
              + zRes.get(i) + ", " + mRes.get(i) + ", " 
              + stripRangeNum + ", "
              + (zMaxArea / diskArea) + ", " 
              + (mMaxArea / diskArea) + ", " 
              + (mMaxArea / diskArea)); 
        }
      }

      writer.close();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

}
