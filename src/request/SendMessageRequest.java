package request;

public final class SendMessageRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	private final String message;
	
	public SendMessageRequest(long friendUserId, String message)
	{
		this.friendUserId = friendUserId;
		this.message = message;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
	
	public String getMessage()
	{
		return message;
	}
}
