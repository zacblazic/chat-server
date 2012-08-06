package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import client.nonauthenticated.RequestHandler;

public final class ConnectionHandler extends Thread
{
	private final Server server;
	private final ServerSocket serverSocket;
	private final ClientList clientList;
	private Socket clientSocket;
	
	public ConnectionHandler(Server server, ServerSocket serverSocket, ClientList clientList)
	{
		this.server = server;
		this.serverSocket = serverSocket;
		this.clientList = clientList;
	}
	
	public void run()
	{		
		while(server.isAcceptingConnections())
		{
			try
			{
				clientSocket = serverSocket.accept();	
				new RequestHandler(clientList, clientSocket).start();
			}
			catch(IOException ioe)
			{
				System.err.println(ioe.toString());
			}
		}
	}
}
