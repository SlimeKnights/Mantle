package mantle.event;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;

public abstract class Migration
{
    private ModContainer mod;

    private String modID;

    /*
     * Store the mod ID so we can convert e.g "item" to "mod:item" later.
     */
    public Migration(ModContainer m)
    {
        m.getModId();
    }

    /*
     * Add a new migration to the internal replacement map e.g. "olditem" -> "newitem" (without ID prefix!).
     * Use an encapsulated object of form Map<String,String> to store this.
     * The prefix will be created using the ID given during construction during processing.
     */
    public void addMigration (String old, String newName)
    {
    }

    /*
     * Register an old ID that's been removed entirely. This isn't suitable for renaming operations!
     * Store this in a List<String> seperate to the rename mapping.
     */
    public void addRemoval (String id)
    {
    };

    /*
     * This works on the assumption the FML event will have a list of string IDs that are missing.
     * During this phase, check for old IDs and remap as needed, or destroy ones marked for removal.
     */
    public void processMigrationEvent (FMLMissingMappingsEvent event)
    {
        event.applyModContainer(mod);
        for (MissingMapping miss : event.get())
        {
        }
    }

}