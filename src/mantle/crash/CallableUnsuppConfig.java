package mantle.crash;

import cpw.mods.fml.common.ICrashCallable;

import java.util.List;

// Crash handler for when an unsupported mod is running alongside us
public class CallableUnsuppConfig implements ICrashCallable
{

    private String modId;
    private List<String> modIds;

    public CallableUnsuppConfig(String modId, List<String> modIds)
    {
        this.modId = modId;
        this.modIds = modIds;
    }

    @Override
    public String getLabel ()
    {
        return modId + " Environment";
    }

    @Override
    public String call () throws Exception
    {
        String str = "DO NOT REPORT THIS CRASH! Unsupported mods in environment: ";
        Boolean firstEntry = true;
        for (String id : modIds)
        {
            str = str + (firstEntry ? id : ", " + id);
            firstEntry = false;
        }

        return str;
    }

}