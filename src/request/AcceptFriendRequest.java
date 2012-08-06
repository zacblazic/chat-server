package request;

public final class AcceptFriendRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public AcceptFriendRequest(long friendUserId)
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
