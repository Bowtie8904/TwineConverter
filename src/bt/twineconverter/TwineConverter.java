package bt.twineconverter;

import bt.io.json.JSON;
import bt.io.text.impl.TextSaver;
import bt.io.text.obj.Text;
import bt.io.xml.XML;
import bt.log.Log;
import bt.twineconverter.data.*;
import bt.twineconverter.exc.ArgumentException;
import bt.twineconverter.exc.TwineFormatException;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class TwineConverter
{
    private static final Pattern STORY_DATA_PATTERN = Pattern.compile("(?s)<tw-storydata.*<\\/tw-storydata>");
    private static final Pattern DIALOG_OPTION_TEXT_PATTERN = Pattern.compile("\\)\\[([^\\[].*?)\\]");
    private static final Pattern DIALOG_OPTION_ID_PATTERN = Pattern.compile("\\(if:\s*\\$choice = (\\d*).*\\)");
    private static final Pattern DIALOG_OPTION_CONDITION_PATTERN = Pattern.compile("\\(if:\\s*\\$choice = \\d* AND (.*)\\)");
    private static final Pattern IF_DICE_ROLL_PATTERN = Pattern.compile("\\(if:\s*\\$roll\\d*.*");
    private static final Pattern IF_DICE_ROLL_VALUE_PATTERN = Pattern.compile("\\(if:\s*\\$roll(\\d*).*");
    private static final Pattern IF_CONDITION_PATTERN = Pattern.compile("\\(if:(.*?)\\)");

    private File inputDir;
    private File outputDir;
    private File textOutputDir;
    private boolean generateTexts;
    private String language;

    public TwineConverter(String inputDir, String outputDir, boolean generateTexts, String language)
    {
        this.inputDir = new File(inputDir);

        if (!this.inputDir.exists())
        {
            throw new ArgumentException("inputDir " + inputDir + " does not exist");
        }

        this.outputDir = new File(outputDir + "/adventures");

        if (!this.outputDir.exists())
        {
            this.outputDir.mkdirs();
        }

        this.generateTexts = generateTexts;
        this.language = language;

        if (generateTexts)
        {
            this.textOutputDir = new File(outputDir + "/texts");

            if (!this.textOutputDir.exists())
            {
                this.textOutputDir.mkdirs();
            }
        }

        Log.debug("Input folder: " + this.inputDir.getAbsolutePath());
        Log.debug("Output folder: " + this.outputDir.getAbsolutePath());

        if (this.textOutputDir != null)
        {
            Log.debug("Text output folder: " + this.textOutputDir.getAbsolutePath());
        }

        Log.debug("Generating texts: " + this.generateTexts);
        Log.debug("Text language: " + this.language);
    }

    public void convert()
    {
        for (File twineFile : this.inputDir.listFiles())
        {
            if (!twineFile.getName().toLowerCase().endsWith(".html"))
            {
                continue;
            }

            try
            {
                Log.debug("Converting " + twineFile.toPath());
                String text = Files.readString(twineFile.toPath());
                String storyData = extractStoryData(text);
                var doc = XML.parse(storyData);

                Node storyDataNode = doc.selectSingleNode("//" + XML.lowerNode("tw-storydata"));
                String storyName = storyDataNode.valueOf(XML.lowerAttribute("name"));
                String formattedStoryName = formatString(storyDataNode.valueOf(XML.lowerAttribute("name")));
                int startNodeId = Integer.parseInt(storyDataNode.valueOf(XML.lowerAttribute("startnode")));

                Log.debug("================================================================");
                Log.debug("================================================================");
                Log.debug("Story name: " + storyName);
                Log.debug("Formatted story name: " + formattedStoryName);
                Log.debug("Start passage id:" + startNodeId);

                TwineStory story = new TwineStory(formattedStoryName, startNodeId);

                List<Node> passageDataList = doc.selectNodes("//" + XML.lowerNode("tw-passagedata"));
                List<Text> texts = new LinkedList<>();

                Log.debug("Found " + passageDataList.size() + " passages");

                for (Node passageData : passageDataList)
                {
                    Set<Integer> diceRolls = new HashSet<>();
                    int passageId = Integer.parseInt(passageData.valueOf(XML.lowerAttribute("pid")));
                    String passageName = passageData.valueOf(XML.lowerAttribute("name"));
                    String formattedPassageName = formatString(passageData.valueOf(XML.lowerAttribute("name")));

                    Log.debug("----------------------------------------------------------------");
                    Log.debug("Passage name: " + passageName);
                    Log.debug("Formatted passage name: " + formattedPassageName);
                    Log.debug("Passage id: " + passageId);

                    if (passageName.equalsIgnoreCase("end"))
                    {
                        Log.debug("End passage. Content ignored");
                    }
                    else
                    {
                        String passageText = passageData.getText();

                        TwinePassage passage = new TwinePassage(passageId, formattedPassageName);

                        String[] lines = passageText.split("\n");

                        passageText = "";

                        for (int i = 0; i < lines.length; i++)
                        {
                            String line = lines[i];

                            if (lineStartsWith(line, "[[") && lineEndsWith(line, "]]"))
                            {
                                TwineSwitchPassage switchPassage = extractSwitchPassage(line);
                                Log.debug("Found unconditional passage switch to " + switchPassage.getPassage());

                                if (passage.getPassageSwitch() != null)
                                {
                                    Log.warn("Multiple unconditional passage switches detected. Previous value will be overwritten.");
                                }

                                passage.setPassageSwitch(switchPassage);
                            }
                            else if (lineStartsWith(line, "(if:$choice ") || lineStartsWith(line, "(if: $choice "))
                            {
                                int dialogOptionId = extractDialogOptionId(line);
                                String dialogCondition = extractDialogOptionCondition(line);

                                Log.debug("Found dialog option with id " + dialogOptionId + " and condition " + dialogCondition);

                                TwineDialogOption option = obtainDialogOption(passage, dialogOptionId);

                                if (option.getText() == null || option.getText().isBlank())
                                {
                                    String dialogOptionText = extractDialogOptionText(line);

                                    if (this.generateTexts)
                                    {
                                        String key = story.getName() + "." + passage.getName() + ".options." + option.getId() + ".text";
                                        texts.add(new Text(key, dialogOptionText));

                                        option.setText(key);
                                    }
                                    else
                                    {
                                        option.setText(dialogOptionText);
                                    }
                                }

                                TwineAction action = extractDialogOptionAction(line);
                                action.setCondition(dialogCondition);
                                option.addAction(action);

                                passage.addDialogOption(option);
                            }
                            else if (lineStartsWith(line, "(set:"))
                            {
                                TwineSetValue setter = extractValueSetter(line);

                                Log.debug("Found value setter " + setter.getSetString());

                                passage.addValueSetter(setter);
                            }
                            else if (lineStartsWith(line, "(if:$default ") || lineStartsWith(line, "(if: $default "))
                            {
                                Log.debug("Found default condition");
                                TwineAction action = extractDefaultCondition(line);
                                passage.addDefaultCondition(action);
                            }
                            else if (lineStartsWith(line, "(if:"))
                            {
                                if (lineMatches(line, IF_DICE_ROLL_PATTERN))
                                {
                                    int value = extractDiceRollValue(line);
                                    diceRolls.add(value);
                                }

                                String conditionText = extractIfCondition(line);
                                TwineCondition condition = obtainIfCondition(passage, conditionText);

                                Log.debug("Found if condition " + conditionText);

                                TwineAction action = extractIfConditionAction(line);
                                condition.addAction(action);

                                passage.addCondition(condition);
                            }
                            else
                            {
                                passageText += line + "\n";
                            }
                        }

                        if (this.generateTexts)
                        {
                            String key = story.getName() + "." + passage.getName() + ".text";
                            texts.add(new Text(key, passageText));

                            passage.setText(key);
                        }
                        else
                        {
                            passage.setText(passageText);
                        }

                        for (Integer dice : diceRolls)
                        {
                            Log.debug("Adding setter for dice roll " + dice);
                            passage.addValueSetter(new TwineSetValue("$roll" + dice + " to random(1, " + dice + ")"));
                        }

                        story.addPassage(passage);
                    }
                }

                // XML.save(story.toXML(), this.outputDir.getAbsolutePath() + "/" + story.getName() + ".xml");
                JSON.save(story.toJSON(), this.outputDir.getAbsolutePath() + "/" + story.getName() + ".adv");

                if (this.generateTexts)
                {
                    File languageFile = new File(this.textOutputDir.getAbsolutePath() + "/adventure_" + story.getName() + "_" + this.language.toLowerCase() + ".lang");
                    new TextSaver().save(languageFile, this.language.toLowerCase(), texts);
                }
            }
            catch (IOException e)
            {
                Log.error("Failed to read file " + twineFile.getAbsolutePath(), e);
            }
            catch (DocumentException e)
            {
                Log.error("Failed to parse story data as XML " + twineFile.getAbsolutePath(), e);
            }
        }
    }

    private boolean lineMatches(String line, Pattern pattern)
    {
        return pattern.matcher(line.toLowerCase().trim()).matches();
    }

    private boolean lineStartsWith(String line, String value)
    {
        return line.toLowerCase().trim().startsWith(value);
    }

    private boolean lineEndsWith(String line, String value)
    {
        return line.toLowerCase().trim().endsWith(value);
    }

    private TwineAction extractIfConditionAction(String line)
    {
        String actionText = line.substring(line.indexOf(")") + 1);

        if (lineStartsWith(actionText, "[[") && lineEndsWith(actionText, "]]"))
        {
            return extractSwitchPassage(actionText);
        }
        else if (lineStartsWith(actionText, "(set:"))
        {
            return extractValueSetter(actionText);
        }

        throw new TwineFormatException("Invalid if condition. Missing or invalid action. " + line);
    }

    private TwineCondition obtainIfCondition(TwinePassage passage, String condition)
    {
        return passage.getConditions()
                      .stream()
                      .filter(tc -> tc.getCondition().trim().equalsIgnoreCase(condition))
                      .findFirst().orElse(new TwineCondition(condition));
    }

    private String extractIfCondition(String line)
    {
        Matcher matcher = IF_CONDITION_PATTERN.matcher(line);

        if (matcher.find())
        {
            return matcher.group(1).trim();
        }

        throw new TwineFormatException("Invalid if condition line. Missing condition part. " + line);
    }

    private TwineAction extractDefaultCondition(String line)
    {
        String actionText = line.substring(line.indexOf(")") + 1);

        if (lineStartsWith(actionText, "[[") && lineEndsWith(actionText, "]]"))
        {
            return extractSwitchPassage(actionText);
        }
        else if (lineStartsWith(actionText, "(set:"))
        {
            return extractValueSetter(actionText);
        }

        throw new TwineFormatException("Invalid default condition line. Missing or invalid action. " + line);
    }

    private int extractDiceRollValue(String line)
    {
        try
        {
            Matcher matcher = IF_DICE_ROLL_VALUE_PATTERN.matcher(line);

            if (matcher.find())
            {
                return Integer.parseInt(matcher.group(1));
            }

            throw new TwineFormatException("Invalid dice roll line. Invalid dice value. " + line);
        }
        catch (NumberFormatException e)
        {
            throw new TwineFormatException("Invalid dice roll line. Invalid dice value. " + line, e);
        }
    }

    private TwineDialogOption obtainDialogOption(TwinePassage passage, int id)
    {
        return passage.getDialogOptions()
                      .stream()
                      .filter(tdo -> tdo.getId() == id)
                      .findFirst().orElse(new TwineDialogOption(id));
    }

    private int extractDialogOptionId(String line)
    {
        try
        {
            Matcher matcher = DIALOG_OPTION_ID_PATTERN.matcher(line);

            if (matcher.find())
            {
                return Integer.parseInt(matcher.group(1));
            }

            throw new TwineFormatException("Invalid dialog option line. Invalid id. " + line);
        }
        catch (NumberFormatException e)
        {
            throw new TwineFormatException("Invalid dialog option line. Invalid id. " + line, e);
        }
    }

    private String extractDialogOptionCondition(String line)
    {
        Matcher matcher = DIALOG_OPTION_CONDITION_PATTERN.matcher(line);

        if (matcher.find())
        {
            return matcher.group(1);
        }

        return null;
    }

    private String extractDialogOptionText(String line)
    {
        Matcher matcher = DIALOG_OPTION_TEXT_PATTERN.matcher(line);

        if (matcher.find())
        {
            return matcher.group(1);
        }

        throw new TwineFormatException("Invalid dialog option line. Missing text part. " + line);
    }

    private TwineAction extractDialogOptionAction(String line)
    {
        String actionText = line.substring(line.indexOf("]") + 1);

        if (lineStartsWith(actionText, "[[") && lineEndsWith(actionText, "]]"))
        {
            return extractSwitchPassage(actionText);
        }
        else if (lineStartsWith(actionText, "(set:"))
        {
            return extractValueSetter(actionText);
        }

        throw new TwineFormatException("Invalid dialog option line. Missing or invalid action. " + line);
    }

    private TwineSetValue extractValueSetter(String line)
    {
        String setter = line.substring(6, line.length() - 1).trim();
        return new TwineSetValue(setter);
    }

    private TwineSwitchPassage extractSwitchPassage(String line)
    {
        String passage = formatString(line.trim()).trim();
        return new TwineSwitchPassage(passage);
    }

    private String formatString(String name)
    {
        String newName = name.replace(" ", "_");
        newName = newName.replaceAll("[^a-zA-Z_0-9]", "");

        return newName.toLowerCase();
    }

    private String extractStoryData(String text)
    {
        Matcher matcher = STORY_DATA_PATTERN.matcher(text);

        if (matcher.find())
        {
            // html allows flag attributes without a vlaue while xml does not
            // Twine will add "hidden" attributes which dont serve a purpose for this converter
            // just remove them
            return matcher.group().replace(" hidden", "");
        }

        return null;
    }
}