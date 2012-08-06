package test;

import server.Server;

public class RunServer 
{
	//Configuration settings
	private static final int port = 22288;
	private static final int backlog = 1000;
	private static final int clientCapacity = 1000;
	
	public static void main(String[] args)
	{
		Server server = null;
		
		try
		{
			server = new Server(port, backlog, clientCapacity);
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		
		server.start();
	}
}
