package update;

import client.authenticated.Friend;

public final class PendingFriendAddedUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final Friend friend;
	
	public PendingFriendAddedUpdate(Friend friend) 
	{
		this.friend = friend;
	}
	
	public Friend getFriend()
	{
		return friend;
	}
}
