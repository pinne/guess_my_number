package guess_my_number_server;

import java.io.IOException;
import java.net.*;

public class Session {

	private DatagramSocket clientSocket = null;
	private static final int SERVER_PORT = 4950;
	private static final int MAXBUF = 1024;
	private int clientPort;
	private InetAddress clientAddr;
	private boolean connection = false;
	private Game game;

	// Constructor
	public Session() {
		this.game = new Game(this);
	}

	/**
	 * Compare the source port in the packet to the current client.
	 */
	private boolean authorizedClient(DatagramPacket packet) {
		if (packet.getPort() == clientPort)
			return true;
		else
			return false;
	}

	/**
	 * Establish a connection with a connecting client.
	 * 
	 */
	public void handshake() throws IOException, SocketTimeoutException {
		connection = false;
		this.clientSocket = new DatagramSocket(SERVER_PORT);

		byte[] data = new byte[MAXBUF];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		clientSocket.setSoTimeout(2000);
		try {
			clientSocket.receive(packet);
		} catch (SocketTimeoutException ste) {
			throw ste;
		}
		
		this.clientAddr = packet.getAddress();
		this.clientPort = packet.getPort();

		// Extract the part of the byte array containing the message
		String message = new String(packet.getData(), 0, packet.getLength()); 
		System.out.printf("Packet contains \"" + message + "\"");
		System.out.println(" from " + clientAddr.getHostName() + " : " + clientPort);

		if (message.equals("HELLO")) {
			send("OK");

			System.out.println("Continuing handshake");
			data = new byte[MAXBUF];
			packet = new DatagramPacket(data, data.length);

			this.clientSocket.receive(packet);
			this.clientAddr = packet.getAddress();
			this.clientPort = packet.getPort();

			// Extract the part of the byte array containing the message
			message = new String(packet.getData(), 0, packet.getLength()); 
			System.out.printf("Packet contains \"" + message + "\"");
			System.out.println(" from " + clientAddr.getHostName());

			if (message.equals("START")) {
				// Success!
				connection = true;
				send("READY");
				return;
			}
		}
		// Fail!
		send("Handshake failed");
		return;
	}

	/**
	 * Receive message from client and put it into the game engine,
	 * then send the result from the game engine to client.
	 */
	public void playGame() throws IOException, SocketTimeoutException {
		while (!game.isCorrect()) {
			// Receive message and request answer from game engine
			byte[] data = new byte[MAXBUF];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			clientSocket.setSoTimeout(20000);
			try {
				clientSocket.receive(packet);
			} catch (SocketTimeoutException ste) {
				send("Disconnected because of timeout");
				close();
				throw ste;
			}

			if (!authorizedClient(packet)) {
				System.out.println("Illegal connection attempt from " + packet.getAddress());
				replyBusy(packet);
			} else {
				// Extract the part of the byte array containing the message
				String message = new String(packet.getData(), 0, packet.getLength()); 
				System.out.printf("Packet contains \"" + message + "\"");
				System.out.println(" from " + clientAddr.getHostName());
				
				if (message.equalsIgnoreCase("QUIT"))
					break;

				send(game.parse(message));
			}
		}
		close();
		connection = false;
	}

	/**
	 * Send a datagram packet.
	 */
	public void send(String msg) {
		System.out.println("<server> " + msg);
		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, clientAddr, clientPort);
		try {
			clientSocket.send(packet);
		} catch (IOException ie) {
			System.out.println("IOException: " + ie.toString());
		}
	}
	
	/**
	 * Extract the sender from the packet and reply busy.
	 */
	private void replyBusy(DatagramPacket packet) {
		InetAddress replyAddr = packet.getAddress();
		int replyPort = packet.getPort();
		String replyMessage = "BUSY";
		byte[] data = replyMessage.getBytes();
		DatagramPacket replyPacket = new DatagramPacket(data, data.length, replyAddr, replyPort);
		try {
			clientSocket.send(replyPacket);
		} catch (IOException ie) {
			System.out.println("IOException: " + ie.toString());
		}
	}

	public boolean isConnected() {
		return connection;
	}

	/**
	 * Close current connection.
	 */
	public void close() {
		if (isConnected())
			send("DISCONNECTED");
		
		connection = false;
		clientSocket.close();
	}
}
