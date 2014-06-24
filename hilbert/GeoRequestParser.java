import java.util.LinkedList;

/**
 *  parse GeoRequest into a series of scan ranges following the
 *  increasing order or their row key.
 */
public abstract class GeoRequestParser {

  //TODO: handle the required resolution in GeoRequest

  /**
   *  translate a GeoRequest into a list of Scan ranges
   */
  public abstract LinkedList<Range> getScanRanges(GeoRequest gr);
}
