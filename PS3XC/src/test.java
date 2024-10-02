public class test {
    public static void main(String[] args) {
        char chara = 'r';
        String num = Integer.toBinaryString(chara);
        System.out.println(num);

        chara = 'r';
        num = Integer.toBinaryString((int)chara);
        System.out.println(num);

        int numX = 9;
        num = Integer.toBinaryString(numX);
        System.out.println(num);
        numX = 9999;
        num = Integer.toBinaryString(numX);
        System.out.println(num);

        Long NumY = 99L;
        num = Long.toBinaryString(NumY);
        System.out.println(num);
    }
}
