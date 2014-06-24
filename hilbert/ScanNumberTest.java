import java.util.LinkedList;

public class ScanNumberTest {

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

  public static void main(String args[]) {

    GeoContext gc = new GeoContext(5, 100, 100);
    GeoEncoding ge = new MooreGeoEncoding(gc);

    GeoRequestParser grp;

    grp = new QuadTreeGeoRequestParser(ge);

    GeoRequest gr = new DiskGeoRequest(50, 50, 30);

    printRanges(grp.getScanRanges(gr));
      
  }

}
