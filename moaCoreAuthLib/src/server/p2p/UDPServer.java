package moa.blockchain.server.p2p;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPServer extends Thread {
	private static final Logger logger = LoggerFactory.getLogger( UDPServer.class);
	private final int MESSAGESIZE = 256;
	private DatagramSocket socket;
	private boolean running;
	private byte[] buf;

	public UDPServer(Reply rp, int port) throws SocketException {
		udpReply = rp;
		socket = new DatagramSocket(port);
	}

	static Reply udpReply;

	public void run() {
		running = true;

		while (running) {
			buf = new byte[MESSAGESIZE];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				logger.error("socket receive error", e);
			}

			// get message, and trim unnecessary null byte;
			String received = new String(packet.getData());

			String rc = "";
			for (char c : received.toCharArray()) {
				if (c == '\0')
					break;
				rc += c;
			}
			received = rc;

			logger.info("received : " + received);
			
			packet = isValidJson(received);

			try {
				// send packet
				if (packet != null)
					socket.send(packet);
			} catch (IOException e) {
				logger.error("socket send error", e);
			}

		}
		socket.close();
	}

	static String replyToServer(String inputLine, Reply rp) {
		return rp.replyToClient(inputLine);
	}

	DatagramPacket isValidJson(String received) {
		DatagramPacket packet = null;
		try {
			// json parse
			JSONParser parser = new JSONParser();
			parser.parse(received);

			// set message
			replyToServer(received, udpReply);

			return packet;
		} catch (ParseException e) {
			logger.error("json parse error", e);
		}
		return packet;
	}

	public static void main(String[] args) throws SocketException {
		Reply foo = (inputLine) -> { // echo
			return Server.functionMatcher(inputLine);
		};
		new UDPServer(foo, 4445).start();
	}
}
