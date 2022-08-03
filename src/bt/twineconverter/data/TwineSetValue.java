package bt.twineconverter.data;

import bt.io.json.JSONBuilder;
import bt.io.json.Jsonable;
import bt.io.xml.ElementBuilder;
import bt.io.xml.XML;
import bt.io.xml.XMLBuilder;
import bt.io.xml.Xmlable;
import org.dom4j.Document;
import org.json.JSONObject;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class TwineSetValue extends TwineAction implements Xmlable, Jsonable
{
    private String setString;

    public TwineSetValue(String setString)
    {
        this.setString = setString;
    }

    public String getSetString()
    {
        return setString;
    }

    public void setSetString(String setString)
    {
        this.setString = setString;
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();
        ElementBuilder element = XML.element("set", this.setString);

        if (this.condition != null)
        {
            element.addAttribute("condition", this.condition);
        }

        builder.addElement(element);

        return builder.toXML();
    }

    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder();

        builder.put("type", "set");
        builder.put("set", this.setString);
        builder.put("order", this.order);

        if (this.condition != null)
        {
            builder.put("condition", this.condition);
        }

        return builder.toJSON();
    }
}