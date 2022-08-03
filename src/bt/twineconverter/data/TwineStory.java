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

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public class TwineStory implements Xmlable, Jsonable
{
    private String name;
    private int startPassageId;
    private List<TwinePassage> passages;

    public TwineStory(String name, int startPassageId)
    {
        this.name = name;
        this.startPassageId = startPassageId;
        this.passages = new LinkedList<>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getStartPassageId()
    {
        return startPassageId;
    }

    public void setStartPassageId(int startPassageId)
    {
        this.startPassageId = startPassageId;
    }

    public List<TwinePassage> getPassages()
    {
        return passages;
    }

    public void setPassages(List<TwinePassage> passages)
    {
        this.passages = passages;
    }

    public void addPassage(TwinePassage passage)
    {
        this.passages.add(passage);
    }

    @Override
    public Document toXML()
    {
        XMLBuilder builder = new XMLBuilder();

        ElementBuilder element = XML.element("story")
                                    .addAttribute("name", this.name)
                                    .addAttribute("startPassage", String.valueOf(this.startPassageId));

        ElementBuilder passagesElement = XML.element("passages");

        for (TwinePassage passage : this.passages)
        {
            passagesElement.addElement(passage.toXML());
        }

        element.addElement(passagesElement);

        builder.addElement(element);

        return builder.toXML();
    }

    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder();

        builder.put("type", "story");
        builder.put("startPassage", this.startPassageId);
        builder.put("name", this.name);
        builder.put("passages", this.passages.toArray());

        return builder.toJSON();
    }
}