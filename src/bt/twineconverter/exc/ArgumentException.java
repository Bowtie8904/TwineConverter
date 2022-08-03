package bt.twineconverter.exc;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class ArgumentException extends RuntimeException
{
    public ArgumentException(String message)
    {
        super(message);
    }

    public ArgumentException(String message, Throwable cause)
    {
        super(message, cause);
    }
}