package bt.twineconverter.data;

import bt.io.json.JSONBuilder;
import org.dom4j.Document;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Hartwig
 * @since 22.08.2022
 */
public class GenericTwineAction extends TwineAction
{
    private Map<String, String> values;
    private String type;

    public GenericTwineAction(String type)
    {
        this.type = type;
        this.values = new HashMap<>();
    }

    public void addValue(String key, String value)
    {
        this.values.put(key.toLowerCase(), value);
    }

    @Override
    public JSONObject toJSON()
    {
        JSONBuilder builder = new JSONBuilder();

        builder.put("type", this.type);
        builder.put("order", this.order);

        if (this.condition != null)
        {
            builder.put("condition", this.condition);
        }

        for (String key : this.values.keySet())
        {
            builder.put(key, this.values.get(key));
        }

        return builder.toJSON();
    }

    @Override
    public Document toXML()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "type: " + this.type + " values: " + this.values.toString();
    }
}