package response;

public class ReceiveUpdatesResponse extends Response
{
	private static final long serialVersionUID = 1L;

	public ReceiveUpdatesResponse(String message, boolean error) 
	{
		super(message, error);
	}
}
