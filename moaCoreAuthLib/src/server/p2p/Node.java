package moa.blockchain.server.p2p;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.BigIntegerMath;

import moa.blockchain.common.account.Account;
import moa.blockchain.common.crypto.Base58;
import moa.blockchain.common.crypto.Crypto;

class Coordinate {
	double latitude = 0.0;
	double longitude = 0.0;
}

public class Node {
	Coordinate coordinate;
	double latitude = 0.0;
	double longitude = 0.0;

	final String ipAddress;
	final Integer port;
	final String nodeId;

	BigInteger blockHeight = BigInteger.ZERO;

	public Node(String ipAddress, Integer port, String nodeId) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.nodeId = nodeId;
	}

	public Node(JSONObject nodeJson) {
		this.ipAddress = nodeJson.get("ipAddress").toString();
		this.port = Integer.valueOf(nodeJson.get("port").toString());
		this.nodeId = nodeJson.get("nodeId").toString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject node = new JSONObject();

		node.put("ipAddress", this.ipAddress);
		node.put("port", this.port);
		node.put("nodeId", this.nodeId);

		return node;
	}

	public boolean validNodeCheck() {
		return Crypto.getInstance().isValidAddress(this.nodeId);
	}

	void sync(RouteTable routeTable) {
		// TODO
		// block height
		// block total count
		// last block
		Ping ping = new Ping(this);
		routeTable.getInnerNodeValues().forEach((node) -> {
			ping.udp(node.ipAddress, node.port);
		});
	}
}

class RouteTable {
	private static final Logger logger = LoggerFactory.getLogger(RouteTable.class);
	final static int DIVIDE_COUNT = 3;
	private HashMap<Integer, Node> nodeOuterTable = new HashMap<Integer, Node>(); // (index, node)
	private HashMap<Node, Integer> nodeInnerTable = new HashMap<Node, Integer>(); // (node, responseTime)
	private Node selfNode;

	public RouteTable(Node selfNode) {
		this.selfNode = selfNode;
	}
	/*
	Node search(Coordinate replyedCoordinate) {
		// TODO 1 FindNode
		Coordinate c1 = this.selfNode.coordinate;
		Coordinate c2 = replyedCoordinate;

		Integer idx = getSubtreeIndex(c1, c2);

		if (idx == DIVIDE_COUNT) {
			/*
			 * if (this.nodeInnerTable.get(idx) != null) {
			 * System.out.println("subtree exist");
			 * 
			 * return this.nodeTable.get(idx); } else {
			 * System.out.println("subtree not exist"); // TODO return null; }
			 *
		} else {
			if (this.nodeOuterTable.get(idx) != null) {
				logger.info("subtree exist");

				return this.nodeOuterTable.get(idx);
			} else {
				logger.info("subtree not exist");
				// TODO
				return null;
			}
		}

	}
	*/
	void regist(Node replyedNode) {
		logger.info("RouteTable node valide : " + replyedNode.validNodeCheck());
		if (!replyedNode.validNodeCheck()) {
			return;
		}
		if (replyedNode == this.selfNode) {
			logger.info("RouteTable replyedNode is itself");
			return;
		}

		Coordinate c1 = this.selfNode.coordinate;
		Coordinate c2 = replyedNode.coordinate;

		Integer idx = getSubtreeIndex(c1, c2);
		if (idx < 0) {
			return;
		} else if (idx == DIVIDE_COUNT) {
			logger.info("RouteTable current Index : innerTable " + idx);
			if (!this.nodeInnerTable.containsKey(replyedNode)) {
				logger.info("RouteTable current value not exist");

				this.nodeInnerTable.put(replyedNode, 10);// TODO ADD RESPONSE TIME AT 10
				logger.info("subtree updated");
			} else {
				logger.info("RouteTable value already exist");
			}
		} else {
			logger.info("RouteTable current Index : outerTable " + idx);
			if (this.nodeOuterTable.get(idx) == null) {
				logger.info("RouteTable current Index of Subtree is empty");

				this.nodeOuterTable.put(idx, replyedNode);
				logger.info("subtree updated");
			} else if (this.nodeOuterTable.containsKey(idx)) {
				// TODO do something to disperse server cohesion
			}
		}
	}

	//
	Integer getSubtreeIndex(Coordinate srcCoordinate, Coordinate dstCoordinate) {
		int rt = -1;

		int indexX = longitudeToIndex(srcCoordinate.longitude, srcCoordinate.longitude);
		int indexY = latitudeToIndex(srcCoordinate.latitude, srcCoordinate.latitude);

		int min = Math.min(indexX, indexY);
		// if equal or grater then 0, sign is 0,
		// else, 1
		int signX;
		int signY;

		if (srcCoordinate.longitude - dstCoordinate.longitude >= 0) {
			signX = 0;
		} else {
			signX = 1;
		}
		if (srcCoordinate.latitude - dstCoordinate.latitude >= 0) {
			signY = 0;
		} else {
			signY = 1;
		}

		// if equal to min, isMin is 0,
		// else, 1
		int isMinX;
		int isMinY;

		if (min == indexX) {
			isMinX = 0;
		} else {
			isMinX = 1;
		}
		if (min == indexY) {
			isMinY = 0;
		} else {
			isMinY = 1;
		}

		// return
		rt = (12 * min) + (3 * (2 * signX + signY)) + (2 * isMinX + isMinY);
		return rt;
	}

	int latitudeToIndex(double srcLatitude, double dstLatitude) {
		int rt;
		final double maxLatitude = 90.0;

		double distence = Math.abs(srcLatitude - dstLatitude);
		if (distence == 0) {
			logger.info("at the same Latitude");
			rt = DIVIDE_COUNT;
			return rt;
		}
		rt = (int) (maxLatitude / Math.log(distence) / Math.log(2));

		return rt;
	}

	int longitudeToIndex(double srcLongitude, double dstLongitude) {
		int rt;
		final double maxLongitude = 180.0;

		double distence = Math.abs(srcLongitude - dstLongitude);
		if (distence == 0) {
			logger.info("at the same Longitude");
			rt = DIVIDE_COUNT;
			return rt;
		}
		rt = (int) (maxLongitude / Math.log(distence) / Math.log(2));

		return rt;
	}

	//
	/*
	 * Integer getSubtreeIndex(byte[] src, byte[] dst) { BigInteger distence =
	 * getXorDistence(src, dst); if (distence.equals(BigInteger.ZERO)) return -1;
	 * else return BigIntegerMath.log2(distence, RoundingMode.DOWN); }
	 * 
	 * BigInteger getXorDistence(byte[] src, byte[] dst) { BigInteger rt = null;
	 * 
	 * byte[] rtbytes = new byte[src.length]; for (int i = 0; i < rtbytes.length;
	 * i++) { rtbytes[i] = (byte) (src[i] ^ dst[i]); }
	 * 
	 * rt = new BigInteger(rtbytes);
	 * System.out.println("RouteTable node distence : " + rt);
	 * 
	 * return rt; }
	 */

	Node getOuterNode(Integer idx) {
		return this.nodeOuterTable.get(idx);
	}

	Node getInnerNode(Integer idx) {

		return ((Node[]) this.nodeInnerTable.keySet().toArray())[idx];
	}

	/*
	 * Collection<Node> getClosestNodeValues(int length){ if
	 * (this.nodeInnerTable.size() < length) { return null; } else {
	 * 
	 * return this.nodeInnerTable.keySet(); } }
	 */
	Collection<Node> getOuterNodeValues() {
		return this.nodeOuterTable.values();
	}

	Collection<Node> getInnerNodeValues() {

		return this.nodeInnerTable.keySet();
	}
}

enum ServerStatus {
	FALSE, SERVER_NOT_FOUND,

	OK,

	PING_RESPONSE,

	FIND_RESPONSE, FIND_RESPONSE_NODE_ITSELF, FIND_RESPONSE_NODE_NOT_FOUND
}

class Pong {
	private final String property = "pong";
	private final Node dstNode;
	private ServerStatus status;
	private BigInteger blockHeight;

	public Pong(Node dstNode, BigInteger blockHeight, ServerStatus status) {
		this.dstNode = dstNode;
		this.blockHeight = blockHeight;
		this.status = status;
	}

	static void receive(JSONObject pong, RouteTable routeTable, Node nodeSelf) {
		Node dstNode = new Node((JSONObject) pong.get("dstNode"));
		routeTable.regist(dstNode);

		// if dstNode.blockHeight is grater then nodeSelf.blockHeight, get
		// (nodeSelf.blockHeight+1) Block TODO
		BigInteger blockHeight = new BigInteger(pong.get("blockHeight").toString());

		if (blockHeight.compareTo(nodeSelf.blockHeight) > 0) {
			/*
			 * GetBlock gb = new GetBlock(dstNode,
			 * nodeSelf.blockHeight.add(nodeSelf.blockHeight.add(BigInteger.ONE)));
			 * 
			 */
		}
	}

	public String udp(String serverIp, int port) {
		UDPClient client = new UDPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject pong = new JSONObject();

		pong.put("property", this.property);
		pong.put("dstNode", this.dstNode.getJson());
		pong.put("status", this.status.toString());
		pong.put("blockHeight", this.blockHeight);

		return pong;
	}
}

class Ping {
	private final String property = "ping";
	private final Node srcNode;

	static void receive(JSONObject ping, RouteTable routeTable, Node nodeSelf) {
		Node srcNode = new Node((JSONObject) ping.get("srcNode"));

		// regist to table
		routeTable.regist(srcNode);

		// construct pong
		Pong pong = new Pong(nodeSelf, nodeSelf.blockHeight, ServerStatus.PING_RESPONSE);
		pong.getJson().toString();

		// set address and port
		String ip = srcNode.ipAddress;
		Integer port = srcNode.port;

		pong.udp(ip, port);
	}

	public Ping(Node srcNode) {
		this.srcNode = srcNode;
	}

	public String udp(String serverIp, int port) {
		UDPClient client = new UDPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	public String tcp(String serverIp, int port) {
		TCPClient client = new TCPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject ping = new JSONObject();

		ping.put("property", this.property);
		ping.put("srcNode", this.srcNode.getJson());

		return ping;
	}
}

class FindNode {
	// TODO 1
	private final String property = "findNode";
	private final Node srcNode;
	private final String dstNodeId;

	public FindNode(Node srcNode, String dstNodeId) {
		this.srcNode = srcNode;
		this.dstNodeId = dstNodeId;
	}

	static void receive(JSONObject find, RouteTable routeTable, Node nodeSelf) {
		Node srcNode = new Node((JSONObject) find.get("srcNode"));

		// regist to table
		routeTable.regist(srcNode);

		// search node from table
		String dstNodeId = find.get("dstNodeId").toString();
		Node searchedNode = null;//routeTable.search(dstNodeId);

		if (searchedNode == null) { // if not exist, nodeSelf is closest node from what you search
			// construct pong
			Pong pong = new Pong(nodeSelf, nodeSelf.blockHeight, ServerStatus.FIND_RESPONSE_NODE_NOT_FOUND);
			pong.getJson().toString();

			// set address and port
			String ip = srcNode.ipAddress;
			Integer port = srcNode.port;

			pong.udp(ip, port);
		} else if (searchedNode.nodeId == nodeSelf.nodeId) { // nodeSelf is what you search
			// construct pong
			Pong pong = new Pong(nodeSelf, nodeSelf.blockHeight, ServerStatus.FIND_RESPONSE_NODE_ITSELF);
			pong.getJson().toString();

			// set address and port
			String ip = srcNode.ipAddress;
			Integer port = srcNode.port;

			pong.udp(ip, port);
		} else { // need to travel more
			UDPClient client = new UDPClient();
			client.sendMessageToServer(searchedNode.ipAddress, searchedNode.port, find.toString());
		}
	}

	public String udp(String serverIp, int port) {
		UDPClient client = new UDPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject fn = new JSONObject();

		fn.put("property", this.property);
		fn.put("srcNode", this.srcNode.getJson());
		fn.put("dstNodeId", this.dstNodeId);

		return fn;
	}
}

class BlockExchange {
	// TODO 2
	void requireBlock() {

	}

	void responseRequire() {

	}

	void checkBlockIsValid() {

	}

	void loadFromDb() {

	}

	void storeToDb() {

	}

	void checkHashAlreadyExist() {

	}

	void requireHashNPreHash() {

	}

	void responseHashNPreHash() {
		
	}

}

class TransactionExchange {
	// TODO 3
	void requireTransaction() {

	}

	void responseRequire() {

	}

	void checkTransactionIsValid() {

	}

	void loadFromDb() {

	}

	void storeToDb() {

	}
}

class Toss {
	private final String property = "toss";
	private final Node bypassNode;
	private final JSONObject jsonPackage;

	public Toss(Node bypassNode, JSONObject jsonPackage) {
		this.bypassNode = bypassNode;
		this.jsonPackage = jsonPackage;
	}

	static void receive(JSONObject toss) {
		JSONObject bypassNode = (JSONObject) toss.get("bypassNode");

		// set address and port
		String ip = bypassNode.get("ipAddress").toString();
		Integer Port = Integer.valueOf(bypassNode.get("port").toString());

		UDPClient client = new UDPClient();
		client.sendMessageToServer(ip, Port, toss.get("jsonPackage").toString());
	}

	public String udp(String serverIp, int port) {
		UDPClient client = new UDPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject js = new JSONObject();

		js.put("property", this.property);
		js.put("bypassNode", this.bypassNode.getJson());
		js.put("jsonPackage", this.jsonPackage);

		return js;
	}
}

class Broadcast {
	// TODO
	private final String property = "broadcast";
	private final Integer bcIndex;
	private final JSONObject jsonPackage;

	public Broadcast(Integer bcIndex, JSONObject jsonPackage) {
		this.bcIndex = bcIndex;
		this.jsonPackage = jsonPackage;
	}

	public Broadcast(JSONObject broadcast) {
		this.bcIndex = Integer.valueOf(broadcast.get("bcIndex").toString());
		this.jsonPackage = (JSONObject) broadcast.get("jsonPackage");
	}

	static void receive(JSONObject broadcast, RouteTable routeTable, Node nodeSelf) {
		Broadcast bc = new Broadcast(broadcast);
		bc.udpBroadcast(routeTable);

		// send package to itself
		UDPClient client = new UDPClient();
		client.sendMessageToServer(nodeSelf.ipAddress, nodeSelf.port, bc.jsonPackage.toString());
	}

	public void udpBroadcast(RouteTable rtbl) {
		UDPClient client = new UDPClient();
		for (int i = bcIndex; i >= 0; i--) {
			Broadcast bc = new Broadcast(i - 1, this.jsonPackage);
			Node n = rtbl.getOuterNode(i);
			if (n != null) {
				client.sendMessageToServer(n.ipAddress, n.port, bc.getJson().toString());
			}
		}
	}

	public String udp(String serverIp, int port) {
		UDPClient client = new UDPClient();

		return client.sendMessageToServer(serverIp, port, getJson().toString());
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject js = new JSONObject();

		js.put("property", this.property);
		js.put("bcIndex", this.bcIndex);
		js.put("jsonPackage", this.jsonPackage);

		return js;
	}
}