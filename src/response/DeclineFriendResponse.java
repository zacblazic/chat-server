package response;

public class DeclineFriendResponse extends Response
{
	private static final long serialVersionUID = 1L;

	public DeclineFriendResponse(String message, boolean error) 
	{
		super(message, error);
	}
}
