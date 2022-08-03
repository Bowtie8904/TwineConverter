package bt.twineconverter;

import bt.console.input.ArgumentParser;
import bt.console.input.FlagArgument;
import bt.console.input.ValueArgument;
import bt.log.ConsoleLoggerHandler;
import bt.log.Log;
import bt.twineconverter.exc.ArgumentException;
import bt.utils.Null;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class Main
{
    public static void main(String[] args)
    {
        Log.createDefaultLogFolder();
        Log.configureDefaultJDKLogger(new ConsoleLoggerHandler());

        ArgumentParser parser = new ArgumentParser("-");

        ValueArgument inputCmd = new ValueArgument("inputDir", "i");
        inputCmd.description("The directory where this application will look for Twine HTML files.");
        inputCmd.usage("-inputDir C:/path/to/my/folder");
        inputCmd.onMissing(() -> {
            throw new ArgumentException("inputDir missing. The application does not know where to search for Twine HTML files.");
        });
        parser.register(inputCmd);

        ValueArgument outputCmd = new ValueArgument("outputDir", "o");
        outputCmd.description("The directory where this application will save the output files.");
        outputCmd.usage("-outputDir C:/path/to/my/folder");
        outputCmd.onMissing(() -> {
            throw new ArgumentException("outputDir missing. The application does not know where to save the output files");
        });
        parser.register(outputCmd);

        FlagArgument textCmd = new FlagArgument("generateTexts", "t");
        textCmd.description("Indicates that the application should export all texts as language files.");
        textCmd.usage("-generateTexts");
        parser.register(textCmd);

        ValueArgument languageCmd = new ValueArgument("language", "l");
        languageCmd.description("The language that the input files are using. Only required if the generateTexts flag was set.");
        languageCmd.usage("-language EN");
        parser.register(languageCmd);

        parser.registerDefaultHelpArgument("help", "h");
        parser.parse(args);

        new TwineConverter(inputCmd.getValue(), outputCmd.getValue(), textCmd.getFlag(), Null.nullValue(languageCmd.getValue(), "EN")).convert();
    }
}