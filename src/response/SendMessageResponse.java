package response;

public class SendMessageResponse extends Response
{
	private static final long serialVersionUID = 1L;
	private final long friendUserId;
	
	public SendMessageResponse(String message, boolean error, long friendUserId) 
	{
		super(message, error);
		this.friendUserId = friendUserId;
	}
	
	public long getFriendUserId()
	{
		return friendUserId;
	}
}
