package bt.twineconverter.util;

/**
 * @author Lukas Hartwig
 * @since 21.08.2022
 */
public final class ReplaceUtils
{
    private static final String[][] INVALID_CHARCTERS = new String[][] {
            { "\u2026", "..." }, // three dots
            { "\u2018\u2019", "\"" }, // opening quotation mark
            { "\u2019\u2019", "\"" }, // closing quotation mark
            { "\u2019", "'" }, // single apostrophe
            { "\u2018", "'" } // single apostrophe
    };

    public static String replaceInvalidCharacters(String text)
    {
        for (String[] pair : INVALID_CHARCTERS)
        {
            text = text.replace(pair[0], pair[1]);
        }

        return text;
    }
}