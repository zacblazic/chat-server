package request;

public final class CreateAccountRequest extends Request
{
	private static final long serialVersionUID = 1L;
	private final String firstName;
	private final String lastName;
	private final String emailAddress;
	private final String username;
	private final String password;
	
	public CreateAccountRequest(String firstName, String lastName, String emailAddress, String username, String password)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.username = username;
		this.password = password;
	}
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public String getLastName()
	{
		return lastName;
	}
	
	public String getEmailAddress()
	{
		return emailAddress;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
}
