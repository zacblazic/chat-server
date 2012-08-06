package response;

import java.io.Serializable;

public class Response implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String message;
	private final boolean error;
	
	public Response(String message, boolean error)
	{
		this.message = message;
		this.error = error;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public boolean isError()
	{
		return error;
	}
}
