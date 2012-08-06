package request;

public final class AddFriendRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final String username;
	
	public AddFriendRequest(String username)
	{
		this.username = username;
	}

	public String getUsername() 
	{
		return username;
	}
}
