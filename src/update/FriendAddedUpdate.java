package update;

import client.authenticated.Friend;

public final class FriendAddedUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	public final Friend friend;
	
	public FriendAddedUpdate(Friend friend) 
	{
		this.friend = friend;
	}
	
	public Friend getFriend()
	{
		return friend;
	}
}
