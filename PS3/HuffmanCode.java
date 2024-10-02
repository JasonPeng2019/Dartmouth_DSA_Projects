import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.File;

/**
 * class that implements Huffman
 * @author Jason Peng, CS 10, 10/2023
 */

public class HuffmanCode implements Huffman {

    public String pathNameEx = "";

    /**
     * Read file provided in pathName and count how many times each character appears
     * @param pathName - path to a file to read
     * @return - Map with a character as a key and the number of times the character appears in the file as value
     * @throws IOException
     */
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {
        // initialize the buffered reader and create a map to store the char and corresponding frequency
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        Map<Character, Long> frequencies = new HashMap<>();
        // if the return integer for read is not -1 (meaning there is a character),
        // add it to the map. Repeat this until you reach the return integer being -1.
        boolean flag = true;
        while (flag == true){
            int charCode = input.read();
            if (charCode == -1) {
                flag = false;
            }
            if (flag == true){
                char letter = (char) (charCode);
                if (frequencies.containsKey(letter)) {
                    Long count = frequencies.get(letter);
                    count += 1;
                    frequencies.put(letter, count);
                } else {
                    frequencies.put(letter, 1L);
                }
            }
        }
        // close the input
        input.close();
        if (frequencies.equals(new HashMap<>())){
            System.out.println("File is empty. Will throw exception:");
        }
        // return the map
        //System.out.println(frequencies);
        return frequencies;
    }

    /**
     * Construct a code tree from a map of frequency counts. Note: this code should handle the special
     * cases of empty files or files with a single character.
     *
     * @param frequencies a map of Characters with their frequency counts from countFrequencies
     * @return the code tree.
     */
    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        // Make a Priority Queue
        PriorityQueue<BinaryTree<CodeTreeElement>> huffmanQueue = new PriorityQueue<>(new TreeComparator());
        // for each key value pair in the map of the characters/frequencies, make it an element that
        // stores both the key and value, then turn that element to a tree. Add all of these to the Priority Queue.
        for (Map.Entry<Character, Long> key : frequencies.entrySet()) {
            CodeTreeElement charNode = new CodeTreeElement(key.getValue(), key.getKey());
            BinaryTree<CodeTreeElement> newTree = new BinaryTree<>(charNode, null, null);
            huffmanQueue.add(newTree);
        }
        // while the huffmanQueue has more than one cohesive tree of three nodes:
        while (huffmanQueue.size() > 1) {
        // for each tree in the Priority Queue, extract the two trees with a root that has the smallest frequency
            BinaryTree<CodeTreeElement> left = huffmanQueue.poll();
            BinaryTree<CodeTreeElement> right = huffmanQueue.poll();
        // Make a new node that has the combined frequencies of its two children roots, which should be the two tree
        // roots that were extracted from Priority Queue.
        // Turn this into a tree:
        // The structure is a tree of three nodes, with the two children each being trees of their
            // own that may or may or not have their own children.
        // Then add this new tree back into the queue
            Long combinedFreq = left.getData().getFrequency() + right.getData().getFrequency();
            CodeTreeElement newRoot = new CodeTreeElement(combinedFreq, null);
            BinaryTree<CodeTreeElement> newRootTree = new BinaryTree<CodeTreeElement>(newRoot, left, right);
            huffmanQueue.add(newRootTree);
        }
        // then return the final tree
        BinaryTree<CodeTreeElement> huffmanTree = huffmanQueue.poll();
       // System.out.println("huffmanTree: "+huffmanTree+"\n");
        return huffmanTree;
    }

    /**
     * Computes the code for all characters in the tree and enters them
     * into a map where the key is a character and the value is the code of 1's and 0's representing
     * that character.
     *
     * @param codeTree the tree for encoding characters produced by makeCodeTree
     * @return the map from characters to codes
     */
    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree) {
        Map<Character, String> a = computeCodesHelper(codeTree, "");
        // Special Case: If the map has an entry with the form a:null, set a:0 to mark it.
        // Since the tree that's being traversed is only a root, the 0 won't matter since
        //it won't be able to traverse to the child anyway.
        for (Map.Entry<Character, String> entry: a.entrySet()){
            if (!a.isEmpty() && (entry.getValue().equals(""))){
                a.put(entry.getKey(), "0");
            }
        }
        //System.out.println(a);
        return a;
        }

    /**
     * Helper for the computeCodes function that is able to keep track of the path
     *
     * @param codeTree the codeTree passed into computeCodes
     * @param currentPath the path of the traversal. Starts as an empty string
     * @return the map from the characters to codes
     */
    private Map<Character, String> computeCodesHelper(BinaryTree<CodeTreeElement> codeTree, String currentPath) {
       // create a map for every recursive call.
        Map<Character, String> codesMap = new HashMap<>();
        // traverse the tree:

        // Base Case: If it's a leaf node, put the character, path as a K/V pair into the map.
        if (!codeTree.hasLeft() && !codeTree.hasRight()) {
            codesMap.put(codeTree.getData().getChar(), currentPath);
            return codesMap;
        }

        // Traverse the left child. Add the submap to the map of this recursive call so that as the
        // traversal bubbles back up, the maps collect more and more submaps for each subtree.
        if (codeTree.hasLeft()) {
            codesMap.putAll(computeCodesHelper(codeTree.getLeft(), currentPath + "0"));
        }

        // Traverse the right child and do the same thing
        if (codeTree.hasRight()) {
            codesMap.putAll(computeCodesHelper(codeTree.getRight(), currentPath + "1"));
        }

        // then return the map. It should return a singular element map for each leaf, but then for
        // parent, it should return the map with the leaf children of the entire subtree.
        return codesMap;
    }

    /**
     * Compress the file pathName and store compressed representation in compressedPathName.
     * @param codeMap - Map of characters to codes produced by computeCodes
     * @param pathName - File to compress
     * @param compressedPathName - Store the compressed data in this file
     * @throws IOException
     */
    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {
        //open a reader and bitWriter
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);
        // make a map with all the char codes and chars of the file
        Map<Character, String> finalMap = computeCodes(makeCodeTree(countFrequencies(pathName)));
        // while the file is reading (flag true):
        boolean flag = true;
        while (flag == true){
            // if the file is done reading, set flag to false.
            int charInt = input.read();
            if (charInt == -1) {
                flag = false;
            }
            // while the file reads, read the character, look it up in the map, and find the code
            // of the character. iterate through the string, and if the char is a 1, write true,
            // and if the char is 0, write false.
            if (flag == true) {
                char letter = (char) (charInt);
                String charCode = finalMap.get(letter);
                int i = charCode.length();
                for (int j = 0; j < i; j++){
                    boolean bit;
                    char digit = charCode.charAt(j);
                    if (digit == '1'){
                        bit = true;
                    } else if (digit == '0'){
                        bit = false;
                    } else {
                        bit = true;
                       // System.out.println("Not 0 or 1 detected!");
                    }
                    bitOutput.writeBit(bit);
                }
            }
        }
        //close the readers and writers
        input.close();
        bitOutput.close();
    }


    /**
     * Decompress file compressedPathName and store plain text in decompressedPathName.
     *
     * @param compressedPathName   - file created by compressFile
     * @param decompressedPathName - store the decompressed text in this file, contents should match the original file before compressFile
     * @param codeTree             - Tree mapping compressed data to characters
     * @throws IOException
     */
    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {
        // make a bitreader and a writer
        BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));
        BinaryTree<CodeTreeElement> codeTracker = codeTree;

        // while bitreader hasn't reached EOF:
        while (bitInput.hasNext()) {
            // read the bit.
            boolean bit = bitInput.readBit();
            // if the codeTracker has children:
            // if the bit is 0, set the new codetracker as the left child of codetracker.
            if (codeTracker.hasLeft() && bit == false) {
                codeTracker = codeTracker.getLeft();
            // if the bit is 1, set the new codetracker as the right child of codetracker
            } else if (codeTracker.hasRight() && bit == true) {
                codeTracker = codeTracker.getRight();
            // once codetracker doesn't have children anymore:
            // find the char stored at codetracker, set codetracker back to the root for tree re-traversal
            }
            if (!codeTracker.hasRight() && !codeTracker.hasLeft()) {
                char charData = codeTracker.getData().getChar();
                codeTracker = codeTree;
                // write the char stored at codetracker to the output file
                output.write(charData);
            }
        }
        output.close();
        bitInput.close();
    }

    public static void main(String[] args) {
        Huffman test1 = new HuffmanCode();
        try {
            String pathname = "test1.txt";
            String compressedName = "test1Compressed.txt";
            BinaryTree<CodeTreeElement> test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            Map<Character, String> mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);
            pathname = "test2.txt";
            compressedName = "test2Compressed.txt";
            test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);
            pathname = "test3.txt";
            compressedName = "test3Compressed.txt";
            test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);
            pathname = "test4.txt";
            compressedName = "test4Compressed.txt";
            test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);
            pathname = "USConstitution.txt";
            compressedName = "USConstitutionCompressed.txt";
            test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);

            pathname = "WarAndPeace.txt";
            compressedName = "WarAndPeaceCompressed.txt";
            test1Tree = test1.makeCodeTree(test1.countFrequencies(pathname));
            mapTest1 = test1.computeCodes(test1Tree);
            test1.compressFile(mapTest1, pathname, compressedName);
            test1.decompressFile(compressedName, pathname, test1Tree);
            File file = new File(compressedName);
            long fileSizeInBytes = file.length();
            System.out.println("File size of War and Peace: " + fileSizeInBytes + " bytes");

        } catch (IOException e){
            System.out.println("File not found/not openable");
        } catch (NullPointerException e){
            System.out.println("Null Pointer Exception thrown");
        }
    }
}



