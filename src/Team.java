public class Team {
    private String mascotName;
    private int score;

    public Team(String mascotName){
        this.mascotName = mascotName;
        this.score = 0;
        // Define a public constructor that takes one parameter
        // and uses its value to initialize the mascot name,
        // while initializing the score to 0
    }

    public String getMascot(){
        return mascotName;
    }
    public int getScore(){
        return score;
    }

    public void score(){
        this.score +=2;
    }

    public static void main(String[] args) {
        Team jags = new Team("jaguar");
        Team lions = new Team("Lions");
        jags.score();
        lions.score();
        lions.score();

        if (jags.getScore() > lions.getScore()){
            System.out.print("jags");
        }
        else {
            System.out.print("lions");
        }
       // if (lions.getScore() > jags.getScore()){
         //   System.out.print("lions");
       // }

    }
}
