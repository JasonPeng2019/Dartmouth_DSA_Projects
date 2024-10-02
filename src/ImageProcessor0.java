import java.awt.image.BufferedImage;

public class ImageProcessor0 {
    private BufferedImage image;

    public ImageProcessor0(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Applies a random color to a square region centered at (x, y) with a given size.
     *
     * @param x        the x-coordinate of the center of the square.
     * @param y        the y-coordinate of the center of the square.
     * @param brushSize the size of the square.
     */
    public void randomColorBrush(int x, int y, int brushSize) {
        int halfSize = brushSize / 2;
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        int randomColor = (red << 16) | (green << 8) | blue;

        for (int i = -halfSize; i <= halfSize; i++) {
            for (int j = -halfSize; j <= halfSize; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX < 0 || newY < 0 || newX >= image.getWidth() || newY >= image.getHeight()) continue;
                image.setRGB(newX, newY, randomColor);
            }
        }
    }
}