package response;

import java.util.LinkedList;

import client.authenticated.Friend;

public class PendingFriendListResponse extends Response
{
	private static final long serialVersionUID = 1L;
	private final LinkedList<Friend> pendingFriendList;
	
	public PendingFriendListResponse(String message, boolean error, LinkedList<Friend> pendingFriendList)
	{
		super(message, error);
		this.pendingFriendList = pendingFriendList;
	}
	
	public LinkedList<Friend> getPendingFriendList()
	{
		return pendingFriendList;
	}
}
