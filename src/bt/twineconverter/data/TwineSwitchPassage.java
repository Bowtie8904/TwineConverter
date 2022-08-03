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
public class TwineSwitchPassage extends TwineAction implements Xmlable, Jsonable
{
    private String passage;

    public TwineSwitchPassage(String passage)
    {
        this.passage = passage;
    }

    public String getPassage()
    {
        return passage;
    }

    public void setPassage(String passage)
    {
        this.passage = passage;
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();
        ElementBuilder element = XML.element("next", this.passage);

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

        builder.put("type", "switch");
        builder.put("next", this.passage);

        if (this.condition != null)
        {
            builder.put("condition", this.condition);
        }

        return builder.toJSON();
    }
}