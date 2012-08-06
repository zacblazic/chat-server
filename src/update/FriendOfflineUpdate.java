package update;

public final class FriendOfflineUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public FriendOfflineUpdate(long friendUserId) 
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
