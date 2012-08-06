package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class TestSQL 
{
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:mysql://localhost:3306/", "root", "password");
		ObjectPool connectionPool = new GenericObjectPool();
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
		PoolingDriver driver = new PoolingDriver();
		driver.registerPool("pool", connectionPool);
		
		Connection connection = null;
		Statement statement = null;
		
		try
		{
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:pool");
			if(isExistingFriendship(8, 7))
			{
				System.out.println("Friendship exists");
			}
			else
			{
				System.out.println("Friendship does not exist");
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
	
	private static Connection createDatabaseConnection() throws SQLException
	{
		Connection connection = null;
		
		connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:pool");
		
		return connection;
	}
	
	private static boolean isExistingFriendship(long userId, long friendUserId) throws SQLException
	{
		return(isFriendOf(userId, friendUserId) && isFriendOf(friendUserId, userId));
	}
	
	private static boolean isFriendOf(long userId, long friendUserId) throws SQLException
	{
		Connection connection = null;
		Statement statement = null;
		boolean friendOf = false;
		
		try
		{
			String sql = "SELECT u.user_id, f.friend_id " +
						 "FROM chat.users u, chat.friendships f " +
						 "WHERE u.user_id = f.user_id " +
						 "AND u.user_id = " + userId + " " +
						 "AND f.friend_id = " + friendUserId;
			
			connection = createDatabaseConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			
			if(resultSet.next())
			{
				friendOf = true;
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
		
		return friendOf;
	}
}
