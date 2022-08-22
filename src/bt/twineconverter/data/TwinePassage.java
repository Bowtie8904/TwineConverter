package bt.twineconverter.data;

import bt.io.json.JSONBuilder;
import bt.io.json.Jsonable;
import bt.io.xml.ElementBuilder;
import bt.io.xml.XML;
import bt.io.xml.XMLBuilder;
import bt.io.xml.Xmlable;
import org.dom4j.Document;
import org.json.JSONObject;

import java.util.*;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class TwinePassage implements Xmlable, Jsonable
{
    private int id;
    private String name;
    private String text;
    private Set<TwineDialogOption> dialogOptions;
    private Set<TwineCondition> conditions;
    private List<TwineAction> defaultConditions;
    private List<GenericTwineAction> genericActions;
    private List<TwineSetValue> valueSetters;
    private TwineSwitchPassage passageSwitch;

    public TwinePassage(int id, String name)
    {
        this.id = id;
        this.name = name;
        this.dialogOptions = new HashSet<>();
        this.conditions = new HashSet<>();
        this.defaultConditions = new LinkedList<>();
        this.valueSetters = new LinkedList<>();
        this.genericActions = new LinkedList<>();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public Set<TwineDialogOption> getDialogOptions()
    {
        return dialogOptions;
    }

    public void setDialogOptions(Set<TwineDialogOption> dialogOptions)
    {
        this.dialogOptions = dialogOptions;
    }

    public void addDialogOption(TwineDialogOption dialogOption)
    {
        this.dialogOptions.add(dialogOption);
    }

    public Set<TwineCondition> getConditions()
    {
        return conditions;
    }

    public void setConditions(Set<TwineCondition> conditions)
    {
        this.conditions = conditions;
    }

    public void addCondition(TwineCondition condition)
    {
        this.conditions.add(condition);
    }

    public List<TwineAction> getDefaultConditions()
    {
        return defaultConditions;
    }

    public void setDefaultConditions(List<TwineAction> defaultConditions)
    {
        this.defaultConditions = defaultConditions;
    }

    public void addDefaultCondition(TwineAction defaultCondition)
    {
        this.defaultConditions.add(defaultCondition);
    }

    public List<TwineSetValue> getValueSetters()
    {
        return valueSetters;
    }

    public void setValueSetters(List<TwineSetValue> valueSetters)
    {
        this.valueSetters = valueSetters;
    }

    public void addValueSetter(TwineSetValue valueSetter)
    {
        this.valueSetters.add(valueSetter);
    }

    public List<GenericTwineAction> getGenericActions()
    {
        return this.genericActions;
    }

    public void setGenericActions(List<GenericTwineAction> actions)
    {
        this.genericActions = actions;
    }

    public void addGenericAction(GenericTwineAction action)
    {
        this.genericActions.add(action);
    }

    public TwineSwitchPassage getPassageSwitch()
    {
        return passageSwitch;
    }

    public void setPassageSwitch(TwineSwitchPassage passageSwitch)
    {
        this.passageSwitch = passageSwitch;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (o == null)
        {
            return false;
        }
        else if (o instanceof String name)
        {
            return Objects.equals(this.name, name);
        }
        else if (o instanceof Integer id)
        {
            return Objects.equals(this.id, id);
        }

        TwinePassage that = (TwinePassage)o;

        return Objects.equals(this.name, that.name);
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();

        ElementBuilder element = XML.element("passage")
                                    .addAttribute("id", String.valueOf(this.id))
                                    .addAttribute("name", String.valueOf(this.name));

        element.addElement(XML.element("text", this.text));

        ElementBuilder dialogOptionsElement = XML.element("dialogOptions");

        for (TwineDialogOption option : this.dialogOptions)
        {
            dialogOptionsElement.addElement(option.toXML());
        }

        element.addElement(dialogOptionsElement);

        ElementBuilder conditionalElement = XML.element("conditional");

        for (TwineCondition condition : this.conditions)
        {
            conditionalElement.addElement(condition.toXML());
        }

        ElementBuilder defaultElement = XML.element("default");

        for (TwineAction action : this.defaultConditions)
        {
            defaultElement.addElement(action.toXML());
        }

        conditionalElement.addElement(defaultElement);
        element.addElement(conditionalElement);

        ElementBuilder valueSetterElement = XML.element("valueSetters");

        for (TwineSetValue setter : this.valueSetters)
        {
            valueSetterElement.addElement(setter.toXML());
        }

        element.addElement(valueSetterElement);

        if (this.passageSwitch != null)
        {
            element.addElement(this.passageSwitch);
        }

        builder.addElement(element);

        return builder.toXML();
    }

    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder();

        builder.put("type", "passage");
        builder.put("id", this.id);
        builder.put("name", this.name);
        builder.put("text", this.text);
        builder.put("dialogOptions", this.dialogOptions.toArray());

        JSONBuilder condBuilder = new JSONBuilder();
        condBuilder.put("ifs", this.conditions.toArray());
        condBuilder.put("default", this.defaultConditions.toArray());
        builder.put("conditional", condBuilder.toJSON());

        builder.put("valueSetters", this.valueSetters.toArray());
        builder.put("genericActions", this.genericActions.toArray());

        if (this.passageSwitch != null)
        {
            builder.put("passageSwitch", this.passageSwitch);
        }

        return builder.toJSON();
    }
}