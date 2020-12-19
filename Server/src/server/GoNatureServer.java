// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.ServerMain;
import entity.Employee;
import message.ClientMessage;
import message.ServerMessage;
import message.ServerMessageType;
import  mysql.MySQLConnection;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */

public class GoNatureServer extends AbstractServer {
	// Class variables *****************

	/**
	 * The default port to listen on.
	 */
	// final public static int DEFAULT_PORT = 5555;

	// Constructors ******************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 * 
	 */
	private MySQLConnection goNatureDB;
	private ArrayList<Object> userList;
	public GoNatureServer(int port) {
		super(port);
		userList=new ArrayList<>();
	}

	// Instance methods ****************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 * @param
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Object returnVal=null;
		ServerMessageType type=null;
		try {
			if(msg instanceof ClientMessage) {
				ClientMessage clientMsg=(ClientMessage)msg;
				switch(clientMsg.getType()) {
				case DISCONNECTED:
					if(clientMsg.getMessage()!=null)
						userList.remove(clientMsg.getMessage());
					ServerMain.guiController.disconnectClient(client);
					break;
				case LOGIN_VISITOR:
					returnVal=MySQLConnection.validateVisitor((String)(clientMsg.getMessage()));
					type=ServerMessageType.LOGIN;
					if(userList.contains(returnVal)) //user already logged in
						returnVal="logged in";
					else if(returnVal!=null) //user isn't already logged in and was found in the database
						userList.add(returnVal);
					break;
				case LOGIN_SUBSCRIBER:
					returnVal=MySQLConnection.validateSubscriber((String)(clientMsg.getMessage()));
					type=ServerMessageType.LOGIN;
					if(userList.contains(returnVal))
						returnVal="logged in";
					else if(returnVal!=null)
						userList.add(returnVal);
					break;
				case LOGIN_EMPLOYEE:
					returnVal=MySQLConnection.validateEmployee((String[])(clientMsg.getMessage()));
					type=ServerMessageType.LOGIN;
					if(userList.contains(returnVal))
						returnVal="logged in";
					else if(returnVal!=null)
						userList.add(returnVal);
					break;
				case CONNECTION:
					break;
				}		
			}
		}catch(Exception e) {try {
			client.sendToClient(new ServerMessage(ServerMessageType.SERVER_ERROR,null));
		} catch (IOException e1) {}}
		
		System.out.println("Message received: " + msg+ " from " + client);
		try {
			client.sendToClient(new ServerMessage(type,returnVal));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {
		ServerMain.guiController.connectClient(client);
	}

	@Override
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		ServerMain.guiController.disconnectClient(client);
	}
}
//End of EchoServer class