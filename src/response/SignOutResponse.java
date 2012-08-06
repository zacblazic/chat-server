package response;

public class SignOutResponse extends Response
{
	private static final long serialVersionUID = 1L;

	public SignOutResponse(String message, boolean error) 
	{
		super(message, error);
	}
}
