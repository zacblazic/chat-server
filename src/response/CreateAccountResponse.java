package response;

public class CreateAccountResponse extends Response
{
	private static final long serialVersionUID = 1L;

	public CreateAccountResponse(String message, boolean error)
	{
		super(message, error);
	}
}
