public class MovingBlob extends Blob {

    private int stepsBwChange;
    private int stepsTaken;

    public MovingBlob(double x, double y) {
        super(x, y);
        this.stepsBwChange = 4 + (int)(Math.random() * 6);
        this.stepsTaken = this.stepsBwChange;
    }

    @Override
    public void step() {
        stepsTaken++;

        if(stepsTaken >= stepsBwChange) {
            stepsTaken = 0;

            dx = 2 * (Math.random() - 0.5);
            dy = 2 * (Math.random() - 0.5);

            stepsBwChange = 4 + (int)(Math.random() * 6);
        }

        x += dx;
        y += dy;
    }
}

