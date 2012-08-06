package client.authenticated;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import update.Update;

public final class UpdateHandler extends Thread
{
	private final Client client;
	private final Socket clientSocket;
	private ObjectOutputStream out;
	private final UpdateQueue updateQueue;
	
	public UpdateHandler(Client client, Socket clientSocket, ObjectOutputStream out, int updateQueueCapacity)
	{
		this.client = client;
		this.clientSocket = clientSocket;
		this.out = out;
		this.updateQueue = new UpdateQueue(updateQueueCapacity);
	}
	
	public void run()
	{
		while(client.isOnline())
		{
			try
			{
				dispatchUpdates();
				Thread.sleep(100);
			}
			catch(InterruptedException ie)
			{
				System.err.println(ie.toString());
			}
		}
		
		closeObjectStream();
		closeSocket();
	}
	
	public void dispatchUpdates()
	{
		while(!updateQueue.isEmpty())
		{
			try
			{
				Update update = updateQueue.takeUpdate();
				
				synchronized(out)
				{
					out.writeObject(update);
					out.flush();
				}	
			}
			catch(IOException ioe)
			{
				//TODO: Send update to sender (update not sent)
				System.err.println(ioe.toString());
			}
		}
	}
	
	public void sendUpdate(Update update)
	{
		updateQueue.putUpdate(update);
	}
	
	private void closeObjectStream()
	{
		try
		{
			synchronized(out)
			{
				if(out != null)
				{
					out.close();
				}
			}
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
	
	private void closeSocket()
	{
		try
		{
			if(clientSocket != null)
			{
				clientSocket.close();
			}
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
}
