public class TestMovingBlob {
    public static void main(String[] args) {
        MovingBlob stepper = new MovingBlob(0, 0);

        for (int i = 0; i < 50; i++) {
            stepper.step();
            System.out.println("Step " + (i + 1) + ": x = " + stepper.getX() + ", y = " + stepper.getY());
        }
    }
}
