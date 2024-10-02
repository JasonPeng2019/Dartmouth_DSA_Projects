package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor2 extends JFrame {


    private String sendString = "group->";

    private String sendString2 = "ungroup->";



    private static String serverIP = "localhost";            // IP address of sketch server
    // "localhost" for your own machine;
    // or ask a friend for their IP address

    private static final int width = 800, height = 800;        // canvas size

    private ArrayListStack<Sketch> undoStack = new ArrayListStack<>();
    private ArrayListStack<Sketch> redoStack = new ArrayListStack<>();

    // Current settings on GUI
    public enum Mode {
        DRAW, MOVE, RECOLOR, DELETE, MOVETOFRONT, MOVETOBACK, GROUP, UNGROUP, UNDO, REDO
    }

    private Mode mode = Mode.DRAW;                // drawing/moving/recoloring/deleting objects
    private String shapeType = "ellipse";        // type of object to add
    private Color color = Color.black;            // current drawing color

    // Drawing state
    // these are remnants of my implementation; take them as possible suggestions or ignore them
    private Shape curr = null;                    // current shape (if any) being drawn
    private Sketch sketch;                        // holds and handles all the completed objects
    private int movingId = -1;                    // current shape id (if any; else -1) being moved
    private Point drawFrom = null;                // where the drawing started
    private Point moveFrom = null;                // where object is as it's being dragged

    private String flag = "false";                // flag to mark whether this client has already moved the object or not
    // (so it is not called twice)


    // Communication
    private EditorCommunicator2 comm;            // communication with the sketch server

    public Editor2() {
        super("Graphical Editor");

        sketch = new Sketch();

        // Connect to server
        comm = new EditorCommunicator2(serverIP, this);
        comm.start();

        // Helpers to create the canvas and GUI (buttons, etc.)
        JComponent canvas = setupCanvas();
        JComponent gui = setupGUI();

        // Put the buttons and canvas together into the window
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(gui, BorderLayout.NORTH);

        // Usual initialization
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Creates a component to draw into
     */
    private JComponent setupCanvas() {
        JComponent canvas = new JComponent() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSketch(g);
            }
        };

        canvas.setPreferredSize(new Dimension(width, height));

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                try {
                    handlePress(event.getPoint());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public void mouseReleased(MouseEvent event) {
                handleRelease();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent event) {
                handleDrag(event.getPoint());
            }
        });

        return canvas;
    }

    /**
     * Creates a panel with all the buttons
     */
    private JComponent setupGUI() {
        // Select type of shape
        String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
        JComboBox<String> shapeB = new JComboBox<String>(shapes);
        shapeB.addActionListener(e -> shapeType = (String) ((JComboBox<String>) e.getSource()).getSelectedItem());

        // Select drawing/recoloring color
        // Following Oracle example
        JButton chooseColorB = new JButton("choose color");
        JColorChooser colorChooser = new JColorChooser();
        JLabel colorL = new JLabel();
        colorL.setBackground(Color.black);
        colorL.setOpaque(true);
        colorL.setBorder(BorderFactory.createLineBorder(Color.black));
        colorL.setPreferredSize(new Dimension(25, 25));
        JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
                "Pick a Color",
                true,  //modal
                colorChooser,
                e -> {
                    color = colorChooser.getColor();
                    colorL.setBackground(color);
                },  // OK button
                null); // no CANCEL button handler
        chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

        // Mode: draw, move, recolor, or delete
        JRadioButton drawB = new JRadioButton("draw");
        drawB.addActionListener(e -> mode = Mode.DRAW);
        drawB.setSelected(true);
        JRadioButton moveB = new JRadioButton("move");
        moveB.addActionListener(e -> mode = Mode.MOVE);
        JRadioButton recolorB = new JRadioButton("recolor");
        recolorB.addActionListener(e -> mode = Mode.RECOLOR);
        JRadioButton deleteB = new JRadioButton("delete");
        deleteB.addActionListener(e -> mode = Mode.DELETE);
        JRadioButton moveToFrontB = new JRadioButton("moveToFront");
        moveToFrontB.addActionListener(e -> mode = Mode.MOVETOFRONT);
        JRadioButton moveToBackB = new JRadioButton("moveToBack");
        moveToBackB.addActionListener(e -> mode = Mode.MOVETOBACK);
        JRadioButton groupB = new JRadioButton("group");
        groupB.addActionListener(e -> mode = Mode.GROUP);
        JRadioButton ungroupB = new JRadioButton("ungroup");
        ungroupB.addActionListener(e -> mode = Mode.UNGROUP);
        JRadioButton undoB = new JRadioButton("undo");
        undoB.addActionListener(e -> mode = Mode.UNDO);
        JRadioButton redoB = new JRadioButton("redo");
        redoB.addActionListener(e -> mode = Mode.REDO);
        ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
        modes.add(drawB);
        modes.add(moveB);
        modes.add(recolorB);
        modes.add(deleteB);
        modes.add(moveToFrontB);
        modes.add(moveToBackB);
        modes.add(groupB);
        modes.add(ungroupB);
        modes.add(undoB);
        modes.add(redoB);
        JPanel modesP = new JPanel(new GridLayout(2, 5)); // group them on the GUI
        modesP.add(drawB);
        modesP.add(moveB);
        modesP.add(recolorB);
        modesP.add(deleteB);
        modesP.add(moveToFrontB);
        modesP.add(moveToBackB);
        modesP.add(groupB);
        modesP.add(ungroupB);
        modesP.add(undoB);
        modesP.add(redoB);

        // Put all the stuff into a panel
        JComponent gui = new JPanel();
        gui.setLayout(new FlowLayout());
        gui.add(shapeB);
        gui.add(chooseColorB);
        gui.add(colorL);
        gui.add(modesP);
        return gui;
    }

    /**
     * Getter for the sketch instance variable
     */
    public Sketch getSketch() {
        return sketch;
    }

    /**
     * Draws all the shapes in the sketch,
     * along with the object currently being drawn in this editor (not yet part of the sketch)
     */
    public synchronized void drawSketch(Graphics g) {
        // TODO: YOUR CODE HERE
        if (curr != null) {
            curr.draw(g);
        }
        for (int s : sketch.getShapeMap().keySet()) {
            sketch.getShapeMap().get(s).draw(g);
        }
    }

    // Helpers for event handlers

    /**
     * Helper method for press at point
     * In drawing mode, start a new object;
     * in moving mode, (request to) start dragging if clicked in a shape;
     * in recoloring mode, (request to) change clicked shape's color
     * in deleting mode, (request to) delete clicked shape
     */
    private void handlePress(Point p) throws Exception {
        // TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) { // create new shape
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            drawFrom = new Point(p.x, p.y);
            // create a new shape depending on the shape type
            if (shapeType.equals("ellipse")) {
                curr = new Ellipse(drawFrom.x, drawFrom.y, color);
            } else if (shapeType.equals("freehand")) {
                curr = new Polyline(p.x, p.y, color);
            } else if (shapeType.equals("rectangle")) {
                curr = new Rectangle(drawFrom.x, drawFrom.y, color);
            } else if (shapeType.equals("segment")) {
                curr = new Segment(drawFrom.x, drawFrom.y, color);
            }

        } else if (mode == Mode.MOVE) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            // else if move, determine the id of the shape that has been clicked.
            drawFrom = new Point(p.x, p.y);
            Map<Integer, Shape> tempShaps = sketch.getShapeMap().descendingMap();
            for (Integer i : tempShaps.keySet()) {
                if (tempShaps.get(i).contains(p.x, p.y)) {
                    moveFrom = new Point(p.x, p.y);
                    movingId = i;
                    break;
                }
            }

            // set a flag to true if there was a shape that was indeed clicked.
            if (movingId != -1) {
                flag = "true";
                String[] placement = new String[]{"0", "1"};
                addUndo(placement, "false", undoStack, sketch);
                System.out.println("FIRST CLICKED MOVE");
            }
        } else if (mode == Mode.RECOLOR) {
            System.out.println("RECOLOR PRESSED");
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            // else if recolor,recolor upon click. Send recolor request to server
            Map<Integer, Shape> tempShaps = sketch.getShapeMap().descendingMap();
            for (int i : tempShaps.keySet()) {
                if (tempShaps.get(i).contains(p.x, p.y)) {
                    comm.send("recolor->" + i + ":" + color.getRGB());
                    break; // only remove the most recent shape (ie. the top layer)
                }
            }
        } else if (mode == Mode.DELETE) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            // else if delete, delete upon click
            // get the delete id from the sketch map
            int delID = -1;
            System.out.println("SKETCH SIZE at time of delete click"+sketch.getSize());
            System.out.println("SKETCH at time of delete click"+sketch);
            for (int i: sketch.getShapeMap().descendingKeySet()) {
                if (sketch.getShapeMap().get(i).contains(p.x, p.y)) {
                    delID = i;
                    break; // only remove the most recent shape (ie the top layer)
                }
            }
            // delete after iterating through the loop. Send a delete request to the server.
            if (delID != -1) {
                comm.send("delete->" + delID);
            }
        } else if (mode == Mode.MOVETOFRONT) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            Map<Integer, Shape> tempShaps = sketch.getShapeMap().descendingMap();
            for (Integer i : tempShaps.keySet()) {
                if (tempShaps.get(i).contains(p.x, p.y)) {
                    movingId = i;
                    break;
                }
            }
            if (movingId != -1) {
                comm.send("moveToFront->" + movingId);
                movingId = -1;
            }
        } else if (mode == Mode.MOVETOBACK) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            Map<Integer, Shape> tempShaps = sketch.getShapeMap().descendingMap();
            for (Integer i : tempShaps.keySet()) {
                if (tempShaps.get(i).contains(p.x, p.y)) {
                    movingId = i;
                    break;
                }
            }
            if (movingId != -1) {
                comm.send("moveToBack->" + movingId);
                movingId = -1;
            }
        } else if (mode == Mode.GROUP) {
            comm.send(sendString2);
            sendString2 = "ungroup->";
            for (int i : sketch.getShapeMap().descendingMap().keySet()) {
                if (sketch.getShapeMap().get(i).contains(p.x, p.y)) {
                    if (!sendString.contains("" + i)) {
                        sendString += (i + ",");
                        break;
                    }
                }
            }
        } else if (mode == Mode.UNGROUP) {
            comm.send(sendString);
            sendString = "group->";
            for (int i : sketch.getShapeMap().descendingMap().keySet()) {
                if (sketch.getShapeMap().get(i).contains(p.x, p.y)) {
                    if (!sendString2.contains("" + i)) {
                        sendString2 += (i + ",");
                        break;
                    }
                }
            }
        } else if (mode == Mode.UNDO) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            comm.send("undo->0");
            System.out.println("SEND THE UNDO COMMAND");
        } else if (mode == Mode.REDO) {
            comm.send(sendString);
            sendString = "group->";
            comm.send(sendString2);
            sendString2 = "ungroup->";
            comm.send("redo->0");
        }
    }

    /**
     * Helper method for drag to new point
     * In drawing mode, update the other corner of the object;
     * in moving mode, (request to) drag the object
     */
    private void handleDrag(Point p) {
        // TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) { // change shape
            if (shapeType.equals("ellipse")) {
                ((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            } else if (shapeType.equals("freehand")) {
                ((Polyline) curr).addPoint(p.x, p.y);
            } else if (shapeType.equals("rectangle")) {
                ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            } else if (shapeType.equals("segment")) {
                ((Segment) curr).setEnd(p.x, p.y);
            }
            repaint();

        } else if (mode == Mode.MOVE) {
            // move the shape. Update moveFrom over and over again while dragging so
            // the shape moves at a constant speed and stays with the mouse.
            if (movingId != -1) {
                String flag1 = "false";
                ArrayList<Integer> markGroup = new ArrayList<>();
                for (ArrayList<Integer> groupArray : sketch.grouped) {
                    if (groupArray.contains(movingId)) {
                        markGroup = groupArray;
                        flag1 = "true";
                    }
                }
                if (flag1.equals("false")) {
                    sketch.getShapeMap().get(movingId).moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
                    moveFrom = new Point(p.x, p.y);
                    repaint();
                } else {
                    int dx = p.x - moveFrom.x;
                    int dy = p.y - moveFrom.y;
                    for (int i : markGroup) {
                        sketch.getShapeMap().get(i).moveBy(dx, dy);
                        repaint();
                    }
                    moveFrom = new Point(p.x, p.y);

                }
            }
        }
    }


    /**
     * Helper method for release
     * In drawing mode, pass the add new object request on to the server;
     * in moving mode, release it
     */
    private void handleRelease() {
        // TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) {
            // draw a shape upon release. send a shape creation request to the server
            comm.send("newShape->" + curr);
            curr = null;

            repaint();
        } else if (mode == Mode.MOVE) {
            //update moving ID back to -1 upon releasing mouse, and send a move request to the server.
            if (movingId != -1) {
                // need a start location to calculate distance moved
                Point tempForStart = drawFrom;
                // need end location to calculate distance moved
                drawFrom = moveFrom;
                comm.send("move->" + movingId + ":" + drawFrom.x + "," + drawFrom.y + "," +
                        tempForStart.x + "," + tempForStart.y);
                movingId = -1;
            }
            repaint();
        }
    }

    /**
     * helper method for adding an ellipse based on an input String
     *
     * @param coColorList
     * @return
     */
    public static Ellipse addEllipse(String[] coColorList) {
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Ellipse(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));
    }

    /**
     * helper method for parsing and adding a rectangle object from a string
     *
     * @param coColorList
     * @return
     */
    public static Rectangle addRectangle(String[] coColorList) {
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Rectangle(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));
    }

    /**
     * helper method for adding polyline object, parsed from input string.
     *
     * @param coColorList
     * @return
     */
    public static Polyline addPolyline(String[] coColorList) {
        int color = Integer.parseInt(coColorList[0]);
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        ArrayList<Integer> coorList = new ArrayList<>();
        for (int i = 3; i < coColorList.length; i++) {
            coorList.add(Integer.parseInt(coColorList[i]));
        }
        Polyline polyline = new Polyline(x1Cor, y1Cor, new Color(color));
        for (int i = 0; i < coorList.size(); i += 2) {
            int yCor = i + 1;
            polyline.addPoint(coorList.get(i), coorList.get(yCor));
        }
        return polyline;
    }

    /**
     * helper method to add segment, parsed from input string
     *
     * @param coColorList
     * @return
     */
    public static Segment addSegment(String[] coColorList) {
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Segment(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));

    }

    /**
     * helper method to add a shape based on an input string.
     *
     * @param shapeString
     * @return
     */
    public synchronized static Shape addShape(String shapeString) {
        String[] shapeArray = shapeString.split(":");
        String shapeType = shapeArray[0];
        String[] coColorList = shapeArray[1].trim().split(",");
        Shape inShape = null;
        if (shapeType.equals("ellipse")) {
            inShape = addEllipse(coColorList);
        } else if (shapeType.equals("rectangle")) {
            inShape = addRectangle(coColorList);
        } else if (shapeType.equals("polyline")) {
            inShape = addPolyline(coColorList);
        } else if (shapeType.equals("segment")) {
            inShape = addSegment(coColorList);
        }
        return inShape;
    }

    public static synchronized void delete(Sketch sketch, String[] stringArray, ArrayList<ArrayList<Integer>> grouped) {
        String flag1 = "false";
        int delID = Integer.parseInt(stringArray[1].trim());
        ArrayList<Integer> targetArray = new ArrayList<>();
        for (ArrayList<Integer> array : grouped) {
            if (array.contains(delID)){
                flag1 = "true";
                targetArray = array;
            }
        }
        if (flag1.equals("false")) {
            sketch.getShapeMap().remove(Integer.parseInt(stringArray[1].trim()));
            sketch.setSize(sketch.getSize()-1);
        } else {
            for (int i : targetArray) {
                sketch.getShapeMap().remove(i);
                sketch.getShapeMap().remove(Integer.parseInt(stringArray[1].trim()));
                sketch.setSize(sketch.getSize()-1);
            }
            grouped.remove(targetArray);
        }
    }

    public static synchronized void moveToFront(Sketch sketch, String[] stringArray, ArrayList<ArrayList<Integer>> grouped) {
        System.out.println("arrays before operations" + grouped);
        String flag1 = "false";
        ArrayList<Integer> targetArray = new ArrayList<>();
        for (ArrayList<Integer> array : grouped) {
            System.out.println("array before operations:" + array);
            if (array.contains(Integer.parseInt(stringArray[1].trim()))) {
                flag1 = "true";
                targetArray = array;
            }
        }
        if (flag1.equals("false")) {
            int movingId = Integer.parseInt(stringArray[1]);
            TreeMap<Integer, Shape> tempShaps = sketch.getShapeMap();
            Shape temp = tempShaps.get(movingId);
            tempShaps.remove(movingId);
            int lastidx = tempShaps.lastKey();
            tempShaps.put(lastidx + 1, temp);
        } else {
            TreeMap<Integer, Shape> tempShaps = sketch.getShapeMap();
            ArrayList<Integer> newGroup = new ArrayList<>();
            targetArray.sort(Integer::compareTo);
            for (int i : targetArray) {
                Shape temp = tempShaps.get(i);
                tempShaps.remove(i);
                int lastidx = tempShaps.lastKey();
                tempShaps.put(lastidx + 1, temp);
                newGroup.add(lastidx + 1);
            }
            for (int i = 0; i < grouped.size(); i++) {
                if (grouped.get(i).equals(targetArray)) {
                    grouped.set(i, newGroup);
                }
            }
        }
    }

    public static synchronized void moveToBack(Sketch sketch, String[] stringArray, ArrayList<ArrayList<Integer>> grouped) {
        //problem: moveToBack not working...at all. find issue later. going to lseep now
        System.out.println("operations before moving back" + grouped);
        String flag1 = "false";
        ArrayList<Integer> targetArray = new ArrayList<>();
        for (ArrayList<Integer> array : grouped) {
            System.out.println("array before back:" + array);
            if (array.contains(Integer.parseInt(stringArray[1].trim()))) {
                flag1 = "true";
                targetArray = array;
            }
        }
        System.out.println(flag1);
        if (flag1.equals("false")) {
            ArrayList<Integer> newGroupIndices = new ArrayList<>();
            ArrayList<Integer> targetArray2 = new ArrayList<>();

            System.out.println("sketch.getShapeMap() before doing anything" + sketch.getShapeMap());

            int movingId = Integer.parseInt(stringArray[1]);
            TreeMap<Integer, Shape> tempShaps = sketch.getShapeMap();
            TreeMap<Integer, Shape> tempShapsCopy = sketch.deepCopy().getShapeMap();
            Shape temp = tempShaps.get(movingId);
            tempShaps.remove(movingId);
            System.out.println("map wellness check 4:" + tempShaps);
            System.out.println("map wellness check 4 (2): " + tempShapsCopy);
            for (int id : tempShapsCopy.descendingKeySet()) {
                System.out.println("map wellness check middle of loop1" + sketch.getShapeMap());
                if (!(id == movingId)) {
                    Shape temp2 = tempShaps.get(id);
                    tempShaps.remove(id);
                    String flag3 = "false";
                    System.out.println("actual REMOVING AND ADDING PROCESS----------------------");
                    System.out.println("grouped:"+grouped);
                    System.out.println("flag3 equals false:"+ flag3.equals("false"));
                    for (ArrayList<Integer> array : grouped) {
                        if (array.contains(id)) {
                            flag3 = "true";
                            targetArray2 = array;
                        }
                    }
                    if (flag3.equals("false")) {
                        System.out.println("adding back step ran");
                        tempShaps.put(id + 1, temp2);
                    } else {
                        tempShaps.put(id + 1, temp2);
                        newGroupIndices.add(id + 1); // wtf does this do?
                    }
                }
            }

            System.out.println("map wellness check 6" + sketch.getShapeMap());
            for (int i = 0; i < grouped.size(); i++) {
                if (grouped.get(i).equals(targetArray2)) {
                    grouped.set(i, newGroupIndices);
                }
            }
            System.out.println("map wellness check last:" + sketch.getShapeMap());
            tempShaps.put(0, temp);

        } else {
            TreeMap<Integer, Shape> newTree = new TreeMap<>();
            ArrayList<Integer> newGroupIndices = new ArrayList<>();
            TreeMap<Integer, Shape> tempShaps = sketch.getShapeMap();
            TreeMap<Integer, Shape> tempShapsCopy = sketch.deepCopy().getShapeMap();

            // Sorting to ensure the order of shapes in the group is maintained
            Collections.sort(targetArray);

            System.out.println("targetArray:" + targetArray);
            //remove all the ids in the target
            for (int id : targetArray) {
                System.out.println("tempShaps before removal of corresponding ID:" + tempShaps);
                System.out.println("removing:" + id);
                newTree.put(id, tempShaps.get(id));
                tempShaps.remove(id);
                System.out.println("tempShaps post removal of targetArray ID:" + tempShaps);
                // store them all in a new treemap
            }
            // shift each shape (except the target ids) up by one index, starting from the end.
            System.out.println("tempShaps before moving all non targetArrays up 1" + tempShaps);
            for (Integer id : tempShapsCopy.descendingKeySet()) {
                System.out.println("Inside loop. ID: " + id);
                if (!targetArray.contains(id)) {
                    System.out.println("in this loop ID:" + id + "a shape was updated");
                    Shape shape = tempShaps.get(id);
                    tempShaps.remove(id);
                    tempShaps.put(id + targetArray.size(), shape);
                }
            }
            System.out.println("tempShaps after moving nontargets up1" + tempShaps);
            // then from lowest to highest, add the shapes to the OG map
            int newIndex = 0;
            System.out.println("newTree IDs and shapes:" + newTree);
            for (int id : newTree.keySet()) {
                Shape shape = newTree.get(id);
                System.out.println("hits this step before error1: " + sketch.getShapeMap() + "," + tempShaps);
                tempShaps.put(newIndex, shape);
                newGroupIndices.add(newIndex);
                newIndex++;
                System.out.println("hits this step before error2: " + sketch.getShapeMap() + "," + tempShaps);

            }

            // Update grouped list with new indices
            System.out.println("newGroupIndices" + newGroupIndices);
            for (int i = 0; i < grouped.size(); i++) {
                if (grouped.get(i).equals(targetArray)) {
                    grouped.set(i, newGroupIndices);
                }
            }
        }
    }


    public static synchronized void group(String[] stringArray, ArrayList<ArrayList<Integer>> grouped) {
        String[] groupList = stringArray[1].trim().split(",");
        ArrayList<Integer> targetGroup = new ArrayList<>();
        // check if any of the existing groups are grouped with the new group you are making
        for (String string : groupList) {
            for (ArrayList<Integer> array : grouped) {
                if (array.contains(Integer.parseInt(string))) {
                    targetGroup = array;
                }
            }
        }
        // if not, make a new group
        if (targetGroup.equals(new ArrayList<Integer>())) {
            ArrayList<Integer> toAdd = new ArrayList<>();
            for (String string : groupList) {
                toAdd.add(Integer.parseInt(string));
            }
            grouped.add(toAdd);
        } else {
            // if so, then add all of them to the original group (except the ones in the old group)
            for (String string : groupList) {
                if (!targetGroup.contains(Integer.parseInt(string))) {
                    targetGroup.add(Integer.parseInt(string));
                }
            }
        }
    }


    public static synchronized void ungroup(String[] stringArray, ArrayList<ArrayList<Integer>> grouped) {
        String[] ungroupList = stringArray[1].trim().split(",");
        // check if any of the existing groups are grouped with the new group you are making
        for (String string : ungroupList) {
            for (ArrayList<Integer> array : grouped) {
                if (array.contains(Integer.parseInt(string))) {
                    array.removeIf(toDelete -> toDelete == (Integer.parseInt(string)));
                }
            }
        }
    }

    public static synchronized Sketch undo(Sketch sketch, ArrayListStack<Sketch> undoStack,
                                           ArrayListStack<Sketch> redoStack) throws Exception {
        redoStack.push(sketch.deepCopy());
        sketch = undoStack.pop();
        return sketch;
    }

    public static synchronized Sketch redo(ArrayListStack<Sketch> redoStack) throws Exception {
        Sketch sketch = redoStack.pop();
        return sketch;
    }

    public static synchronized void addUndo(String[] stringArray, String flag, ArrayListStack<Sketch> undoStack, Sketch sketch) throws Exception {
        if (!stringArray[0].equals("undo") && (stringArray.length > 1) && flag.equals("false")) {
            undoStack.push(sketch.deepCopy());
        }
    }

    public static synchronized String setFlagFalse(String flag) {
        flag = "false";
        return flag;
    }

    public static void reverseArray(String[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            String temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    public static synchronized ArrayListStack<Sketch> clearRedo(){
        return new ArrayListStack<Sketch>();
    }

    /**
     * a method that parses a string upon receiving a message from EditorCommunicator, and then performs
     * the corresponding actions to the local sketch map.
     * <p>
     * <p>
     * note to grader: considered receiving just the sketch and rebuilding the entire sketch map from scratch
     * but that would have a runtime of n(logn), while updating the specific shape after a recieved command
     * has a runtime of log(n). So despite needing more code for the 2nd option, went with 1st.
     *
     * @param inputString
     */
    public void receiveString(String inputString) throws Exception {
        System.out.println("--------------------------------------------------------------");
        System.out.println("INPUT STRING:::: "+ inputString);
        System.out.println("undoStack at start"+undoStack.list + "| size" + undoStack.list.size());
        System.out.println("redoStack at top" +redoStack.list + "size" + redoStack.list.size());

        if (inputString.split("->").length>1) {
            System.out.println("sketch at top" + sketch);
            if (inputString.split("->")[0].equals("undo")) {
                System.out.println("grouped at top:" + sketch.grouped);
            }
            if (!undoStack.isEmpty()) {
                System.out.println("peek undo at top" + undoStack.peek());
            }
            String[] stringArray = inputString.split("->", -1);
            if (!stringArray[0].equals("redo") || !redoStack.isEmpty()) {
                // something wrong with addundo
                addUndo(stringArray, flag, undoStack, sketch);
            }
            if (!stringArray[0].equals("undo") && !stringArray[0].equals("redo")){
                redoStack = clearRedo();
            }
            // build the local sketch tree map
            System.out.println("sketch after addundo:" + sketch);
            if (!undoStack.isEmpty()) {
                System.out.println("peek at addundo" + undoStack.peek());
            }
            if (stringArray[0].equals("sketch") && stringArray.length != 1) {
                System.out.println("STRING ARRAY LENGTH" +stringArray.length);
                for (String ele: stringArray){
                    System.out.println("ele:" + ele);
                }
                if (!stringArray[1].equals("")){
                    String[] idAndShapesArray = stringArray[1].split("&&");
                    for (String idAndShape : idAndShapesArray) {
                        int id = Integer.parseInt(idAndShape.split("/")[0]);
                        String shapeString = idAndShape.split("/")[1];
                        //problem: doesn't maintain IDs, starts from scratch
                        // other problems: delete doesn't delete from the group list
                        Shape inShape = addShape(shapeString);
                        System.out.println("sketch size"+sketch.getSize());
                        // problem: as I add it to the sketch, I'm not adding them with proper IDs either.
                        sketch.addShape(id, inShape);
                        repaint();
                    }
                    System.out.println("built sketch"+sketch);
                }
                String[] sketchArray = stringArray[3].split("!!");
                System.out.println("STRING STRINGARRAY[3]:"+stringArray[3]);
                System.out.println("SKETCHARRAY::::");
                for (String sketch: sketchArray){
                    System.out.println(sketch);
                }
                String [] sketchArrayCopy = Arrays.copyOf(sketchArray, sketchArray.length);
                reverseArray(sketchArrayCopy);
                if (!sketchArray[0].equals("")) {
                    System.out.println("building undoStack initiated");
                    for (String sketchString : sketchArrayCopy) {
                        Sketch toAdd = new Sketch();
                        String[] idAndShapesArray = sketchString.split("&&");
                        for (String idAndShape : idAndShapesArray) {
                            int id = Integer.parseInt(idAndShape.split("/")[0]);
                            String shapeString = idAndShape.split("/")[1];
                            Shape inShape = addShape(shapeString);
                            System.out.println("undo sketch size" + toAdd.getSize());
                            toAdd.addShape(id, inShape);
                        }
                        undoStack.push(toAdd);
                    }
                }
                String[] sketchRedoArray = stringArray[5].split("!!");
                if (!sketchRedoArray[0].equals("")) {
                    System.out.println("building undoStack initiated");
                    for (String sketchString : sketchRedoArray) {
                        Sketch toAdd = new Sketch();
                        String[] idAndShapesArray = sketchString.split("&&");
                        for (String idAndShape : idAndShapesArray) {
                            int id = Integer.parseInt(idAndShape.split("/")[0]);
                            String shapeString = idAndShape.split("/")[1];
                            Shape inShape = addShape(shapeString);
                            System.out.println("sketch size" + toAdd.getSize());
                            toAdd.addShape(id, inShape);
                        }
                        redoStack.push(toAdd);
                    }
                }
                System.out.println("End of build");
                System.out.println("UNDOSTACK.LIST"+undoStack.list);
                System.out.println("UNDOSTACKLIST SIZE"+undoStack.list.size());
                System.out.println("IS UNDOSTACK EMPTY"+undoStack.isEmpty());
                System.out.println(redoStack.list);
                System.out.println(redoStack.isEmpty());
                if (!(undoStack.isEmpty())){
                    System.out.println("UndoStack:"+undoStack.peek());
                }
                if (!(redoStack.isEmpty())){
                    System.out.println("READOSTACK" +redoStack.peek());
                }
                repaint();
                // add a new shape to the local sketch tree map
            } else if (stringArray[0].equals("newShape")) {
                System.out.println("step3:" + sketch.getShapeMap());
                String shapeString = stringArray[1];
                Shape inShape = addShape(shapeString);
                System.out.println("size of sketch:" + sketch.getSize());
                sketch.addShape(inShape);
                System.out.println(shapeString);
                repaint();
                System.out.println("after add:" + sketch.getShapeMap());
                // recolor a clicked shape in the local tree map
            } else if (stringArray[0].equals("recolor")) {
                String[] iDAndColor = stringArray[1].split(":");
                int id = Integer.parseInt(iDAndColor[0].trim());
                String colorString = iDAndColor[1].trim(); // e.g., "16777215" for white
                int rgb = Integer.parseInt(colorString);
                Color color = new Color(rgb);
                ArrayList<Integer> targetArray = new ArrayList<>();
                String flag1 = "false";
                for (ArrayList<Integer> array : sketch.grouped) {
                    if (array.contains(id)) {
                        flag1 = "true";
                        targetArray = array;
                    }
                }
                if (flag1.equals("false")) {
                    sketch.getShapeMap().get(id).setColor(color);
                } else {
                    for (int idx : targetArray) {
                        sketch.getShapeMap().get(idx).setColor(color);
                    }
                }
                repaint();
            }
            // delete a clicked shape in the local tree map
            else if (stringArray[0].equals("delete")) {
                delete(sketch, stringArray, sketch.grouped);
                repaint();
                // move a click+drag shape in the local tree map
            } else if (stringArray[0].equals("move")) {
                String[] moveString = stringArray[1].trim().split(":");
                int moveID = Integer.parseInt(moveString[0].trim());
                String[] coordinateString = moveString[1].trim().split(",");
                int corX = Integer.parseInt(coordinateString[0]);
                int corY = Integer.parseInt(coordinateString[1]);
                int startCorX = Integer.parseInt(coordinateString[2]);
                int startCorY = Integer.parseInt(coordinateString[3]);
                ArrayList<Integer> targetArray = new ArrayList<>();
                // if the flag is false, the shape was not moved by this client and thus needs to move.
                if (flag.equals("false")) {
                    String flag2 = "false";
                    for (ArrayList<Integer> array : sketch.grouped) {
                        if (array.contains(moveID)) {
                            flag2 = "true";
                            targetArray = array;
                        }
                    }
                    if (flag2.equals("false")) {
                        sketch.getShapeMap().get(moveID).moveBy(corX - startCorX, corY - startCorY);
                        System.out.println("not in group");
                    } else if (flag2.equals("true")) {
                        System.out.println("in group");
                        for (int idx : targetArray) {
                            sketch.getShapeMap().get(idx).moveBy(corX - startCorX, corY - startCorY);
                        }
                    } // if the flag is true, the shape has already been moved in the drag, and does not need
                    // to be moved again. However, the flag must be reset.
                } else if (flag.equals("true")) {
                    flag = setFlagFalse(flag);
                }
                repaint();
            } else if (stringArray[0].equals("moveToFront")) {
                moveToFront(sketch, stringArray, sketch.grouped);
                System.out.println(sketch.getShapeMap());
                repaint();
            } else if (stringArray[0].equals("moveToBack")) {
                moveToBack(sketch, stringArray, sketch.grouped);
                System.out.println(sketch.getShapeMap());
                repaint();
            } else if (stringArray[0].equals("group")) {
                if (stringArray.length > 1) {
                    group(stringArray, sketch.grouped);
                }
            } else if (stringArray[0].equals("ungroup")) {
                if (stringArray.length > 1) {
                    ungroup(stringArray, sketch.grouped);
                }
            } else if (stringArray[0].equals("undo")) {
                if (!undoStack.isEmpty()) {
                    System.out.println("undid");
                    sketch = undo(sketch, undoStack, redoStack);
                    repaint();
                }
            }else if (stringArray[0].equals("redo")) {
                if (!redoStack.isEmpty()) {
                    sketch = redo(redoStack);
                    repaint();
                }
            }
            if (!undoStack.isEmpty()) {
                System.out.println("peek at bottom" + undoStack.peek());
            }
            if ((stringArray[0].equals("group") || stringArray[0].equals("ungroup"))&&flag.equals("true")){
                addUndo(stringArray,"false", undoStack, sketch);
                System.out.println("ADDED UNDOSTACK FOR MOVING SPECIAL CASE");
            }
            System.out.println("undostack at bottom:" + undoStack.list);
            System.out.println("redoStack at bottom" + redoStack.list);
            System.out.println("sketch at bottom:" + sketch + "///////////");
            System.out.println("grouped at bottom:" + sketch.grouped);
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Editor();
            }
        });
    }
}