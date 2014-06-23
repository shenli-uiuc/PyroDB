import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class SpaceFillingCurveDrawer{

  private static final int WIDTH = 1024;
  private static final int HEIGHT = 1024;

  public static void paintSpace(Graphics g, SpaceFillingCurve sfc, int r) {
    int maxX = 1 << r;
    int maxY = maxX;
    int step = WIDTH / maxX; 

    float hue = 0F;
    float sat = 0.8F;
    float bright = 0.9F;
    float hueStep = 1.0F / (maxX * maxY);

    long index = 0;
    int x, y;
    for (x = 0; x < maxX; ++x) {
      for (y = 0; y < maxY; ++y) {
        index = sfc.encode(x, y, r);
        hue = (index * hueStep);
        Color color = Color.getHSBColor(hue, sat, bright);
        g.setColor(color);
        g.fillRect(x * step, y * step, step, step);
      }
    }

    /*
    g.setColor(Color.BLACK);
    for (x = 0; x < maxX; ++x) {
      for (y = 0; y < maxY; ++y) {
        g.drawRect(x * step, y * step, step, step);
      }
    }
    */
  }


  public static void main(String [] args) {
    String curveName = args[0];
    int r = Integer.parseInt(args[1]);

    BufferedImage im = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    Graphics2D ig2 = im.createGraphics();

    SpaceFillingCurve sfc = null;
    if (curveName.matches("Moore")) {
      sfc = new Moore();
    } else if (curveName.matches("Zcurve")) {
      sfc = new Zcurve();
    } else {
      System.out.println("Unrecgonized curve");
      return;
    }

    paintSpace(ig2, sfc, r);

    try {
      ImageIO.write(im, "PNG", new File(curveName + "_" + r + ".png"));
    } catch (Exception ex) {
      System.out.println("Exception!");
    }
  }

}
