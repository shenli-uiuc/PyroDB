/**
 *  a scan range
 */
public class Range {
  private long start;
  private long end;

  public Range(long start, long end) {
    this.start = start;
    this.end = end;
  }

  public long getStart() {
    return this.start;
  }

  public long getEnd() {
    return this.end;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public boolean isConsecutive(Range r) {
    return this.end + 1 == r.start;
  }

  /*
   * span the end of the current range to the 
   * end of the given range
   */
  public void spanTo(Range r) {
    this.end = r.end;
  }

}
