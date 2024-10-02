public class actorClass2 implements Comparable<actorClass2>{
    public String actor;
    public double pathLength;

    public actorClass2(String actor, double pathLength){
        this.actor = actor;
        this.pathLength = pathLength;
    }

    public int compareTo(actorClass2 o) {
        if (pathLength < o.pathLength) {
            return -1;
        }
        if (pathLength == o.pathLength){
            return 0;
        }
        return 1;
    }

    public String getActor(){
        return this.actor;
    }
}
