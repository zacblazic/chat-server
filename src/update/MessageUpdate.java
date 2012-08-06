package update;

public final class MessageUpdate extends Update
{
	private static final long serialVersionUID = 1L;
	private final long senderUserId;
	private final String message;
	
	public MessageUpdate(long senderUserId, String message) 
	{
		this.senderUserId = senderUserId;
		this.message = message;
	}
	
	public long getSenderUserId()
	{
		return senderUserId;
	}

	public String getMessage() 
	{
		return message;
	}
}
