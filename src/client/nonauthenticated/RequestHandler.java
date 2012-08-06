package client.nonauthenticated;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import request.CreateAccountRequest;
import request.Request;
import request.SignInRequest;
import response.CreateAccountResponse;
import response.SignInResponse;
import server.ClientList;
import client.authenticated.Client;
import exception.AlreadySignedInException;
import exception.IncorrectPasswordException;
import exception.NoSuchUserException;
import exception.UsernameExistsException;

public final class RequestHandler extends Thread
{
	private final ClientList clientList;
	private final Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public RequestHandler(ClientList clientList, Socket clientSocket)
	{
		this.clientList = clientList;
		this.clientSocket = clientSocket;
	}
	
	public void run()
	{
		createObjectStreams();
		
		try
		{
			Request request = (Request)in.readObject();
			
			if(request instanceof CreateAccountRequest)
			{
				try
				{
					CreateAccountRequest createAccountRequest = (CreateAccountRequest)request;
					createAccount(createAccountRequest);
					
					//Successfully created account
					out.writeObject(new CreateAccountResponse("", false));
					out.flush();
				}
				catch(UsernameExistsException uee)
				{
					//User already exists
					out.writeObject(new CreateAccountResponse("Username already exists", true));
					out.flush();
				}
				catch(SQLException sqle)
				{
					//Error with database
					out.writeObject(new CreateAccountResponse("A database error has occurred", true));
					out.flush();
				}
				
				closeObjectStreams();
				closeSocket();
			}
			else if(request instanceof SignInRequest)
			{
				try
				{
					SignInRequest signInRequest = (SignInRequest)request;
					signClientIn(signInRequest);
					
					//Signed successfully
					out.writeObject(new SignInResponse("", false, signInRequest.getUsername()));
					out.flush();
					//Dont close streams or socket
				}
				catch(NoSuchUserException nsue)
				{
					//No username that matches the one entered
					out.writeObject(new SignInResponse("No such username exists", true, ""));
					out.flush();
					closeObjectStreams();
					closeSocket();
				}
				catch(AlreadySignedInException asie)
				{
					//Someone is already signed in on that username
					out.writeObject(new SignInResponse("A user with that username is already signed in", true, ""));
					out.flush();
					closeObjectStreams();
					closeSocket();
				}
				catch(IncorrectPasswordException ipe)
				{
					//Password is not correct
					out.writeObject(new SignInResponse("Incorrect password entered", true, ""));
					out.flush();
					closeObjectStreams();
					closeSocket();
				}
				catch(SQLException sqle)
				{
					//Error with database
					out.writeObject(new SignInResponse("A database error has occurred", true, ""));
					out.flush();
					closeObjectStreams();
					closeSocket();
				}
			}
			else
			{
				closeObjectStreams();
				closeSocket();
			}
		}
		catch(ClassNotFoundException cnfe)
		{
			System.err.println(cnfe.toString());
			closeObjectStreams();
			closeSocket();
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
			closeObjectStreams();
			closeSocket();
		}
	}
	
	private void createAccount(CreateAccountRequest request) throws UsernameExistsException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			if(isExistingUser(request.getUsername()))
			{
				throw new UsernameExistsException();
			}
			
			String sql = "INSERT INTO chat.users(username, password, email_address, first_name, last_name) "
					  + "VALUES('" + request.getUsername() + "','" + request.getPassword() + "','" 
					  + request.getEmailAddress() + "','" + request.getFirstName() + "','" 
					  + request.getLastName()+ "')";
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);	
		}
		finally
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
	}
	
	private void signClientIn(SignInRequest request) throws NoSuchUserException, AlreadySignedInException, IncorrectPasswordException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			if(isUserOnline(request.getUsername()))
			{
				throw new AlreadySignedInException();
			}
			
			if(!isCorrectPassword(request.getUsername(), request.getPassword()))
			{
				throw new IncorrectPasswordException();
			}
			
			long userId = determineUserId(request.getUsername());
			Client client = new Client(userId, clientList, clientSocket, out, in);
			clientList.insertClient(client);
			
			String sql = "UPDATE chat.users SET online_status = 'online' WHERE username = '" + request.getUsername() + "'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);
		}
		finally
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
	}
	
	private int determineUserId(String username) throws NoSuchUserException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		int userId;
		
		try
		{
			String sql = "SELECT user_id FROM chat.users WHERE username = '" + username + "'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				userId = resultSet.getInt(1);
			}
			else
			{
				throw new NoSuchUserException();
			}
		}
		finally
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
		
		return userId;
	}
	
	private boolean isCorrectPassword(String username, String password) throws NoSuchUserException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean correct = false;
		
		try
		{
			String sql = "SELECT password FROM chat.users WHERE username = '" + username + "'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				String correctPassword = resultSet.getString(1);
				
				if(password.equals(correctPassword))
				{
					correct = true;
				}
			}
			else
			{
				throw new NoSuchUserException();
			}
		}
		finally
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
		
		return correct;
	}
	
	private boolean isUserOnline(String username) throws NoSuchUserException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean online = false;
		
		try
		{
			String sql = "SELECT online_status FROM chat.users WHERE username = '" + username + "'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				String onlineStatus = resultSet.getString(1);
				
				if(onlineStatus.equalsIgnoreCase("online"))
				{
					online = true;
				}
			}
			else
			{
				throw new NoSuchUserException();
			}
		}
		finally
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
		
		return online;
	}
	
	private boolean isExistingUser(String username) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean exists = false;
		
		try
		{
			String sql = "SELECT username FROM chat.users WHERE username = '" + username + "'";
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			exists = resultSet.next();
		}
		finally
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
		
		return exists;
	}
	
	private Connection createDatabaseConnection() throws SQLException
	{
		Connection connection = null;
		
		connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:pool");
		
		return connection;
	}
	
	private void createObjectStreams()
	{
		try
		{
			if(clientSocket != null)
			{
				out = new ObjectOutputStream(this.clientSocket.getOutputStream());
				in = new ObjectInputStream(this.clientSocket.getInputStream());
			}
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}
	
	private void closeObjectStreams()
	{
		try
		{
			if(out != null)
			{
				out.close();
			}
			
			if(in != null)
			{
				in.close();
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
