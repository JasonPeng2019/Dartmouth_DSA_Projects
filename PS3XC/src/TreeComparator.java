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
        // if they're equal, check the order of creation. The first to be created
        // get priority. The reason this needs to be done is so the tree can be the EXACT same for
        // the encode and decode.
        // if the first tree's frequency is larger than the second tree, put the 2nd tree in priority.
        if (tree1.getData().getFrequency() < tree2.getData().getFrequency()){
            return -1;
        } else if (tree1.getData().getFrequency() == tree2.getData().getFrequency()){
            int order1 = tree1.getOrder();
            int order2 = tree2.getOrder();
            if (order1 < order2) {
                return -1;
            } else {
                return 1;
            }
            //}
        }
        return 1;
    }
}
