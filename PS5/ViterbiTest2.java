import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/* @author lukas dunford, jason peng
    nov 6 2023
    Make a HMM/Viterbi Part of Speech tagger for PS-5
 */

public class ViterbiTest2 {
    private Map<String, Map<String, Double>> observations;
    private Map<String, Map<String, Double>> transitions;

    public ViterbiTest2(String type) throws Exception {
        this.observations = new HashMap<>();
        this.transitions = new HashMap<>();

        trainModel("C:\\texts\\brown-train-sentences.txt", "C:\\texts\\brown-train-tags.txt");

        if ("HardCode".equals(type)) {
            String[] sentenceArray = {"you", "and", "your", "mother", "are", "found", "there", "."};
            System.out.println(Arrays.toString(sentenceArray));
            actualViterbi(sentenceArray);
        } else if ("Console".equals(type)) {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] words = input.split(" ");
            actualViterbi(words);
            scanner.close();
        } else if ("File".equals(type)) {
            trainModel("C:\\texts\\simple-test-sentences.txt", "C:\\texts\\simple-test-tags.txt");
            fileTester("C:\\texts\\brown-test-sentences.txt", "C:\\texts\\brown-test-tags.txt");        } else {
            throw new IllegalArgumentException("Invalid type specified.");
        }
    }

    private void trainModel(String sentencesFilePath, String tagsFilePath) throws IOException {
        Map<String, Map<String, Double>> observationCounts = new HashMap<>();
        Map<String, Map<String, Double>> transitionCounts = new HashMap<>();
        Map<String, Double> startTagCounts = new HashMap<>();
        double totalStartTags = 0.0;

        try (BufferedReader sentenceReader = new BufferedReader(new FileReader(sentencesFilePath));
             BufferedReader tagReader = new BufferedReader(new FileReader(tagsFilePath))) {
            String sentenceLine, tagLine;

            while ((sentenceLine = sentenceReader.readLine()) != null && (tagLine = tagReader.readLine()) != null) {
                String[] words = sentenceLine.trim().split("\\s+");
                String[] tags = tagLine.trim().split("\\s+");

                if (words.length != tags.length) {
                    throw new IllegalArgumentException("The number of words and POS tags do not match: " + sentenceLine + " / " + tagLine);
                }

                String previousTag = "#";
                totalStartTags++;
                startTagCounts.put(tags[0], startTagCounts.getOrDefault(tags[0], 0.0) + 1);

                for (int i = 0; i < words.length; i++) {
                    String word = words[i].toLowerCase();
                    String tag = tags[i];

                    // Update observation counts
                    observationCounts.putIfAbsent(word, new HashMap<>());
                    Map<String, Double> wordTagCounts = observationCounts.get(word);
                    wordTagCounts.put(tag, wordTagCounts.getOrDefault(tag, 0.0) + 1);

                    // Update transition counts
                    transitionCounts.putIfAbsent(previousTag, new HashMap<>());
                    Map<String, Double> tagTransitionCounts = transitionCounts.get(previousTag);
                    tagTransitionCounts.put(tag, tagTransitionCounts.getOrDefault(tag, 0.0) + 1);

                    previousTag = tag;
                }

                // Update transition to end tag
                transitionCounts.putIfAbsent(previousTag, new HashMap<>());
                Map<String, Double> endTagTransitionCounts = transitionCounts.get(previousTag);
                endTagTransitionCounts.put("#", endTagTransitionCounts.getOrDefault("#", 0.0) + 1);
            }
        }

        // Convert counts to probabilities
        for (String tag : transitionCounts.keySet()) {
            double totalTransitions = 0.0;
            for (Double count : transitionCounts.get(tag).values()) {
                totalTransitions += count;
            }
            for (String nextTag : transitionCounts.get(tag).keySet()) {
                transitions.putIfAbsent(tag, new HashMap<>());
                double count = transitionCounts.get(tag).get(nextTag);
                double probability = Math.log(count / totalTransitions);
                transitions.get(tag).put(nextTag, probability);
            }
        }

        for (String word : observationCounts.keySet()) {
            double totalObservations = 0.0;
            for (Double count : observationCounts.get(word).values()) {
                totalObservations += count;
            }
            for (String tag : observationCounts.get(word).keySet()) {
                observations.putIfAbsent(word, new HashMap<>());
                double count = observationCounts.get(word).get(tag);
                double probability = Math.log(count / totalObservations);
                observations.get(word).put(tag, probability);
            }
        }

        for (String tag : startTagCounts.keySet()) {
            transitions.putIfAbsent("#", new HashMap<>());
            double count = startTagCounts.get(tag);
            double probability = Math.log(count / totalStartTags);
            transitions.get("#").put(tag, probability);
        }

        // Add suffix observation probabilities
        addSuffixObservations(observationCounts);
    }

    private void addSuffixObservations(Map<String, Map<String, Double>> observationCounts) {
        // Define a set of common suffixes
        String[] suffixes = {"ed", "ing", "ion", "er", "est", "ly", "ity", "ty", "ment", "ness", "ship", "s", "es"};

        // Normalize suffix observation counts to probabilities
        Map<String, Map<String, Double>> suffixTagCounts = new HashMap<>();
        for (String word : observationCounts.keySet()) {
            for (String suffix : suffixes) {
                if (word.endsWith(suffix)) {
                    suffixTagCounts.putIfAbsent(suffix, new HashMap<>());
                    Map<String, Double> tagCounts = suffixTagCounts.get(suffix);
                    observationCounts.get(word).forEach((tag, count) -> {
                        tagCounts.put(tag, tagCounts.getOrDefault(tag, 0.0) + count);
                    });
                }
            }
        }

        // Normalize suffix observation counts to probabilities
        for (String suffix : suffixTagCounts.keySet()) {
            double totalSuffixObservations = suffixTagCounts.get(suffix).values().stream().mapToDouble(f -> f).sum();
            for (String tag : suffixTagCounts.get(suffix).keySet()) {
                double count = suffixTagCounts.get(suffix).get(tag);
                double probability = Math.log(count / totalSuffixObservations);
                // Ensure the map for the suffix is initialized before putting the probability
                observations.putIfAbsent(suffix, new HashMap<>());
                observations.get(suffix).put(tag, probability);
            }
        }
    }



    // this is the actual viterbi function, it returns the backtracked list of the best-selected route.
    public List<String> actualViterbi(String[] sentence) {
        List<Map<String, Map<String, Double>>> eachMovement = new ArrayList<>();
        Set<String> currentStates;
        Map<String, Double> currentScores;
        Set<String> nextStates;
        Map<String, Double> nextScores;

        currentStates = new HashSet<>();
        currentScores = new HashMap<>();

        currentStates.add("#");
        currentScores.put("#", 0.0);

        for (int i = 0; i < sentence.length; i++) {
            nextStates = new HashSet<>();
            nextScores = new HashMap<>();
            for (String currentState : currentStates) {
                for (String nextState : transitions.get(currentState).keySet()) {
                    nextStates.add(nextState);
                    double nextScore = currentScores.get(currentState) + transitions.get(currentState).get(nextState);

                    String word = sentence[i].toLowerCase();
                    if (observations.containsKey(word) && observations.get(word).containsKey(nextState)) {
                        nextScore += observations.get(word).get(nextState);
                    } else {
                        // Check for known suffixes in the word if the word itself is unknown
                        String knownSuffix = findKnownSuffix(word);
                        if (knownSuffix != null && observations.containsKey(knownSuffix) && observations.get(knownSuffix).containsKey(nextState)) {
                            nextScore += observations.get(knownSuffix).get(nextState);
                        } else {
                            // Apply a penalty for completely unknown words
                            nextScore += -100; // Use the parameterized penalty
                        }
                    }

                    if (!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                        nextScores.put(nextState, nextScore);

                        if (eachMovement.size() <= i) {
                            Map<String, Double> current = new HashMap<>();
                            current.put(currentState, nextScore);
                            Map<String, Map<String, Double>> next = new HashMap<>();
                            next.put(nextState, current);
                            eachMovement.add(i, next);
                        } else {
                            Map<String, Double> current = new HashMap<>();
                            current.put(currentState, nextScore);
                            eachMovement.get(i).put(nextState, current);
                        }
                    }
                }
            }
            currentStates = nextStates;
            currentScores = nextScores;
        }

        return backTrack(eachMovement);
    }

    private String findKnownSuffix(String word) {
        String[] suffixes = {"ed", "ing", "ion", "er", "est", "ly", "ity", "ty", "ment", "ness", "ship", "s", "es"};
        for (String suffix : suffixes) {
            if (word.endsWith(suffix) && observations.containsKey(suffix)) {
                return suffix;
            }
        }
        return null;
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
                }
            }
        }
        double ratio = totalCorrect/total;
        System.out.println("Total: " + total + " Total correct: " + totalCorrect + " Percent correct: " + ratio);
    }


    public static void main(String[] args) throws Exception{
        ViterbiTest2 test = new ViterbiTest2("File");

    }
}
