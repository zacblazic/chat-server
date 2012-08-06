package request;

public final class RemoveFriendRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public RemoveFriendRequest(long friendUserId)
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
