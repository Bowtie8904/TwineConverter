package bt.twineconverter.data;

import bt.io.json.JSONBuilder;
import bt.io.json.Jsonable;
import bt.io.xml.ElementBuilder;
import bt.io.xml.XML;
import bt.io.xml.XMLBuilder;
import bt.io.xml.Xmlable;
import org.dom4j.Document;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class TwineDialogOption implements Xmlable, Jsonable
{
    private int id;
    private String text;
    private List<TwineAction> actions;

    public TwineDialogOption(int id)
    {
        this.id = id;
        this.actions = new LinkedList<>();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public List<TwineAction> getActions()
    {
        return actions;
    }

    public void setActions(List<TwineAction> actions)
    {
        this.actions = actions;
    }

    public void addAction(TwineAction action)
    {
        this.actions.add(action);
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

        TwineDialogOption that = (TwineDialogOption)o;

        return Objects.equals(this.id, that.id);
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();

        ElementBuilder element = XML.element("option")
                                    .addAttribute("id", String.valueOf(this.id));

        element.addElement(XML.element("text", this.text));

        ElementBuilder actionsElement = XML.element("actions");

        for (TwineAction action : this.actions)
        {
            actionsElement.addElement(action.toXML());
        }

        element.addElement(actionsElement);

        builder.addElement(element);

        return builder.toXML();
    }

    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder();

        builder.put("type", "option");
        builder.put("id", this.id);
        builder.put("text", this.text);
        builder.put("actions", this.actions.toArray());

        return builder.toJSON();
    }
}