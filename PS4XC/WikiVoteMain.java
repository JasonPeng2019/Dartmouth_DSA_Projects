import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class WikiVoteMain {
    public static void main(String[] args) {
        try {
            AdjacencyMapGraph<String, Integer> map = wikiVoteParser.buildMap(
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\cs10notes\\PS4XC\\Wiki-Vote.txt");
            System.out.println("Path Finder: Press w <target>, or press q to quit, or press u <center>");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("q")) {
                    break;
                }
                else if (input.startsWith("w ")) {
                    String target = input.substring(2);
                    List<String> path = wikiVoteParser.getRandomPath(map, target);
                    System.out.println(path);
                    System.out.println("path contains " + path.size() + " steps on this randomwalk");
                } else if (input.startsWith("u ")){
                    String target = input.substring(2);
                    wikiVoteParser.center = target;
                    System.out.println("center assigned");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
