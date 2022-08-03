package bt.twineconverter.data;

import bt.io.json.Jsonable;
import bt.io.xml.Xmlable;

/**
 * @author Lukas Hartwig
 * @since 15.05.2022
 */
public abstract class TwineAction implements Xmlable, Jsonable
{
    protected String condition;

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }
}