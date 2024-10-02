public class actorClass implements Comparable<actorClass>{

    public String actor;
    public int outDegree;
    public actorClass (String actor, int outDegree){
        this.actor = actor;
        this.outDegree = outDegree;
    }

    public int compareTo(actorClass o) {
        if (this.outDegree < o.outDegree) {
            return -1;
        }
        if (this.outDegree == o.outDegree) {
            return 0;
        }
        return 1;
    }

    public String getActor(){
        return actor;
    }
}
