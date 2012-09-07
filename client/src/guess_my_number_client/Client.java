package guess_my_number_client;

/** A simple implementation of a (UDP) client handling
 *  parallell and asynchronous input (receive) and output (send).
 *  User input (send):
 *  When the user prints a text in a text field a datagram packet 
 *  is sent by the event dispatch thread in the class ClientUDP.
 *  Input from socket (receive):
 *  Datagrams are received by a separate thread in the class Receiver,
 *  and displayded in a text area.
 *
 *  Test this client can be tested together with the EchoServerUDP, port 4950.
 *
 *  Usage: java ClientUDP servername
 */

import java.net.*;
import java.io.*;

public class Client {
	
	private int port = 4950;
	private InetAddress iaddr;
	private DatagramSocket socket;

	public Client(String server, int port) throws IOException {
		iaddr = InetAddress.getByName(server);
		this.port = port;
		socket = new DatagramSocket();
		handshake();
	}

	private void playing() throws IOException {
		System.out.println("Playing game");
		boolean running = true;
		BufferedReader inFromUser =
				new BufferedReader(new InputStreamReader(System.in));

		while (running) {
			byte[] data = new byte[1024];
			String sentence = new String(inFromUser.readLine());

			if (sentence.equalsIgnoreCase("QUIT")) {
				System.out.println("Client terminated.");
				System.exit(0);
			} else if (sentence.equalsIgnoreCase("HANDSHAKE")) {
				handshake();
			}

			data = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, iaddr, port);
			socket.send(sendPacket);

			data = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("<server> " + modifiedSentence);
		}
		socket.close();
	}

	private void handshake() {
		send("HELLO");
		
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String modifiedSentence = new String(packet.getData());
		System.out.println("<server> " + modifiedSentence);
		
		send("START");
		packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		modifiedSentence = new String(packet.getData());
		System.out.println("<server> " + modifiedSentence);
	}

	/**
	 * Send a datagram packet.
	 */
	public void send(String msg) {
		System.out.println("<client> " + msg);
		
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

	public static void main(String[] args) throws IOException {
		Client client = null;
		try {
			client = new Client("localhost", 4950);
		} catch (Exception e) {
			System.out.println("Exception while initializing, " + e.toString());
		}

		client.playing();
	}
}