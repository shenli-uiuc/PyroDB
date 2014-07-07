public class VarInteger{
  // priority;
  private int i;

  public VarInteger() {
    this(0);
  }

  public VarInteger(int i) {
    this.i = i;
  }

  public int inc(int step) {
    this.i += step;
  }

  public int inc() {
    ++this.i;
  }

  public int get() {
    return this.i;
  }
}
