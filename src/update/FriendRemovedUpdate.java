package update;

public final class FriendRemovedUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public FriendRemovedUpdate(long friendUserId)
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
