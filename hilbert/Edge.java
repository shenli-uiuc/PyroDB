/**
 *  a helper class to hold the information for an edge
 */
public class Edge {
  public Point a;
  public Point b;

  public double xx = 0;
  public double yy = 0;

  public Edge(Point a, Point b) {
    reset(a, b);
  }

  public void reset(Point a, Point b) {
    this.a = a;
    this.b = b;
    xx = b.x - a.x;
    yy = b.y - a.y;
  }

  /**
   * check whether point c is on the left or right side of edge ab
   * If the point falls on the segment, it is considered to be on the
   * right (out side of a rectangular, if the edges follow 
   * the counter-clockwise order);
   */
  public boolean isOnRight(Point c) {
    return xx * (c.y - a.y) - yy * (c.x - a.x) <= 0;
  }

  public String toString() {
    return a.toString() + " --> " + b.toString();
  }
}
