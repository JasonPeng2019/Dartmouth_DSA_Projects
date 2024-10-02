package src;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {

	String flag = "false";



	private Socket sock;                    // to talk with client
	private BufferedReader in;                // from client
	private PrintWriter out;                // to client
	private SketchServer server;            // handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
		System.out.println("WTFFFFFFF"+server.undoStack.list.size());
		System.out.println("UNDO LIST AT THE START OF ALL THIS:"+server.undoStack.list);
	}

	/**
	 * Sends a message to the client
	 *
	 * @param msg
	 */
	public synchronized void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			System.out.println("someone connected");

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			System.out.println("sketch at build:" + server.getSketch());
			if (!server.undoStack.isEmpty()) {
				System.out.println("UNDOSTACK top: " + server.undoStack.peek());
			}
			ArrayList<Sketch> undoStackTracker = new ArrayList<>();
			ArrayList <Sketch> redoStackTracker = new ArrayList<>();

			String undoStackString = "";
			while (!server.undoStack.isEmpty()){
				System.out.println("UNDOSTACK LIST"+server.undoStack.list);
				System.out.println("UNDOSTACK LIST SIZE" + server.undoStack.list.size());
				Sketch newSketch = server.undoStack.pop();
				undoStackString += (newSketch+"!!");
				undoStackTracker.add(newSketch);
				System.out.println("Undo stack string in between itnerations:"+undoStackString);
			}
			String redoStackString = "";
			while (!server.redoStack.isEmpty()){
				Sketch newSketch = server.redoStack.pop();
				redoStackString += (newSketch+"!!");
				redoStackTracker.add(newSketch);
			}
			System.out.println("undo tracker:"+undoStackTracker);
			for (int i = undoStackTracker.size()-1; i>=0; i--){
				server.undoStack.push(undoStackTracker.get(i));
			}
			System.out.println("redo tracker:"+redoStackTracker);
			for (int i = redoStackTracker.size()-1; i>=0; i--){
				server.redoStack.push(redoStackTracker.get(i));
			}

			if (!server.undoStack.isEmpty()) {
				System.out.println("UNDOSTACK peek after rebuilding: " + server.undoStack.peek());
			}
			String submitString = "sketch->" + server.getSketch().toString() + "->undoStack->" + undoStackString + "->redoStack->" + redoStackString;
			System.out.println("submitString.length"+submitString.split("->", -1).length);
			System.out.println(submitString);
			send(submitString);
			send("buildGrouped->");

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("----------------NEW COMMAND INCOME-----------------------------------");
				Sketch sketch = server.getSketch();
				String inputString = line;
				String[] stringArray = inputString.split("->");
				System.out.println("received: " + line);
				// parse the data to determine instruction type
				System.out.println("--------------------------------------------------------------");
				System.out.println("INPUT STRING:::: "+ inputString);
				System.out.println("undoStack at start"+server.undoStack.list + "| size" + server.undoStack.list.size());
				System.out.println("redoStack at top" +server.redoStack.list + "size" + server.redoStack.list.size());

				if (inputString.split("->").length>1) {
					System.out.println("sketch at top" + sketch);
					if (inputString.split("->")[0].equals("undo")) {
						System.out.println("grouped at top:" + sketch.grouped);
					}
					if (!server.undoStack.isEmpty()) {
						System.out.println("peek undo at top" + server.undoStack.peek());
					}
					if (!stringArray[0].equals("redo") || !server.redoStack.isEmpty()) {
						Editor.addUndo(stringArray, flag, server.undoStack, sketch);
					}
					if (!stringArray[0].equals("undo") && !stringArray[0].equals("redo")){
						server.redoStack = Editor.clearRedo();
					}
					// build the local sketch tree map
					System.out.println("sketch after addundo:" + sketch);
					if (!server.undoStack.isEmpty()) {
						System.out.println("peek at addundo" + server.undoStack.peek());
					}
					if (stringArray[0].equals("newShape")) {
						System.out.println("step3:" + sketch.getShapeMap());
						String shapeString = stringArray[1];
						Shape inShape = Editor.addShape(shapeString);
						System.out.println("size of sketch:" + sketch.getSize());
						sketch.addShape(inShape);
						System.out.println(shapeString);
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
					}
					// delete a clicked shape in the local tree map
					else if (stringArray[0].equals("delete")) {
						Editor.delete(sketch, stringArray, sketch.grouped);
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
							flag = Editor.setFlagFalse(flag);
						}
					} else if (stringArray[0].equals("moveToFront")) {
						Editor.moveToFront(sketch, stringArray, sketch.grouped);
						System.out.println(sketch.getShapeMap());
					} else if (stringArray[0].equals("moveToBack")) {
						Editor.moveToBack(sketch, stringArray, sketch.grouped);
						System.out.println(sketch.getShapeMap());
					} else if (stringArray[0].equals("group")) {
						if (stringArray.length > 1) {
							Editor.group(stringArray, sketch.grouped);
						}
					} else if (stringArray[0].equals("ungroup")) {
						if (stringArray.length > 1) {
							Editor.ungroup(stringArray, sketch.grouped);
						}
					} else if (stringArray[0].equals("undo")) {
						if (!server.undoStack.isEmpty()) {
							System.out.println("undid");
							sketch = Editor.undo(sketch, server.undoStack, server.redoStack);
							server.setSketch(sketch);
						}
					}else if (stringArray[0].equals("redo")) {
						if (!server.redoStack.isEmpty()) {
							sketch = Editor.redo(server.redoStack);
							server.setSketch(sketch);
						}
					}
					if (!server.undoStack.isEmpty()) {
						System.out.println("peek at bottom" + server.undoStack.peek());
					}
					if ((stringArray[0].equals("group") || stringArray[0].equals("ungroup"))&&flag.equals("true")){
						Editor.addUndo(stringArray,"false", server.undoStack, sketch);
						System.out.println("ADDED UNDOSTACK FOR MOVING SPECIAL CASE");
					}
					System.out.println("undostack at bottom:" + server.undoStack.list);
					System.out.println("redoStack at bottom" + server.redoStack.list);
					System.out.println("sketch at bottom:" + sketch + "///////////");
					System.out.println("grouped at bottom:" + sketch.grouped);
					server.broadcast(line);
				}
			}



			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();

		}
		catch(Exception e){
				e.printStackTrace();
		}
	}
}


