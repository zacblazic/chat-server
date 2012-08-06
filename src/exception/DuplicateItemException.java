package exception;

public class DuplicateItemException extends Exception
{
	private static final long serialVersionUID = 1L;

	public DuplicateItemException(String message)
	{
		super(message);
	}
}
