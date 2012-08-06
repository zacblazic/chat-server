package client.authenticated;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import server.ClientList;

public final class Client 
{
	private final long userId;
	private final RequestHandler requestHandler;
	private final UpdateHandler updateHandler;
	private volatile boolean online;
	
	public Client(long userId, ClientList clientList, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in)
	{
		this.userId = userId;
		this.online = true;
		(this.requestHandler = new RequestHandler(this, clientList, clientSocket, out, in)).start();
		this.updateHandler = new UpdateHandler(this, clientSocket, out, 100);
	}
	
	public void setOnline(boolean signedIn)
	{
		this.online = signedIn;
	}
	
	public long getUserId()
	{
		return userId;
	}
	
	public UpdateHandler getUpdateHandler()
	{
		return updateHandler;
	}
	
	public RequestHandler getRequestHandler()
	{
		return requestHandler;
	}
	
	public boolean isOnline()
	{
		return online;
	}
}
