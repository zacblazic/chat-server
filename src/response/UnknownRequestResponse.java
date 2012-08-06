package response;

public class UnknownRequestResponse extends Response
{
	private static final long serialVersionUID = 1L;

	public UnknownRequestResponse(String message, boolean error)
	{
		super(message, error);
	}
}
