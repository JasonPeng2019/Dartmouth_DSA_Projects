import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessingGUI0 extends DrawingGUI {
    private ImageProcessor0 proc;
    private boolean brushDown = false;

    public ImageProcessingGUI0(ImageProcessor0 proc) {
        super("Image processing", proc.getImage().getWidth(), proc.getImage().getHeight());
        this.proc = proc;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(proc.getImage(), 0, 0, null);
    }

    @Override
    public void handleKeyPress(char op) {
        if (op == 'b') {
            brushDown = true;
        } else if (op == 'u') {
            brushDown = false;
        } else if (op == 's') {
            saveImage(proc.getImage(), "pictures/snapshot.png", "png");
        } else {
            System.out.println("Unknown operation");
        }
        repaint();
    }

    @Override
    public void handleMouseMotion(int x, int y) {
        if (brushDown) {
            proc.randomColorBrush(x, y, 10);
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BufferedImage baker = loadImage("C:/Users/14005/OneDrive/Pictures/baker.jpg");
                new ImageProcessingGUI0(new ImageProcessor0(baker));
            }
        });
    }
}