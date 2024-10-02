package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor2 extends JFrame {

    private String flag = "false";
    private static String serverIP = "localhost";			// IP address of sketch server
    // "localhost" for your own machine;
    // or ask a friend for their IP address

    private static final int width = 800, height = 800;		// canvas size

    // Current settings on GUI
    public enum Mode {
        DRAW, MOVE, RECOLOR, DELETE
    }
    private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
    private String shapeType = "ellipse";		// type of object to add
    private Color color = Color.black;			// current drawing color

    // Drawing state
    // these are remnants of my implementation; take them as possible suggestions or ignore them
    private Shape curr = null;					// current shape (if any) being drawn
    private Sketch sketch;						// holds and handles all the completed objects
    private int movingId = -1;					// current shape id (if any; else -1) being moved
    private Point drawFrom = null;				// where the drawing started
    private Point moveFrom = null;				// where object is as it's being dragged


    // Communication
    private EditorCommunicator2 comm;			// communication with the sketch server

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
                handlePress(event.getPoint());
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
        shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

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
                e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
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
        ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
        modes.add(drawB);
        modes.add(moveB);
        modes.add(recolorB);
        modes.add(deleteB);
        JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
        modesP.add(drawB);
        modesP.add(moveB);
        modesP.add(recolorB);
        modesP.add(deleteB);

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
        if (curr!= null) {curr.draw(g); }
        for(int s : sketch.getShapeMap().keySet()) {
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
    private void handlePress(Point p) {
        // TODO: YOUR CODE HERE
        if (mode == Editor2.Mode.DRAW) { // create new shape
            drawFrom = new Point(p.x, p.y);
            if (shapeType.equals("ellipse")) {
                curr = new Ellipse(drawFrom.x, drawFrom.y, color);
            } else if (shapeType.equals("freehand")) {
                curr = new Polyline(p.x, p.y, color);
            } else if (shapeType.equals("rectangle")) {
                curr = new Rectangle(drawFrom.x, drawFrom.y, color);
            } else if (shapeType.equals("segment")) {
                curr = new Segment(drawFrom.x, drawFrom.y, color);
            }

        } else if (mode == Editor2.Mode.MOVE) {
            drawFrom = new Point(p.x, p.y);
            Map<Integer, Shape> tempShaps = sketch.getShapeMap();
            for (Integer i: tempShaps.keySet()) {
                System.out.println(tempShaps);
                System.out.println("i"+i);
                if(tempShaps.get(i).contains(p.x, p.y)) {
                    moveFrom = new Point(p.x, p.y);
                    movingId = i;
                }
            }
            if (movingId != -1){
                flag = "true";
            }
        } else if (mode == Editor2.Mode.RECOLOR) {
            Map<Integer, Shape> tempShaps = sketch.getShapeMap();
            for (int i: tempShaps.keySet()) {
                if (tempShaps.get(i).contains(p.x, p.y)) {
                    tempShaps.get(i).setColor(color);
                    repaint();
                    comm.send("recolor->" + i + ":" + color.getRGB());
                    break; // only remove the most recent shape (ie. the top layer)
                }
            }
        } else if (mode == Editor2.Mode.DELETE) {
            int delID = -1;
            for (int i = sketch.getSize() - 1; i >= 0;  i--) {
//				if (sketch.getShapeMap().containsKey(i) && ) might be used later? unsure
                if (sketch.getShapeMap().get(i).contains(p.x, p.y)) {
                    delID = i;
                    break; // only remove the most recent shape (ie the top layer)
                }
            }
            if (delID != -1) {
                sketch.remove(delID);
                comm.send("delete->" + delID);
                repaint();
            }
        }
    }

    /**
     * Helper method for drag to new point
     * In drawing mode, update the other corner of the object;
     * in moving mode, (request to) drag the object
     */
    private void handleDrag(Point p) {
        // TODO: YOUR CODE HERE
        if (mode == Editor2.Mode.DRAW) { // change shape
            if (shapeType.equals("ellipse")) {
                ((Ellipse)curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            } else if (shapeType.equals("freehand")) {
                ((Polyline) curr).addPoint(p.x, p.y);
            } else if (shapeType.equals("rectangle")) {
                ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
            } else if (shapeType.equals("segment")) {
                ((Segment) curr).setEnd(p.x, p.y);
            }
            repaint();

        } else if (mode == Editor2.Mode.MOVE) {
            if (movingId!= -1) {
                sketch.getShapeMap().get(movingId).moveBy(p.x - moveFrom.x, p.y - moveFrom.y);
                moveFrom = new Point(p.x, p.y);
                repaint();
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
        if (mode == Editor2.Mode.DRAW) {
            comm.send("newShape->" + curr);
            curr = null;

            repaint();
        }
        else if (mode == Editor2.Mode.MOVE) {
            Point tempForStart = drawFrom;
            drawFrom = moveFrom;
            comm.send("move->" + movingId + ":" + drawFrom.x + "," + drawFrom.y + "," +
                    tempForStart.x + "," + tempForStart.y);
            movingId = -1;


            repaint();
        }
    }

    public static Ellipse addEllipse(String[] coColorList){
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Ellipse(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));
    }

    public static Rectangle addRectangle(String[] coColorList){
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Rectangle(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));
    }

    public static Polyline addPolyline(String[] coColorList){
        int color = Integer.parseInt(coColorList[0]);
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        ArrayList<Integer> coorList = new ArrayList<>();
        for (int i=3; i < coColorList.length; i++) {
            coorList.add(Integer.parseInt(coColorList[i]));
        }
        Polyline polyline = new Polyline(x1Cor, y1Cor, new Color(color));
        for (int i=0; i<coorList.size(); i+=2) {
            int yCor = i+1;
            polyline.addPoint(coorList.get(i), coorList.get(yCor));
        }
        return polyline;
    }

    public static Segment addSegment(String [] coColorList){
        int x1Cor = Integer.parseInt(coColorList[1]);
        int y1Cor = Integer.parseInt(coColorList[2]);
        int x2Cor = Integer.parseInt(coColorList[3]);
        int y2Cor = Integer.parseInt(coColorList[4]);
        int color = Integer.parseInt(coColorList[0]);

        return new Segment(x1Cor, y1Cor, x2Cor, y2Cor, new Color(color));

    }

    public synchronized static Shape addShape(String shapeString){
        String [] shapeArray = shapeString.split(":");
        String shapeType = shapeArray[0];
        String [] coColorList = shapeArray[1].trim().split(",");
        Shape inShape = null;
        if (shapeType.equals("ellipse")){
            inShape = addEllipse(coColorList);
        } else if (shapeType.equals("rectangle")){
            inShape = addRectangle(coColorList);
        } else if (shapeType.equals("polyline")){
            inShape = addPolyline(coColorList);
        } else if (shapeType.equals("segment")){
            inShape = addSegment(coColorList);
        }
        return inShape;
    }


    /**
     * note to grader: considered receiving just the sketch and rebuilding the entire sketch map from scratch
     * but that would have a runtime of nlogn, while updating the specific shape after a recieved command
     * has a runtime of log(n). So despite needing more code for the 2nd option, went with 1st.
     * @param inputString
     */
    public void receiveString(String inputString) {
        String[] stringArray = inputString.split("->");
        if (stringArray[0].equals("sketch") && stringArray.length != 1 ) {
            String[] idAndShapesArray = stringArray[1].split("&&");
            for (String idAndShape : idAndShapesArray) {
                int id = Integer.parseInt(idAndShape.split("/")[0]);
                String shapeString = idAndShape.split("/")[1];
                Shape inShape = addShape(shapeString);
                sketch.addShape(inShape);
                repaint();
            }
        } else if (stringArray[0].equals("newShape")) {
            String shapeString = stringArray[1];
            Shape inShape = addShape(shapeString);
            sketch.addShape(inShape);
            repaint();
        } else if (stringArray[0].equals("recolor")){
            String[] iDAndColor = stringArray[1].split(":");
            int id = Integer.parseInt(iDAndColor[0].trim());
            String colorString = iDAndColor[1].trim(); // e.g., "16777215" for white
            int rgb = Integer.parseInt(colorString);
            Color color = new Color(rgb);
            sketch.getShapeMap().get(id).setColor(color);
            repaint();
        }
        else if (stringArray[0].equals("delete")){
            sketch.getShapeMap().remove(Integer.parseInt(stringArray[1].trim()));
            repaint();
        } else if (stringArray[0].equals("move")){
            String [] moveString = stringArray[1].trim().split(":");
            int movingID = Integer.parseInt(moveString[0].trim());
            String [] coordinateString = moveString[1].trim().split(",");
            int corX = Integer.parseInt(coordinateString[0]);
            int corY = Integer.parseInt(coordinateString[1]);
            int startCorX = Integer.parseInt(coordinateString[2]);
            int startCorY = Integer.parseInt(coordinateString[3]);
            if (flag.equals("false")) {
                sketch.getShapeMap().get(movingID).moveBy(corX - startCorX, corY - startCorY);
            } else if (flag.equals("true")){
                flag = "false";
            }
            repaint();
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