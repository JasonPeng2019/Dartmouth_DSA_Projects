import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

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
            System.out.println("File is empty. Will throw NullPointer exception:");
        }
        // return the map
       // System.out.println("frequency map" + frequencies);
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
        int order = 0;
        // for each key value pair in the map of the characters/frequencies, make it an element that
        // stores both the key and value, then turn that element to a tree. Add all of these to the Priority Queue.
        for (Map.Entry<Character, Long> key : frequencies.entrySet()) {
            CodeTreeElement charNode = new CodeTreeElement(key.getValue(), key.getKey());
            BinaryTree<CodeTreeElement> newTree = new BinaryTree<>(charNode, null, null);
            order += 1;
            huffmanQueue.add(newTree);
        }
     //   System.out.println("HuffmanQueue: "+ huffmanQueue);
        int iterationCount = 0;
        // while the huffmanQueue has more than one cohesive tree of three nodes:
        while (huffmanQueue.size() > 1) {
            iterationCount+=1;
          //  System.out.println("reach marker A (to be repeated)");
            // for each tree in the Priority Queue, extract the two trees with a root that has the smallest frequency
            BinaryTree<CodeTreeElement> left = huffmanQueue.poll();
          //  System.out.println("passed left"+ left);
            BinaryTree<CodeTreeElement> right = huffmanQueue.poll();
          //  System.out.println("passed right"+ right);
            // Make a new node that has the combined frequencies of its two children roots, which should be the two tree
            // roots that were extracted from Priority Queue.
            // Turn this into a tree:
            // The structure is a tree of three nodes, with the two children each being trees of their
            // own that may or may or not have their own children.
            // Then add this new tree back into the queue
          //  System.out.println("reached marker 1");
            Long combinedFreq = left.getData().getFrequency() + right.getData().getFrequency();
            CodeTreeElement newRoot = new CodeTreeElement(combinedFreq, null);
         //   System.out.println("reached marker 2");
         //   System.out.println("newRoot" + newRoot);
            BinaryTree<CodeTreeElement> newRootTree = new BinaryTree<CodeTreeElement>(newRoot, left, right);
            order += 1;
            huffmanQueue.add(newRootTree);
         //   System.out.println("reach marker 3");
        }
       // System.out.println("reach marker 4");
        // then return the final tree
        BinaryTree<CodeTreeElement> huffmanTree = huffmanQueue.poll();
       // System.out.println("huffmanTree at iteration"+ iterationCount +": "+huffmanTree+"\n");
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
        // Special Case: If the map has an entry with the form a:"", set a:0 to mark it.
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
     * @param pathName - File to compress
     * @param compressedPathName - Store the compressed data in this file
     * @throws IOException
     */
    public void compressFile(String pathName, String compressedPathName) throws IOException {
        //open a reader and bitWriter
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);
        // make a frequency table, and a map that has the path of each character.
        Map<Character, Long> frequencyTable = countFrequencies(pathName);
        Map<Character, String> finalMap = computeCodes(makeCodeTree(frequencyTable));
        // Now need to store this same frequency table into encoding.

        // The easiest way to do this is store the frequencies as data, encode that data, and
        // then build a new tree based on that data.

        // The easiest way to encode the data is by using ASCII - 8 bits for each ASCII character, 32 bits for each
        // frequency.

        // for each character in the frequency table:

        for (Map.Entry<Character, Long> entry : frequencyTable.entrySet()) {
            // create a bitvalue for each character, and create a bitvalue for each frequency (int).
            String charBit = Integer.toBinaryString((int) entry.getKey());
            while (charBit.length() < 16) {
                charBit = "0" + charBit;
            }
            String freqBit = Long.toBinaryString(entry.getValue());
            while (freqBit.length() < 32) {
                freqBit = "0" + freqBit;
            }
            String outBit = charBit + freqBit;
            // Write the bit sequences as "true" and "false" to the compressedFile
            for (int j = 0; j < 48; j++) {
                boolean bit;
                char digit = outBit.charAt(j);
                if (digit == '1') {
                    bit = true;
                } else if (digit == '0') {
                    bit = false;
                } else {
                    bit = false;
                    System.out.println("Error. Reading a bit that is not a 1 or 0.");
                }
                bitOutput.writeBit(bit);

            }
        }
            // When you reach the end of the frequency map, add 8 0's
            // to the sequence. Since 'Null' is not a character in the map, there should never be a sequence of 8 0's
            // in a "character" slot. This will indicate that it is the delimiter for starting to read the content.
        for (int i = 0; i < 16; i++) {
            bitOutput.writeBit(false);
        }


        // after the header is done, read the file.

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
     * helper method for turning a bitString to Char.
     * @param bitString String. the 8bitString you want to turn into char
     * @return char representation of 8bitString
     */
    public char bitToChar(String bitString){
        return (char)Integer.parseInt(bitString,2);
    }

    /**
     * helper method for turning a bitString into Long
     * @param bitString String. the 32bitString you want to turn into the Long
     * @return Long representation of 32bitString
     */
    public Long bitToLong(String bitString){
        return Long.parseLong(bitString,2);
    }

    /**
     * Decompress file compressedPathName and store plain text in decompressedPathName.
     *
     * @param compressedPathName   - file created by compressFile
     * @param decompressedPathName - store the decompressed text in this file, contents should match the original file before compressFile
     * @throws IOException
     */
    public void decompressFile(String compressedPathName, String decompressedPathName) throws IOException {
        // make a bitreader and a writer
     //   System.out.println("============================Start Decomp============================");
        BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
     //   System.out.println("file1 found");
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));
     //   System.out.println("file 2 found");

        BinaryTree<CodeTreeElement> codeTree;

        boolean hasReachedHeader = false;
        //make a map from the frequency map information sent over by the encoding
        Map <Character, Long> frequencyMap = new HashMap<>();

        // if it hasn't reached the header:
        while (hasReachedHeader == false){
            // first decode the 16 bit character
            String charBitString = "";
            for (int i=0; i<16; i++){
                 boolean nextBit = bitInput.readBit();
                 if (nextBit == true){
                     charBitString += '1';
                 } else {
                     charBitString += '0';
                 }
            }

            if (charBitString.equals("0000000000000000")){
                hasReachedHeader = true;
            }
            char bitChar = bitToChar(charBitString);
            // if it hasn't hit the header, then decode the 32 bit Long for the frequency that should
            // follow each character encoding
            String longBitString = "";
            if (hasReachedHeader == false){
                for (int i = 0 ; i < 32; i ++){
                    boolean nextBit = bitInput.readBit();
                    if (nextBit == true){
                        longBitString += 1;
                    } else {
                        longBitString += 0;
                    }
                }
                Long bitLong = bitToLong(longBitString);
                // once you've decoded the character and its frequency, add it to the map you're using
                // to reconstruct the tree.
                frequencyMap.put(bitChar, bitLong);
            }
        }

      //  System.out.println("-------------------------------------------------------\n");
      //  System.out.println("frequency map: "+frequencyMap);
        // reconstruct the tree:
        codeTree = makeCodeTree(frequencyMap);
        BinaryTree<CodeTreeElement> codeTracker = codeTree;

     //   System.out.println("codeTracker: " + codeTracker);

        // while bitreader hasn't reached EOF:
        while (bitInput.hasNext()) {
         //   System.out.println("loop runs");
            // read the bit.
            boolean bit = bitInput.readBit();
            // if the codeTracker has children:
            // if the bit is 0, set the new codetracker as the left child of codetracker.
            if (codeTracker.hasLeft() && bit == false) {
            //    System.out.println("0");
                codeTracker = codeTracker.getLeft();
                // if the bit is 1, set the new codetracker as the right child of codetracker
            } else if (codeTracker.hasRight() && bit == true) {
             //   System.out.println("1");
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
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            pathname = "test2.txt";
            compressedName = "test2Compressed.txt";
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            pathname = "test3.txt";
            compressedName = "test3Compressed.txt";
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            pathname = "test4.txt";
            compressedName = "test4Compressed.txt";
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            pathname = "USConstitution.txt";
            compressedName = "USConstitutionCompressed.txt";
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            pathname = "WarAndPeace.txt";
            compressedName = "WarAndPeaceCompressed.txt";
            test1.compressFile(pathname, compressedName);
            test1.decompressFile(compressedName, pathname);
            File file = new File(compressedName);
            long fileSizeInBytes = file.length();
            System.out.println("File size of War and Peace: " + fileSizeInBytes + " bytes");
        } catch (IOException e){
            System.out.println("File not found/not openable");
        }
    }

}



