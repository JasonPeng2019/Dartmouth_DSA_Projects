import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Pollock extends DrawingGUI {
    private static final int numBlobs = 20000;
    private static final int numToMove = 5000;

    private BufferedImage result;
    private ArrayList<WanderingPixel> pixels;

    public Pollock(int width, int height) {
        super("Pollock Inspired Art", width, height);

        result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        pixels = new ArrayList<WanderingPixel>();
        for (int i=0; i<numBlobs; i++) {
            int x = (int)(width*Math.random());
            int y = (int)(height*Math.random());
            Color randomColor = new Color((int)(Math.random() * 16777215));
            pixels.add(new WanderingPixel(x, y, randomColor));
        }

        startTimer();
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(result, 0, 0, null);
        for (WanderingPixel pixel : pixels) {
            pixel.draw(g);
        }
    }

    @Override
    public void handleTimer() {
        for (int b = 0; b < numToMove; b++) {
            WanderingPixel pixel = pixels.get((int)(Math.random()*pixels.size()));
            int x = (int)pixel.getX(), y = (int)pixel.getY();
            if (x>=0 && x<width && y>=0 && y<height) {
                result.setRGB(x, y, pixel.getColor().getRGB());
            }
            pixel.step();
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Pollock(800, 600);
            }
        });
    }
}

class WanderingPixel extends MovingBlob {
    private Color color;

    public WanderingPixel(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        super.draw(g);
    }
}