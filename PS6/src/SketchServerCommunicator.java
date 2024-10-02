package src;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;                    // to talk with client
	private BufferedReader in;                // from client
	private PrintWriter out;                // to client
	private SketchServer server;            // handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
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
			send("sketch->" + server.getSketch().toString());

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("received: " + line);
				// parse the data to determine instruction type
				String[] stringArray = line.split("->");
				Sketch sketch = server.getSketch();
				if (stringArray[0].trim().equals("newShape")) {
					// add a new shape to the server-side sketch map
					if (stringArray[0].equals("newShape")) {
						String shapeString = stringArray[1];
						Shape inShape = Editor.addShape(shapeString);
						sketch.addShape(inShape);
						server.broadcast(line);
					}
				}else if (stringArray[0].equals("move")){
					// move a shape in the server-side sketch map
					String [] moveString = stringArray[1].trim().split(":");
					int movingID = Integer.parseInt(moveString[0].trim());
					String [] coordinateString = moveString[1].trim().split(",");
					int corX = Integer.parseInt(coordinateString[0]);
					int corY = Integer.parseInt(coordinateString[1]);
					int startCorX = Integer.parseInt(coordinateString[2]);
					int startCorY = Integer.parseInt(coordinateString[3]);
					System.out.println(sketch.getShapeMap());
					System.out.println(movingID);
					sketch.getShapeMap().get(movingID).moveBy(corX-startCorX, corY-startCorY);
					server.broadcast(line);
				} else if (stringArray[0].equals("recolor")){
					// recolor a shape in the server-side sketch map
					String[] iDAndColor = stringArray[1].split(":");
					int id = Integer.parseInt(iDAndColor[0].trim());
					String colorString = iDAndColor[1].trim(); // e.g., "16777215" for white
					int rgb = Integer.parseInt(colorString);
					Color color = new Color(rgb);
					sketch.getShapeMap().get(id).setColor(color);
					server.broadcast(line);
				} else if (stringArray[0].equals("delete")){
					// delete a shape in the server-side sketch map
					Editor.delete(sketch, stringArray);
					server.broadcast(line);
				}
			}

				// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();

		}
		catch(IOException e){
				e.printStackTrace();
		}
	}
}


