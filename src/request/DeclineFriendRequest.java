package request;

public final class DeclineFriendRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public DeclineFriendRequest(long friendUserId)
	{
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
