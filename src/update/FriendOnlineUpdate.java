package update;

public final class FriendOnlineUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public FriendOnlineUpdate(long friendUserId) 
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
