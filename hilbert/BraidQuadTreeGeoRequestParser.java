import java.util.LinkedList;
import java.util.ArrayList;

/*
 * implement a simple linked list that can efficient concatenate two lists.
 */
public class BraidQuadTreeGeoRequestParser extends GeoRequestParser{

  public static final long MAX_RESOLUTION = 28L;
  private static final long X_LEFT_SHIFT = MAX_RESOLUTION;
  private static final long R_LEFT_SHIFT = MAX_RESOLUTION << 1;
  private static final long COOR_MASK = (1L << MAX_RESOLUTION) - 1;

  private long maxResolution;
  private double maxXTileLen;
  private double maxYTileLen;
  private double minXTileLen;
  private double minYTileLen;

  // TODO: QuadTree only applies to a subset of GeoEncoding
  // algorithms. StripGeoEncoding is a counter example.
  // Apply checks during construction.
  public BraidQuadTreeGeoRequestParser(GeoEncoding ge) {
    super(ge);
    this.maxResolution = this.gc.getMaxResolution();
    if (this.maxResolution > MAX_RESOLUTION) {
      throw new IllegalStateException("Encoding resolution " 
          + this.maxResolution + " is larger than MAX_RESOLUTION "
          + MAX_RESOLUTION);
    }
    this.maxXTileLen = gc.getMaxX();
    this.maxYTileLen = gc.getMaxY();
    this.minXTileLen = 
      this.maxXTileLen / (1L << this.maxResolution);
    this.minYTileLen = 
      this.maxYTileLen / (1L << this.maxResolution);
  }

  public double getMinXTileLen() {
    return minXTileLen;
  }

  public double getMinYTileLen() {
    return minYTileLen;
  }

  public LinkedList<Range> mergeRanges(LinkedList<Range> head, 
                                       LinkedList<Range> tail) {
    // no empty list is allowed
    if (null == tail) {
      return head;
    } else {
      if (null == head) {
        return tail;
      } else {
        if (head.getLast().isConsecutive(tail.getFirst())) {
          head.getLast().spanTo(tail.getFirst());
          tail.remove(0);
        }
        head.addAll(tail);
        return head;
      }
    }
  }

  @Override
  public LinkedList<Range> getScanRanges(GeoRequest gr) {
    return internalGetScanRanges(gr, 0, 0, 
        this.maxXTileLen, this.maxYTileLen, 0, 0, (int)this.maxResolution);
  }

  private LinkedList<Range> internalGetScanRanges(GeoRequest gr,
      int xTile, int yTile, double xTileLen, double yTileLen,
      long ph, int pd, int curBit) {
    int index = 0;
    long ch;
    // d1 d0 x y
    if (curBit >= this.maxResolution) {
      ch = 0;
    } else {
      int mask = 1 << curBit;
      int xBit = (xTile & mask) >> curBit;
      int yBit = (yTile & mask) >> curBit;
      index = (pd << 2) | (xBit << 1) | yBit;
      ch = (MooreCurve.codeMap[index] << (curBit << 1)) | ph;
    }

    int isCovered = gr.isCovered(xTile * this.minXTileLen, 
                                 yTile * this.minYTileLen, 
                                 xTileLen, yTileLen);
    if (GeoRequest.NO_COVER == isCovered) {
      // not covered
      return null;
    } else if (GeoRequest.FULL_COVER == isCovered
               || curBit <= 0
               || curBit <= this.maxResolution - gr.getResolution()) {
      // full cover
      LinkedList<Range> res = new LinkedList<Range> ();
      res.add(new Range(ch, ch + (1L << (curBit << 1)) - 1));
      return res;
    }

    --curBit;
    xTileLen /= 2;
    yTileLen /= 2;

    int cd = MooreCurve.orientationMap[index];
    int xUnit, yUnit;
    LinkedList<Range> res = null;
    LinkedList<Range> tmp = null;

    index = cd << 2;
    for (int i = 0 ; i < 4; ++i) {
      xUnit = MooreCurve.childXMap[index] << curBit;
      yUnit = MooreCurve.childYMap[index] << curBit;
      tmp = internalGetScanRanges(gr, xTile | xUnit, yTile | yUnit,
                                  xTileLen, yTileLen, ch, cd, curBit);
      res = mergeRanges(res, tmp);
      ++index;
    }

    return res;
  }

  public static void main(String args[]) {
    int r = Integer.parseInt(args[0]);
    int len = 1 << (r << 1);
    GeoContext gc = new GeoContext(r, len, len);
    GeoEncoding ge = new MooreGeoEncoding(gc);
    BraidQuadTreeGeoRequestParser grp = 
      new BraidQuadTreeGeoRequestParser(ge);
  
    Rectangle rect = new Rectangle(0.5, 0.5, len - 0.5, len - 0.5);

    RectangleGeoRequest rgr = new RectangleGeoRequest(rect, r);
    RectangleScanNumberTest.printRanges(grp.getScanRanges(rgr));
    /*
    for (int i = 0 ; i < (1 << r); ++i) {
      for (int j = 0 ; j < (1 << r); ++j) {
        double x = i * grp.getMinXTileLen();
        double y = j * grp.getMinYTileLen();
        Rectangle rect = new Rectangle(x + 0.1, y + 0.1, 0.1, 0.1);
        RectangleGeoRequest rgr = new RectangleGeoRequest(rect, r);
        Range range = grp.getScanRanges(rgr).get(0);
        System.out.print("(" + range.getStart() + ",\t" 
            + range.getEnd()+ ")\t");
      }
      System.out.println("\n");
    }
    */
  }
}
