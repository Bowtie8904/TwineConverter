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
public class TwineCondition implements Xmlable, Jsonable
{
    private String condition;
    private List<TwineAction> actions;

    public TwineCondition(String condition)
    {
        this.condition = condition;
        this.actions = new LinkedList<>();
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
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
        else if (o instanceof String cond)
        {
            return Objects.equals(condition, cond);
        }

        TwineCondition that = (TwineCondition)o;

        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(condition);
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();

        ElementBuilder element = XML.element("if")
                                    .addAttribute("condition", this.condition);

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

        builder.put("type", "if");
        builder.put("condition", this.condition);
        builder.put("actions", this.actions.toArray());

        return builder.toJSON();
    }
}