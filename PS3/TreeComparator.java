import java.util.Comparator;

/**
 * A comparator class that allows for trees containing codeTreeElements to be compared. Works by
 * comparing the root's frequency with that of another tree.
 * @author Jason Peng, CS 10, 10/2023
 */
public class TreeComparator implements Comparator<BinaryTree<CodeTreeElement>> {
    @Override
    public int compare(BinaryTree<CodeTreeElement> tree1, BinaryTree<CodeTreeElement> tree2) {
        // if the first tree's frequency is smaller than the second tree, put the first tree in priority.
        // if they're equal, put them in same priority.
        // if the first tree's frequency is larger than the second tree, put the 2nd tree in priority.
        if (tree1.getData().getFrequency() < tree2.getData().getFrequency()){
            return -1;
        } else if (tree1.getData().getFrequency() == tree2.getData().getFrequency()){
            return 0;
        }
        return 1;
    }
}
