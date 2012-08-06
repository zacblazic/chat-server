package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public final class Server 
{
	//Configuration settings
	private final int port;
	private final int backlog;
	private final int clientCapacity;
	
	private ServerSocket serverSocket;
	private final ClientList clientList;
	private ConnectionHandler connectionHandler;
	private volatile boolean acceptConnections;
	
	public Server(int port, int backlog, int clientCapacity) throws IllegalArgumentException
	{
		//Validate parameters
		if(port < 0 || port > 65535)
		{
			throw new IllegalArgumentException("Port must be >= 0 and < 65535");
		}
		
		if(clientCapacity <= 0)
		{
			throw new IllegalArgumentException("Client capacity must be > 0");
		}
		
		this.port = port;
		this.backlog = backlog;
		this.clientCapacity = clientCapacity;
		this.clientList = new ClientList(this.clientCapacity);
		
		configureDatabaseDriver();
		performDatabaseCleanUp();
	}
	
	public void start()
	{
		try
		{
			System.out.println("Starting server...");
			
			acceptConnections = true;
			serverSocket = new ServerSocket(port, backlog);
			connectionHandler = new ConnectionHandler(this, serverSocket, clientList);
			connectionHandler.start();
			
			System.out.println("Server started...");
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
	
	public void stop()
	{
		try
		{
			System.out.println("Stopping server...");
			
			if(serverSocket != null)
			{
				acceptConnections = false;
				serverSocket.close();
			}
			
			System.out.println("Server stopped...");
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
	
	public boolean isAcceptingConnections()
	{
		return acceptConnections;
	}
	
	@SuppressWarnings("unused")
	private void configureDatabaseDriver()
	{
		System.out.println("Configuring database driver...");
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:mysql://localhost:3306/", "root", "password");
			ObjectPool connectionPool = new GenericObjectPool();
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
			PoolingDriver driver = new PoolingDriver();
			driver.registerPool("pool", connectionPool);
		}
		catch(ClassNotFoundException cnfe)
		{
			System.err.println("Failed to start database driver...");
		}
	}
	
	private void performDatabaseCleanUp()
	{
		System.out.println("Performing database clean up...");
		
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "UPDATE chat.users SET online_status = 'offline' WHERE online_status = 'online'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);
		}
		catch(SQLException sqle)
		{
			System.err.println();
		}
		finally
		{
			try
			{
				if(statement != null)
				{
					statement.close();
				}
				
				if(connection != null)
				{
					connection.close();
				}
			}
			catch(SQLException sqle)
			{
				System.err.println();
			}
		}
	}
	
	private Connection createDatabaseConnection() throws SQLException
	{
		Connection connection = null;
		
		connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:pool");
		
		return connection;
	}
}
