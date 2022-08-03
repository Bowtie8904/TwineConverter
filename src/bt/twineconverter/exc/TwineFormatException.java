package bt.twineconverter.exc;

/**
 * @author Lukas Hartwig
 * @since 10.07.2022
 */
public class TwineFormatException extends RuntimeException
{
    public TwineFormatException(String message)
    {
        super(message);
    }

    public TwineFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }
}