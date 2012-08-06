package response;

public final class SignInResponse extends Response
{
	private static final long serialVersionUID = 1L;
	private final String username;

	public SignInResponse(String message, boolean error, String username)
	{
		super(message, error);
		this.username = username;
	}
	
	public String getUsername()
	{
		return username;
	}
}
