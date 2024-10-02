import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
// *** this question is much easier with an int[][], but for the sake of practice it is worth doing with
// an array list of linked lists
// 4.1
public class Connect4 {
    private ArrayList<LinkedList<Integer>> board;
    // '1' for player 1, '2' for player 2
// let index 0 of each LinkedList represent the bottom of the stack of pieces
    private int columns; // the number of columns in the board, also equal to board.size()
    private int rows;
    // the number of rows in the board, and the max size of any LinkedList in the board
// initializes the variables and connect4 board with the proper number of linkedlists
    public Connect4(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.board = new ArrayList<>();
// add linked lists to the board to represent the columns
        for(int i = 0; i < columns; i++) {
            this.board.add(new LinkedList<>());
        }
    }
    /* should insert the piece into the board, if it is possible
    return true if the move is made
    return false if the move is not possible
    assume that player is either 0 or 1, but do NOT assume column is valid
    */
    public boolean move(int column, int player) {
        if(column < 0 || column >= columns) return false;
        LinkedList<Integer> activeColumn = board.get(column);
        if(activeColumn.size() == rows) return false;
        activeColumn.add(player);
        return true;
    }
    /* should check if a player has won the game
    if player 1 wins, return 1
    if player 2 wins, return 2
    if no player wins, return 0
    (this is a very difficult method - do not forget to check for
    horizontal, vertical,
    and diagonal victories and watch out for index exceptions!)
    */
    public int checkForWinner() {
        int horizontalWinner = horizontalWinner();
        int verticalWinner = verticalWinner();
        int diagonalWinner = diagonalWinner();
        if(horizontalWinner != 0) return horizontalWinner;
        if(verticalWinner != 0) return verticalWinner;
        if(diagonalWinner != 0) return diagonalWinner;
        return 0;
    }
    // checks for winners in the horizontal direction
// strategy - traverse each row, looking for four in a rows
    private int horizontalWinner() {
        for(int r = 0; r < rows; r++) {
            int current = 0;
            int currentCount = 0;
            for(int j = 0; j < columns; j++) {
                if(board.get(j).size() <= r) {
                    current = 0;
                    currentCount = 0;
                    continue;
                }
                int next = board.get(j).get(r);
                if(next == current && current != 0) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
            }
        }
        return 0;
    }
    // checks for winners in the vertical direction
// strategy - traverse the columns looking for 4-in-a-rows
    private int verticalWinner() {
        for(int c = 0; c < columns; c++) {
            LinkedList<Integer> activeList = board.get(c);
            int current = 0;
            int currentCount = 0;
            for(int j = 0; j < activeList.size(); j++) {
                int next = activeList.get(j);
                if(next == current) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
            }
        }
        return 0;
    }
// checks for winners in the diagonal directions
// strategy - traverse the left side and bottom edge and work up-right or bottom-left, looking for 4-in-a-rows
    private int diagonalWinner() {
        for(int r = rows - 1; r >= 0; r--) { // left column down
            int step = 0;
            int current = 0;
            int currentCount = 0;
            while(r + step < rows && step < columns) {
                if(board.get(step).size() <= r + step) {
                    current = 0;
                    currentCount = 0;
                    step++;
                    continue;
                }
                int next = board.get(step).get(r + step);
                if(current == next && current != 0) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
                step++;
            }
            step = 0;
            current = 0;
            currentCount = 0;
            while(r - step >= 0 && step < columns) {
                if(board.get(step).size() <= r - step) {
                    current = 0;
                    currentCount = 0;
                    step++;
                    continue;
                }
                int next = board.get(step).get(r - step);
                if(current == next && current != 0) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
                step++;
            }
        }
        for(int c = 1; c < columns; c++) { // bottom right
            int step = 0;
            int current = 0;
            int currentCount = 0;
            while(c + step < columns && step < rows) {
                if(board.get(c + step).size() <= step) {
                    current = 0;
                    currentCount = 0;
                    step++;
                    continue;
                }
                int next = board.get(c + step).get(step);
                if(current == next && current != 0) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
                step++;
            }
            int inverseStep = rows - 1;
            step = 0;
            current = 0;
            currentCount = 0;
            while(c + step < columns && inverseStep >= 0) {
                if(board.get(c + step).size() <= inverseStep) {
                    current = 0;
                    currentCount = 0;
                    step++;
                    continue;
                }
                int next = board.get(c + step).get(inverseStep);
                if(current == next && current != 0) {
                    currentCount++;
                } else {
                    current = next;
                    currentCount = 1;
                }
                if(currentCount == 4) {
                    return current;
                }
                step++;
                inverseStep--;
            }
        }
        return 0;
    }
    /* should print out the board in a grid formation
    with blank spaces filled in by 0s and the linked lists running from the bottom
    up
    */
    public void printBoard() {
        for(int r = rows - 1; r >= 0; r--) {
            for(int c = 0; c < columns; c++) {
                if(board.get(c).size() <= r) {
                    System.out.print("0 ");
                } else {
                    System.out.print(board.get(c).get(r) + " ");
                }
            }
            System.out.println();
        }
    }
    // added a basic console UI if you would like to test your own methods
    public static void main(String[] args) {
        Connect4 c4 = new Connect4(7, 6);
        int player = 1;
        Scanner scanner = new Scanner(System.in);
        while(c4.checkForWinner() == 0) {
            c4.printBoard();
            System.out.println("Player " + player + " move.");
            System.out.print("Column: ");
            int column = scanner.nextInt();
            if(c4.move(column, player)) {
            } else {
                System.out.println("Invalid move");
            }
            if(player == 1) player = 2;
            else player = 1;
            System.out.println();
        }
        c4.printBoard();
        System.out.println("Player " + c4.checkForWinner() + " wins!");
    }
}