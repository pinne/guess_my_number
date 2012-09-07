package guess_my_number_server;

/**
 */

import java.net.*;
import java.util.StringTokenizer;
import java.io.*;

public class GuessMyNumberServer {

	public static final int SERVER_PORT = 4950;
	public static final int MAXBUF = 1024;

	private int port = 4950;
	private InetAddress iaddr;
	private DatagramSocket socket;
	
	public static void main(String[] args) {
		DatagramSocket sock = null;
		String message = "";
		boolean running = true;
		boolean handshake = false;
		boolean playing = false;
		int LOWEST  = 0;
		int HIGHEST = 100;
		GuessEngine fsm = new GuessEngine(LOWEST, HIGHEST);

		try {
			// Bind to the specified port
			sock = new DatagramSocket(SERVER_PORT);
			DatagramPacket packet;

			while (running) {
				byte[] data = new byte[MAXBUF];
				packet = new DatagramPacket(data, data.length);
				sock.receive(packet);

				// Print message
				InetAddress clientAddr = packet.getAddress();
				int clientPort = packet.getPort();

				// Extract the part of the byte array containing the message
				message = new String(packet.getData(), 0, packet.getLength()); 
				System.out.println("Packet received from " + clientAddr.getHostName());
				System.out.println("Packet contains \"" + message + "\"");

				// Handshake
				if (!playing) {
					if (message.toUpperCase().charAt(0) == 'H'
							&& handshake == false) {
						// Client says HELLO
						message = "OK";
						handshake = true;
					} else if (message.toUpperCase().charAt(0) == 'S'
							&& handshake == true) {
						// Client says START
						fsm = new GuessEngine(LOWEST, HIGHEST);
						message = "READY";
						handshake = false;
						playing = true;
					} else {
						// Client says something else
						message = "ERROR";
						playing = false;
						handshake = true;
					}
				} else if (playing && !handshake) {
					switch (message.toUpperCase().charAt(0)) {
					// Client says QUIT
					case 'Q':
						message = "ECHO SESSION TERMINATED.";
						playing = false;
						break;
						
					// Client guesses
					case 'G':
						// Check last part of string and extract the number
						String guessNumber = new String();
						StringTokenizer st = new StringTokenizer(message);
						guessNumber = st.nextToken(); // First word
	
						if (st.hasMoreTokens())	
							guessNumber = st.nextToken(); // Second word
						else
							break;

						fsm.setGuess(Integer.parseInt(guessNumber));

						// Write distance between secret number and guess.
						System.out.println(fsm.getPosition());
						String tmp = new String();
						message = fsm.toString();
						System.out.println(message);

						// Correct guess!
						if (fsm.isCorrect()) {
							fsm.genSecretNumber(LOWEST, HIGHEST);
							playing = false;
							handshake = true;
						}
						break;

					// Client says something else
					default:
						System.out.println("ERROR");
						message = "ERROR";
						break;
					}
				}

				// Send the answer to the client in a UDP packet.
				data = new byte[MAXBUF];
				data = message.getBytes();
				packet = new DatagramPacket(data, data.length, clientAddr, clientPort);
				sock.send(packet);
			}
		} catch (IOException ie) {
			System.out.println(ie.toString());
		} finally {
			sock.close();
		}
	}
	
	/**
	 * Send a datagram packet.
	 */
	public void send(String msg) {
		if (msg.equalsIgnoreCase("QUIT"))
			System.exit(0);

		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, iaddr, port);
		try {
			socket.send(packet);
		} catch (IOException ie) {
			System.out.println("IOException: " + ie.toString());
		}
	}
}