/**
 * Guess my number game - Client
 *
 * Communication Systems, HI1032
 * Lab assignment 3 - Client-Server programming
 *
 * Simon Kers skers@kth.se
 * Sakib Pathan sakibp@kth.se
 *                                 KTH STH 2012
 */

package guess_my_number_client;

import java.net.*;
import java.io.*;

public class Client {

	private static final String DEFAULT_SERVER = "localhost";
	private static final int DEFAULT_SERVER_PORT = 4950;
	private InetAddress iaddr;
	private DatagramSocket socket;

	public Client(String server, int port) throws IOException {
		this.iaddr = InetAddress.getByName(server);
		this.socket = new DatagramSocket();
		handshake();
	}

	/**
	 * Plays a round of guess my number with the server.
	 */
	private void playing() throws IOException {
		boolean running = true;
		BufferedReader inFromUser =
				new BufferedReader(new InputStreamReader(System.in));

		socket.setSoTimeout(20000);
		
		while (running) {
			System.out.printf("<client> ");
			String sentence = new String(inFromUser.readLine());

			if (sentence.equalsIgnoreCase("QUIT")) {
				send("QUIT");
				System.out.println("Client terminated.");
				System.exit(0);
			} else if (sentence.equalsIgnoreCase("HANDSHAKE")) {
				handshake();
			} else {
				send(sentence);

				byte[] data = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(data, data.length);
				try {
					socket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String plaintext = new String(receivePacket.getData());
				System.out.println("<server> " + plaintext);
				
				if (plaintext.trim().equals("CORR")) {
					System.out.println("  -!-    Game over");
					socket.close();
					return;
				}
			}
		}
		socket.close();
	}

	/**
	 * Perform a handshake with the server to establish a connection.
	 */
	private void handshake() {
		System.out.printf("<client> ");
		System.out.println("HELLO");
		send("HELLO");

		// Receive packet
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		try {
			socket.receive(packet);
		} catch (SocketTimeoutException ste) {
			System.out.printf(".");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String plaintext = new String(packet.getData());
		System.out.println("<server> " + plaintext);

		if (plaintext.trim().equals("OK")) {
			System.out.printf("<client> ");
			System.out.println("START");
			send("START");
			packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			plaintext = new String(packet.getData());
			System.out.println("<server> " + plaintext);
		} else {
			System.out.println("Handshake failed");
			System.exit(-1);
		}
	}

	/**
	 * Send a datagram packet.
	 */
	public void send(String msg) {
		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, iaddr, DEFAULT_SERVER_PORT);
		try {
			socket.send(packet);
		} catch (IOException ie) {
			System.out.println("IOException: " + ie.toString());
		}
	}

	public static void main(String[] args) throws IOException {
		Client client = null;
		try {
			client = new Client(DEFAULT_SERVER, DEFAULT_SERVER_PORT);
		} catch (Exception e) {
			System.out.println("Exception while initializing, " + e.toString());
		}

		client.playing();
	}
}