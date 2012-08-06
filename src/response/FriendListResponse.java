package response;

import java.util.LinkedList;

import client.authenticated.Friend;

public class FriendListResponse extends Response
{
	private static final long serialVersionUID = 1L;
	private final LinkedList<Friend> friendList;
	
	public FriendListResponse(String message, boolean error, LinkedList<Friend> friendList)
	{
		super(message, error);
		this.friendList = friendList;
	}
	
	public LinkedList<Friend> getFriendList()
	{
		return friendList;
	}
}
