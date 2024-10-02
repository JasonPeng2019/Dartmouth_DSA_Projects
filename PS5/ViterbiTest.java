import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/* @author lukas dunford, jason peng
    nov 6 2023
    Make a HMM/Viterbi Part of Speech tagger for PS-5
 */

public class ViterbiTest {
    Map<String, Map<String, Double>> observations;  //observations map
    Map<String, Map<String, Double>> transitions; // similar for transitions

    // this is the constructor. It basically does everything and calls all the other functions. depends on type sent to it,
    // it will run hardcode, console, or file compare
    public ViterbiTest(String type) throws Exception {
        this.observations = new HashMap<>();
        this.transitions = new HashMap<>();

        // send both to load files, this loads the files
        Map[] both = loadFiles("C:\\texts\\brown-train-sentences.txt", "C:\\texts\\brown-train-tags.txt");
        observations = both[0];
        transitions = both[1];

//        the following is the training stuff:
        Map<String, Integer> transTotals = new HashMap<>(); // collect totals across levels for transitions
        for(String currSt : transitions.keySet()) { // for each transition
            int total = 0;
            for(double i : transitions.get(currSt).values()){ // for each
                total += i;
            }
            transTotals.put(currSt, total);
        }
        for(String currSt : transitions.keySet()) {  // set the values in transitions to those divided by sum and logged
            int total = transTotals.get(currSt);
            for(String nextSt : transitions.get(currSt).keySet()){
                double temp = transitions.get(currSt).get(nextSt);
                double dividend = (double) transTotals.get(currSt);
                transitions.get(currSt).put(nextSt, Math.log(temp/dividend)); // Math.log of the count divided by the total
            }
            transTotals.put(currSt, total);
        }

        // get the totals for the observations across
        Map<String, Integer> tagTotals = new HashMap<>();
        for(Map<String, Double> m : observations.values()) { // maybe able to do this in the first loop?
            for (String pos : m.keySet()) {
                if (tagTotals.containsKey(pos)) { // if tag totals already has the part of speech
                    int tempCount = tagTotals.get(pos);
                    tagTotals.put(pos, tempCount + 1); // iterate up one
                } else {
                    tagTotals.put(pos, 1);
                }
            }
        }
        for(Map<String, Double> m : observations.values()) { // maybe able to do this in the first loop?
            for (String pos : m.keySet()) {
                double temp = m.get(pos);
                double dividend = tagTotals.get(pos);
                m.put(pos, Math.log(temp/dividend)); // Math.log of the count divided by the total
            }
        }

        // hardcode version
        if(type == "HardCode") {
            String[] sentenceArray = { // this is for doing different tests
//                    "your", "game", "is", "great", "."
//                "we", "should", "eat", "fifteen", "dogs", "having", "been", "made", "tired", "."
                    "you", "and", "your", "mother", "are", "found", "there", "."
            };
            System.out.println(sentenceArray);
            actualViterbi(sentenceArray);

            // console mode
        } else if (type == "Console") {
            Scanner s = new Scanner(System.in);
            String in = s.nextLine();
            String[] words = in.split(" ");
            actualViterbi(words); // send to viterbi
            s.close();

            // file mode
        } else if (type == "File") {
            String sentPathName = "C:\\texts\\simple-test-sentences.txt"; // change to brown or whatever
            String tagsPathName = "C:\\texts\\simple-test-tags.txt";
            fileTester(sentPathName, tagsPathName); // send to file tester
        }
    }
    // this is the actual viterbi function, it returns the backtracked list of the best-selected route.
    public List<String> actualViterbi(String[] sentence) {
        List<Map<String, Map<String, Double>>> eachMovement = new ArrayList<>(); // the crazy nested loop that I use
        //as the returned thing
        Set<String> currStates; // filling it out
        Map<String, Double> currScores;
        Set<String> nextStates;
        Map<String, Double> nextScores;

        currStates = new HashSet<>(); // new set and map for current values
        currScores = new HashMap<>();

        currStates.add("#"); // start it out
        currScores.put("#", 0.0);

        for (int i = 0; i < sentence.length; i++) { // for each piece of sentence
            nextStates = new HashSet<>(); // make new transitions for each movement
            nextScores = new HashMap<>();
            for (String currState : currStates) { // go into each current state
                for (String nextState : transitions.get(currState).keySet()) { // and go into each next state for each current state
                    nextStates.add(nextState); // add it to the set
                    double nextScore; // for access, declare here

                    // if not a new word, and observations has the next state in its map
                    if (observations.containsKey(sentence[i]) && observations.get(sentence[i]).containsKey(nextState)) {
                        nextScore = currScores.get(currState) +
                                transitions.get(currState).get(nextState) +
                                observations.get(sentence[i]).get(nextState);
                    } else { // if word is new, give penalty added to the curr score and transition weight
                        nextScore = currScores.get(currState) +
                                transitions.get(currState).get(nextState) - 100; // -100 new word penalty
                    }
                    // if next score is new, or lower than current value of it
                    if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                        nextScores.put(nextState, nextScore); // change or add its value

                        if (eachMovement.size() <= i) { // if the size of the list of each movement is less than current i
                            // i.e., if the list need to be added to.
                            Map<String, Double> cur = new HashMap<>(); // boiler plate fill ins:
                            cur.put(currState, nextScore);
                            Map<String, Map<String, Double>> next = new HashMap<>();
                            next.put(nextState, cur);
                            eachMovement.add(i, next);
                            // otherwise: just add a new map with the correct state and score references
                        } else {
                            Map<String, Double> cur = new HashMap<>();
                            cur.put(currState, nextScore);
                            eachMovement.get(i).put(nextState, cur);
                        }
                    }
                }
            }
            currStates = nextStates; // iterate
            currScores = nextScores;
        }
        List<String> theAns = backTrack(eachMovement); // send to backtrack
        return theAns;
    }

    // the backtrack method, takes the nested map and gives the optimal back track list
    public List<String> backTrack(List<Map<String, Map<String, Double>>> eachMovement){
        List<String> ans = new ArrayList<>();
        for (int i = 0; i < eachMovement.size(); i++) {
            String maxString = "";
            Double maxNum = -99999.9; // arbitrarily low
            for(String next : eachMovement.get(i).keySet()) { // nested loops for accessing the numbers inside
                for(String curr : eachMovement.get(i).get(next).keySet()){
                    Double tempNum = eachMovement.get(i).get(next).get(curr);
                    if (maxNum < tempNum){ // if lower, update
                        maxString = next;
                        maxNum = tempNum;
                    }
                }
            }
            ans.add(maxString); // add the lowest
        }
        return ans;
    }

    // file tester method, just for the file mode of main constructor.
    public void fileTester(String sentsPath, String tagsPath) throws Exception{

        BufferedReader run = new BufferedReader(new FileReader(sentsPath));
        BufferedReader check = new BufferedReader(new FileReader(tagsPath));
        String sentence, checker;
        double total = 0;
        double totalCorrect = 0;
        while((sentence = run.readLine()) != null && (checker = check.readLine()) != null){ // read corresponding lines
            List<String> viterbi = actualViterbi(sentence.split(" ")); // set them to lists
            String[] checking = checker.split(" ");

            for(int i =0; i< checking.length; i++){ // iterate and compare
                total++;
                if(viterbi.get(i).equals(checking[i])){
                    totalCorrect++;
                } else {
                    System.out.println(viterbi.get(i)+ " " + checking[i]);
                }
            }
        }
        double ratio = totalCorrect/total;
        System.out.println("Total: " + total + " Total correct: " + totalCorrect + " Percent correct: " + ratio);
    }

    public static Map[] loadFiles(String sentencePath, String partPath) throws Exception {
        Map<String, Map<String, Double>> obs = new HashMap<>(); // will become global observations
        Map<String, Map<String, Double>> trans = new HashMap<>(); // will become global transitions
        BufferedReader inSentence = new BufferedReader(new FileReader(sentencePath));
        BufferedReader inPOS = new BufferedReader(new FileReader(partPath));
        String sentenceLine, POSLine;
        // read each line of both files
        while ((sentenceLine = inSentence.readLine()) != null && (POSLine = inPOS.readLine()) != null) {
            String[] words = sentenceLine.split(" ");
            String[] pos = POSLine.split(" ");

            String prevTag = "#";

            for (int i = 0; i < words.length; i++) {  // for each word and complementary POS tag in the line
                String tempWord = words[i].toLowerCase();
                String tempPOS = pos[i];

                // the following is filling observations
                if (!obs.containsKey(tempWord)) { // if the map doesn't have word,
                    obs.put(tempWord, new HashMap<>()); // then add it with empty map
                    obs.get(tempWord).put(tempPOS, 1.0); // add the tag to the nested map and set val to 0
                } else if (!obs.get(tempWord).containsKey(tempPOS)) { // else if it does, but doesn't contain current POS tag
                    obs.get(tempWord).put(tempPOS, 1.0); // set new POS
                } else { // else increment the POS tag
                    double tCount = obs.get(tempWord).get(tempPOS); // get current count
                    obs.get(tempWord).put(tempPOS, tCount + 1.0); // then increment the correct POS tag
                }
                // the following is for transitions
                if (!trans.containsKey(prevTag)) { // if the map doesn't have word,
                    trans.put(prevTag, new HashMap<>()); // then add it with empty map
                    trans.get(prevTag).put(tempPOS, 1.0); // add the tag to the nested map and set val to 0
                } else if (!trans.get(prevTag).containsKey(tempPOS)) { // else if it does, but doesn't contain current POS tag
                    trans.get(prevTag).put(tempPOS, 1.0); // set new POS
                } else if (i == words.length-1) {
                    trans.put(tempWord, new HashMap<>());
                } else { // else increment the POS tag
                    double tCount = trans.get(prevTag).get(tempPOS); // get current count
                    trans.get(prevTag).put(tempPOS, tCount + 1.0); // then increment the correct POS tag
                }
                prevTag = tempPOS; // set prev tag to current for next loop
            }
        }
        inPOS.close();
        inSentence.close();

        Map[] ans = new Map[2]; // just for ease of returning one thing
        ans[0] = obs;
        ans[1] = trans;
        return ans; // return the array
    }

    public static void main(String[] args) throws Exception{
        ViterbiTest test = new ViterbiTest("File");

    }
}
