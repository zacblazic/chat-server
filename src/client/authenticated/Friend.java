package client.authenticated;

import java.io.Serializable;

public class Friend implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final long userId;
	private final String username;
	private final String firstName;
	private final String lastName;
	private final String emailAdress;
	private boolean online;
	private boolean pending;
	
	public Friend(long userId, String username, String firstName, String lastName, String emailAddress, boolean online, boolean pending)
	{
		this.userId = userId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAdress = emailAddress;
		this.online = online;
		this.pending = pending;
	}
	
	public void setOnline(boolean online)
	{
		this.online = online;
	}
	
	public void setPending(boolean pending)
	{
		this.pending = pending;
	}
	
	public long getUserId()
	{
		return userId;
	}

	public String getUsername()
	{
		return username;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getEmailAdress()
	{
		return emailAdress;
	}
	
	public boolean isOnline()
	{
		return online;
	}
	
	public boolean isPending()
	{
		return pending;
	}
}
