package client.authenticated;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import request.AcceptFriendRequest;
import request.AddFriendRequest;
import request.DeclineFriendRequest;
import request.FriendListRequest;
import request.PendingFriendListRequest;
import request.ReceiveUpdatesRequest;
import request.RemoveFriendRequest;
import request.Request;
import request.SendMessageRequest;
import request.SignOutRequest;
import response.AcceptFriendResponse;
import response.AddFriendResponse;
import response.DeclineFriendResponse;
import response.FriendListResponse;
import response.PendingFriendListResponse;
import response.ReceiveUpdatesResponse;
import response.RemoveFriendResponse;
import response.SendMessageResponse;
import response.SignOutResponse;
import response.UnknownRequestResponse;
import server.ClientList;
import update.FriendAddedUpdate;
import update.FriendOfflineUpdate;
import update.FriendOnlineUpdate;
import update.FriendRemovedUpdate;
import update.MessageUpdate;
import update.PendingFriendAddedUpdate;
import update.PendingFriendRemovedUpdate;
import exception.MutualFriendshipExistsException;
import exception.NoFriendshipExistsException;
import exception.NoMutualFriendshipExistsException;
import exception.NoPendingFriendshipExistsException;
import exception.NoSuchUserException;
import exception.PendingFriendshipExistsException;
import exception.SelfFriendshipException;
import exception.UserOfflineException;

public final class RequestHandler extends Thread
{
	private final Client client;
	private final ClientList clientList;
	private final Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public RequestHandler(Client client, ClientList clientList, Socket clientSocket, ObjectOutputStream out, ObjectInputStream in)
	{
		this.client = client;
		this.clientList = clientList;
		this.clientSocket = clientSocket;
		this.out = out;
		this.in = in;
	}

	public void run()
	{
		dispatchOnlineUpdates();
		
		while(client.isOnline())
		{
			try
			{
				Request request = null;
				
				synchronized(in)
				{
					request = (Request)in.readObject();
				}
				
				if(request instanceof SendMessageRequest)
				{
					SendMessageRequest sendMessageRequest = (SendMessageRequest)request;
					
					try
					{		
						sendMessage(sendMessageRequest);
						
						synchronized(out)
						{
							out.writeObject(new SendMessageResponse("", false, sendMessageRequest.getFriendUserId()));
							out.flush();
						}
					}
					catch(NoMutualFriendshipExistsException nmfee)
					{
						synchronized(out)
						{
							out.writeObject(new SendMessageResponse("Friendship does not exist", true, sendMessageRequest.getFriendUserId()));
							out.flush();
						}
					}
					catch(NoSuchUserException nsue)
					{
						synchronized(out)
						{
							out.writeObject(new SendMessageResponse("Recipient does not exist", true, sendMessageRequest.getFriendUserId()));
							out.flush();
						}
					}
					catch(UserOfflineException uole)
					{
						synchronized(out)
						{
							out.writeObject(new SendMessageResponse("Recipient is offline", true, sendMessageRequest.getFriendUserId()));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new SendMessageResponse("Database error", true, sendMessageRequest.getFriendUserId()));
							out.flush();
						}
					}
				}
				else if(request instanceof FriendListRequest)
				{
					try
					{
						LinkedList<Friend> friendList = createFriendList();
						
						synchronized(out)
						{
							out.writeObject(new FriendListResponse("", false, friendList));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new FriendListResponse("A database error has occurred", true, null));
							out.flush();
						}
					}
				}
				else if(request instanceof PendingFriendListRequest)
				{
					try
					{
						LinkedList<Friend> pendingFriendList = createPendingFriendList();
						
						synchronized(out)
						{
							out.writeObject(new PendingFriendListResponse("", false, pendingFriendList));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new PendingFriendListResponse("A database error has occurred", true, null));
							out.flush();
						}
					}
				}
				else if(request instanceof ReceiveUpdatesRequest)
				{
					client.getUpdateHandler().start();
					
					synchronized(out)
					{
						out.writeObject(new ReceiveUpdatesResponse("", false));
						out.flush();
					}
				}
				else if(request instanceof SignOutRequest)
				{
					client.setOnline(false);
					
					synchronized(out)
					{
						out.writeObject(new SignOutResponse("", false));
						out.flush();
					}
				}
				else if(request instanceof AddFriendRequest)
				{
					AddFriendRequest addFriendRequest = (AddFriendRequest)request;

					try
					{
						addFriend(addFriendRequest);
						
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("", false));
							out.flush();
						}
					}
					catch(SelfFriendshipException sfe)
					{
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("You cannot add yourself as a friend", true));
							out.flush();
						}
					}
					catch(NoSuchUserException nsue)
					{
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("User does not exit", true));
							out.flush();
						}
					}
					catch(MutualFriendshipExistsException mfee)
					{
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("Mutual friendship already exists", true));
							out.flush();
						}
					}
					catch(PendingFriendshipExistsException pfee)
					{
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("Friendship is pending", true));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new AddFriendResponse("Database error", true));
							out.flush();
						}
						
						System.out.println(sqle);
					}
				}
				else if(request instanceof RemoveFriendRequest)
				{
					RemoveFriendRequest removeFriendRequest = (RemoveFriendRequest)request;
					
					try
					{
						removeFriend(removeFriendRequest);
						
						synchronized(out)
						{
							out.writeObject(new RemoveFriendResponse("", false));
							out.flush();
						}
					}
					catch(NoFriendshipExistsException nfee)
					{
						synchronized(out)
						{
							out.writeObject(new RemoveFriendResponse("No friendship exists", true));
							out.flush();
						}
					}
					catch(NoSuchUserException nsue)
					{
						synchronized(out)
						{
							out.writeObject(new RemoveFriendResponse("User does not exist", true));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new RemoveFriendResponse("Database error", true));
							out.flush();
						}
					}
				}
				else if(request instanceof AcceptFriendRequest)
				{
					AcceptFriendRequest acceptFriendRequest = (AcceptFriendRequest)request;
					
					try
					{
						acceptFriend(acceptFriendRequest);
						
						synchronized(out)
						{
							out.writeObject(new AcceptFriendResponse("", false));
							out.flush();
						}
					}
					catch(NoPendingFriendshipExistsException npfee)
					{
						synchronized(out)
						{
							out.writeObject(new AcceptFriendResponse("No pending friendship exsits", true));
							out.flush();
						}
					}
					catch(NoSuchUserException nsue)
					{
						synchronized(out)
						{
							out.writeObject(new AcceptFriendResponse("User does not exist", true));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new AcceptFriendResponse("Database error", true));
							out.flush();
						}
						
						System.out.println(sqle);
					}
				}
				else if(request instanceof DeclineFriendRequest)
				{
					DeclineFriendRequest declineFriendRequest = (DeclineFriendRequest)request;
					
					try
					{
						declineFriend(declineFriendRequest);
						
						synchronized(out)
						{
							out.writeObject(new DeclineFriendResponse("", false));
							out.flush();
						}
					}
					catch(NoPendingFriendshipExistsException npfee)
					{
						synchronized(out)
						{
							out.writeObject(new DeclineFriendResponse("No pending friendship exists", true));
							out.flush();
						}
					}
					catch(SQLException sqle)
					{
						synchronized(out)
						{
							out.writeObject(new DeclineFriendResponse("Database error", true));
							out.flush();
						}
						
						System.out.println(sqle);
					}
				}
				else
				{
					synchronized(out)
					{
						out.writeObject(new UnknownRequestResponse("Unknown request", true));
					}
				}
			}
			catch(ClassNotFoundException cnfe)
			{
				System.err.println(cnfe.toString());
				client.setOnline(false);
			}
			catch(SocketException se)
			{
				System.err.println(se.toString());
				client.setOnline(false);
			}
			catch(IOException ioe)
			{
				System.err.println(ioe.toString());
				client.setOnline(false);
			}
		}
		
		signClientOut();
		dispatchOfflineUpdates();
		closeObjectStreams();
		closeSocket();
	}
	
	private void dispatchOnlineUpdates()
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "SELECT u.user_id, online_status " +
						 "FROM chat.users u, chat.friendships f " +
						 "WHERE u.user_id = f.friend_id " +
						 "AND f.user_id = (SELECT ff.friend_id " +
						 				  "FROM chat.friendships ff " +
						 				  "WHERE ff.user_id = f.friend_id " +
						 				  "AND ff.friend_id = " + client.getUserId() + ")";
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while(resultSet.next())
			{
				long userId = resultSet.getInt(1);
				String onlineStatus = resultSet.getString(2);
				
				if(onlineStatus.equalsIgnoreCase("online"))
				{
					Client friend = clientList.getClient(userId);
					
					if(friend != null)
					{
						friend.getUpdateHandler().sendUpdate(new FriendOnlineUpdate(client.getUserId()));
					}
				}
			}
		}
		catch(SQLException sqle)
		{
			System.err.println(sqle.toString());
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
				System.err.println(sqle.toString());
			}
		}
	}
	
	private void dispatchOfflineUpdates()
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "SELECT u.user_id, online_status " +
						 "FROM chat.users u, chat.friendships f " +
						 "WHERE u.user_id = f.friend_id " +
						 "AND f.user_id = (SELECT ff.friend_id " +
						 				  "FROM chat.friendships ff " +
						 				  "WHERE ff.user_id = f.friend_id " +
						 				  "AND ff.friend_id = " + client.getUserId() + ")";
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while(resultSet.next())
			{
				long userId = resultSet.getInt(1);
				String onlineStatus = resultSet.getString(2);
				
				if(onlineStatus.equalsIgnoreCase("online"))
				{
					Client friend = clientList.getClient(userId);
					
					if(friend != null)
					{
						friend.getUpdateHandler().sendUpdate(new FriendOfflineUpdate(client.getUserId()));
					}
				}
			}
		}
		catch(SQLException sqle)
		{
			System.err.println(sqle.toString());
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
				System.err.println(sqle.toString());
			}
		}
	}
	
	private void sendMessage(SendMessageRequest request) throws NoMutualFriendshipExistsException, NoSuchUserException, UserOfflineException, SQLException
	{
		if(!isMutualFriendship(client.getUserId(), request.getFriendUserId()))
		{
			throw new NoMutualFriendshipExistsException();
		}
		
		if(!isUserOnline(request.getFriendUserId()))
		{
			throw new UserOfflineException();
		}
		
		Client friend = clientList.getClient(request.getFriendUserId());
		
		if(friend != null)
		{
			//Put message into friend update queue
			friend.getUpdateHandler().sendUpdate(new MessageUpdate(client.getUserId(), request.getMessage()));
		}
		else
		{
			throw new UserOfflineException();
		}
	}
	
	private LinkedList<Friend> createFriendList() throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		LinkedList<Friend> initialFriendList = new LinkedList<Friend>();
		LinkedList<Friend> actualFriendList = new LinkedList<Friend>();
		
		try
		{
			String sql = "SELECT u.user_id, username, first_name, last_name, email_address, online_status " +
					     "FROM chat.users u, chat.friendships f " +
					     "WHERE u.user_id = f.friend_id " +
					     "AND f.user_id = " + client.getUserId();
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while(resultSet.next())
			{
				long userId = resultSet.getLong(1);
				String username = resultSet.getString(2);
				String firstName = resultSet.getString(3);
				String lastName = resultSet.getString(4);
				String emailAddress = resultSet.getString(5);
				String onlineStatus = resultSet.getString(6);
				boolean online = false;
				
				if(onlineStatus.equalsIgnoreCase("online"))
				{
					online = true;
				}
				
				initialFriendList.add(new Friend(userId, username, firstName, lastName, emailAddress, online, false));
			}
			
			for(int i = 0; i < initialFriendList.size(); i++)
			{
				Friend friend = initialFriendList.get(i);
				
				if(!isMutualFriendship(client.getUserId(), friend.getUserId()))
				{
					friend.setOnline(false);
				}
				
				actualFriendList.add(friend);
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
		
		return actualFriendList;
	}
	
	//TODO: Create a better solution to pending friends (inheritence)
	private LinkedList<Friend> createPendingFriendList() throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		LinkedList<Friend> pendingFriendList = new LinkedList<Friend>();
		
		try
		{
			String sql = "SELECT u.user_id, username, first_name, last_name, email_address " +
					     "FROM chat.users u, chat.pending_friendships p " +
					     "WHERE u.user_id = p.user_id " +
					     "AND p.pending_friend_id = " + client.getUserId();
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			while(resultSet.next())
			{
				long userId = resultSet.getInt(1);
				String username = resultSet.getString(2);
				String firstName = resultSet.getString(3);
				String lastName = resultSet.getString(4);
				String emailAddress = resultSet.getString(5);
				
				pendingFriendList.add(new Friend(userId, username, firstName, lastName, emailAddress, false, true));
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
		
		return pendingFriendList;
	}
	
	private void signClientOut()
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "UPDATE chat.users SET online_status = 'offline' WHERE user_id =" + client.getUserId();
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);
			
			clientList.removeClient(client.getUserId());
		}
		catch(SQLException sqle)
		{
			System.err.println(sqle);
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
				System.err.println(sqle);
			}
		}
	}
	
	private void addFriend(AddFriendRequest request) throws SelfFriendshipException, NoSuchUserException, MutualFriendshipExistsException, PendingFriendshipExistsException, SQLException
	{
		long friendUserId = determineUserId(request.getUsername());
		
		if(client.getUserId() == friendUserId)
		{
			throw new SelfFriendshipException();
		}
		
		if(isMutualFriendship(client.getUserId(), friendUserId))
		{
			throw new MutualFriendshipExistsException();
		}
		
		//Pending friend request to friend
		if(isPendingFriendshipTo(client.getUserId(), friendUserId))
		{
			throw new PendingFriendshipExistsException();
		}
		
		//Pending friend request from friend
		if(isPendingFriendshipFrom(friendUserId, client.getUserId()))
		{
			//Remove pending friendship from friend
			removePendingFriendshipFrom(friendUserId, client.getUserId());
			client.getUpdateHandler().sendUpdate(new PendingFriendRemovedUpdate(friendUserId));
			
			//Create mutual friendship between client and friend
			createFriendshipTo(client.getUserId(), friendUserId);
			Friend friend = getFriendDetails(friendUserId);
			client.getUpdateHandler().sendUpdate(new FriendAddedUpdate(friend));

			if(isUserOnline(friendUserId))
			{
				Client friendClient = clientList.getClient(friendUserId);
				
				if(friendClient != null)
				{
					//Send friend online update to friend
					friendClient.getUpdateHandler().sendUpdate(new FriendOnlineUpdate(client.getUserId()));
				}
				
				//Send friend online update to client
				client.getUpdateHandler().sendUpdate(new FriendOnlineUpdate(friendUserId));
			}
		}
		else
		{
			if(!isFriendship(client.getUserId(), friendUserId))
			{
				createFriendshipTo(client.getUserId(), friendUserId);	
				Friend friend = getFriendDetails(friendUserId);
				friend.setOnline(false);
				client.getUpdateHandler().sendUpdate(new FriendAddedUpdate(friend));
			}
			
			createPendingFriendshipTo(client.getUserId(), friendUserId);

			//Send pending friend update to friend
			if(isUserOnline(friendUserId))
			{
				Client friendClient = clientList.getClient(friendUserId);
				
				if(friendClient != null)
				{
					Friend clientMe = getFriendDetails(client.getUserId());
					clientMe.setOnline(false);
					clientMe.setPending(true);
					friendClient.getUpdateHandler().sendUpdate(new PendingFriendAddedUpdate(clientMe));
				}
			}
		}
	}
	
	private void removeFriend(RemoveFriendRequest request) throws NoFriendshipExistsException, NoSuchUserException, SQLException
	{
		//Redundant to perform this
		if(!isFriendship(client.getUserId(), request.getFriendUserId()))
		{
			throw new NoFriendshipExistsException();
		}
		
		if(isMutualFriendship(client.getUserId(), request.getFriendUserId()))
		{
			removeFriendshipTo(client.getUserId(), request.getFriendUserId());
			client.getUpdateHandler().sendUpdate(new FriendRemovedUpdate(request.getFriendUserId()));
			
			if(isUserOnline(request.getFriendUserId()))
			{
				Client friendClient = clientList.getClient(request.getFriendUserId());
				
				if(friendClient != null)
				{
					friendClient.getUpdateHandler().sendUpdate(new FriendOfflineUpdate(client.getUserId()));
				}
			}
		}
		else 
		{
			removeFriendshipTo(request.getFriendUserId(), client.getUserId());
			client.getUpdateHandler().sendUpdate(new FriendRemovedUpdate(request.getFriendUserId()));
			
			if(isPendingFriendshipTo(client.getUserId(), request.getFriendUserId()))
			{
				removePendingFriendshipTo(client.getUserId(), request.getFriendUserId());
				
				if(isUserOnline(request.getFriendUserId()))
				{
					Client friendClient = clientList.getClient(request.getFriendUserId());
					
					if(friendClient != null)
					{
						friendClient.getUpdateHandler().sendUpdate(new PendingFriendRemovedUpdate(client.getUserId()));
					}
				}
			}
		}
	}
	
	private void acceptFriend(AcceptFriendRequest request) throws NoPendingFriendshipExistsException, NoSuchUserException, SQLException
	{
		//Check if pending friendship still exists
		if(!isPendingFriendshipFrom(request.getFriendUserId(), client.getUserId()))
		{
			throw new NoPendingFriendshipExistsException();
		}
		
		//Remove pending friendship from friend
		removePendingFriendshipFrom(request.getFriendUserId(), client.getUserId());
		client.getUpdateHandler().sendUpdate(new PendingFriendRemovedUpdate(request.getFriendUserId()));
		
		//Create mutual friendship between client and friend
		if(!isFriendship(client.getUserId(), request.getFriendUserId()))
		{
			createFriendshipTo(client.getUserId(), request.getFriendUserId());
			Friend friend = getFriendDetails(request.getFriendUserId());
			client.getUpdateHandler().sendUpdate(new FriendAddedUpdate(friend));
		}
		
		if(isUserOnline(request.getFriendUserId()))
		{
			Client friendClient = clientList.getClient(request.getFriendUserId());
			
			if(friendClient != null)
			{
				friendClient.getUpdateHandler().sendUpdate(new FriendOnlineUpdate(client.getUserId()));
			}
			
			//Not needed
			client.getUpdateHandler().sendUpdate(new FriendOnlineUpdate(request.getFriendUserId()));
		}
	}
	
	private void declineFriend(DeclineFriendRequest request) throws NoPendingFriendshipExistsException, SQLException
	{
		//Check if pending friendship still exists
		if(!isPendingFriendshipFrom(request.getFriendUserId(), client.getUserId()))
		{
			throw new NoPendingFriendshipExistsException();
		}
		
		//Remove pending friendship from friend
		removePendingFriendshipFrom(request.getFriendUserId(), client.getUserId());
		client.getUpdateHandler().sendUpdate(new PendingFriendRemovedUpdate(request.getFriendUserId()));
	}
	
	private void removeFriendshipTo(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "DELETE FROM chat.friendships " +
					     "WHERE user_id = " + userId + " " +
					     "AND friend_id = " + friendUserId;
			
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
	
	private Friend getFriendDetails(long friendUserId) throws NoSuchUserException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		Friend friend = null;
		
		try
		{
			String sql = "SELECT user_id, username, first_name, last_name, email_address, online_status FROM chat.users WHERE user_id = " + friendUserId;
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				long userId = resultSet.getInt(1);
				String username = resultSet.getString(2);
				String firstName = resultSet.getString(3);
				String lastName = resultSet.getString(4);
				String emailAddress = resultSet.getString(5);
				String onlineStatus = resultSet.getString(6);
				boolean online = false;

				if(onlineStatus.equalsIgnoreCase("online"))
				{
					online = true;
				}
				
				friend = new Friend(userId, username, firstName, lastName, emailAddress, online, false);
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
		
		return friend;
	}

	private void removePendingFriendshipFrom(long friendUserId, long userId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "DELETE FROM chat.pending_friendships " +
					     "WHERE user_id = " + friendUserId + " " +
					     "AND pending_friend_id = " + userId;
			
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
	
	private void removePendingFriendshipTo(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "DELETE FROM chat.pending_friendships " +
					     "WHERE user_id = " + userId + " " +
					     "AND pending_friend_id = " + friendUserId;
			
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
	
	private void createFriendshipTo(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "INSERT INTO chat.friendships VALUES(" + userId + ", " + friendUserId + ")";
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
	
	private void createPendingFriendshipTo(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			String sql = "INSERT INTO chat.pending_friendships VALUES(" + userId + ", " + friendUserId + ")";
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
	
	private boolean isPendingFriendshipTo(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean pendingFriendship = false;
		
		try
		{
			String sql = "SELECT user_id, pending_friend_id " +
				     	 "FROM chat.pending_friendships " +
				     	 "WHERE user_id = " + userId +  " " +
				     	 "AND pending_friend_id = " + friendUserId;
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				pendingFriendship = true;
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
		
		return pendingFriendship;
	}
	
	private boolean isPendingFriendshipFrom(long friendUserId, long userId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean pendingFriendship = false;
		
		try
		{
			String sql = "SELECT user_id, pending_friend_id " +
					     "FROM chat.pending_friendships " +
					     "WHERE user_id = " + friendUserId +  " " +
					     "AND pending_friend_id = " + userId;
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				pendingFriendship = true;
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
		
		return pendingFriendship;
	}
	
	private boolean isMutualFriendship(long userId, long friendUserId) throws SQLException
	{
		return(isFriendship(userId, friendUserId) && isFriendship(friendUserId, userId));
	}
	
	private boolean isFriendship(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean friendship = false;
		
		try
		{
			String sql = "SELECT user_id, friend_id " +
					     "FROM chat.friendships " +
					     "WHERE user_id = " + userId + " " +
					     "AND friend_id = " + friendUserId;
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				friendship = true;
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
		
		return friendship;
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
	
	private boolean isUserOnline(long userId) throws NoSuchUserException, SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean online = false;
		
		try
		{
			String sql = "SELECT online_status FROM chat.users WHERE user_id = " + userId;
			
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
	
	private Connection createDatabaseConnection() throws SQLException
	{
		Connection connection = null;
		
		connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:pool");
		
		return connection;
	}
	
	private void closeObjectStreams()
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
			
			synchronized(in)
			{
				if(in != null)
				{
					in.close();
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
