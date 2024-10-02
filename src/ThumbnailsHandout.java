import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ThumbnailsHandout extends DrawingGUI {
    private static final int trows = 3, tcols = 3;
    private static final int thumbWidth = 200, thumbHeight = 150;
    private ArrayList<BufferedImage> thumbs = new ArrayList<>();

    public ThumbnailsHandout(ArrayList<BufferedImage> images) {
        super("Thumbnails Handout", thumbWidth * tcols, thumbHeight * trows); // Init window with title and dimensions
        for (int i = 0; i < trows * tcols; i++) { // Corrected the for loop
            thumbs.add(thumbnailify(images.get(i)));
        }
    }

    private static BufferedImage thumbnailify(BufferedImage image) {
        BufferedImage result = new BufferedImage(thumbWidth, thumbHeight, image.getType());

        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < thumbWidth; x++) {
            for (int y = 0; y < thumbHeight; y++) {
                int pX = x * width / thumbWidth; // Pixel coordinates in the original image
                int pY = y * height / thumbHeight;
                result.setRGB(x, y, image.getRGB(pX, pY));
            }
        }
        return result;
    }

    public void draw(Graphics g) {
        for (int i = 0; i < trows; i++) { // Adjusted the loop limit to < trows
            for (int j = 0; j < tcols; j++) { // Adjusted the loop limit to < tcols
                g.drawImage(thumbs.get(i * tcols + j), j * thumbWidth, i * thumbHeight, null);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<BufferedImage> images = new ArrayList<>();
                for (int i = 0; i < trows; i++) {
                    for (int j = 0; j < tcols; j++) {
                        images.add(loadImage("C:\\Users\\14005\\Downloads\\dart\\dart\\dart" + (i * tcols + j) + ".jpg")); // Assuming loadImage is a method in DrawingGUI or implemented elsewhere
                    }
                }
                new ThumbnailsHandout(images);
            }
        });
    }
}
