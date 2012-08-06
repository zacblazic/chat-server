package update;

public final class PendingFriendRemovedUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final long pendingFriendUserId;
	
	public PendingFriendRemovedUpdate(long pendingFriendUserId)
	{
		this.pendingFriendUserId = pendingFriendUserId;
	}
	
	public long getPendingFriendUserId()
	{
		return pendingFriendUserId;
	}
}
