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

  @Override
  public LinkedList<Range> getScanRanges(GeoRequest gr) {
    return internalGetScanRanges(gr, 0, 0, 
        this.maxXTileLen, this.maxYTileLen, 0, 0, (int)this.maxResolution);
  }

  private LinkedList<Range> internalGetScanRanges(GeoRequest gr,
      int xTile, int yTile, double xTileLen, double yTileLen,
      long ph, int pd, int curBit) {
    // d1 d0 x y
    int mask = 1 << curBit;
    int xBit = (xTile & mask) >> curBit;
    int yBit = (yTile & mask) >> curBit;
    int index = (pd << 2) | (xBit << 1) | yBit;
    long ch = (MooreCurve.codeMap[index] << (curBit << 1)) | ph;

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
      res.add(new Range(ch, ch + (1 << (curBit << 1))));
      return res;
    }

    --curBit;
    xTileLen /= 2;
    yTileLen /= 2;

    int cd = MooreCurve.orientationMap[index];
    int xUnit, yUnit;
    LinkedList<Range> res = new LinkedList<Range> ();
    LinkedList<Range> tmp = null;

    index = pd << 2;
    for (int i = 0 ; i < 4; ++i) {
      xUnit = MooreCurve.childXMap[index] << curBit;
      yUnit = MooreCurve.childYMap[index] << curBit;
      tmp = internalGetScanRanges(gr, xTile | xUnit, yTile | yUnit,
                                  xTileLen, yTileLen, ch, cd, curBit);
      if (null != tmp)
        res.addAll(tmp);
      ++index;
    }

    return res;
  }

  public static void main(String args[]) {
    int r = 5;
    int len = 1 << (r << 1);
    GeoContext gc = new GeoContext(5, len, len);
    GeoEncoding ge = new MooreGeoEncoding(gc);
    BraidQuadTreeGeoRequestParser grp = 
      new BraidQuadTreeGeoRequestParser(ge);

  }
}
