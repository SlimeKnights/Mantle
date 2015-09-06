package mantle.event;

import static mantle.lib.CoreRepo.logger;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class Migration
{
    Map<String, Item> remapItem = Maps.newHashMap();

    Map<String, Block> remapBlock = Maps.newHashMap();

    List<String> ignore = Lists.newArrayList();

    private ModContainer mod;

    private String modID;

    /*
     * Store the mod ID so we can convert e.g "item" to "mod:item" later.
     */
    public Migration(ModContainer m)
    {
        this.mod = m;
        this.modID = m.getModId();
    }

    /*
     * Add a new migration to the internal replacement map e.g. "olditem" -> "newitem" (without ID prefix!).
     * Use an encapsulated object of form Map<String,Item> to store this.
     * The prefix will be created using the ID given during construction during processing.
     */
    public void addMigration(String old, Item newItem)
    {
        this.remapItem.put(this.modID + ":" + old, newItem);
    }

    /*
     * Add a new migration to the internal replacement map e.g. "olditem" -> "newitem" (without ID prefix!).
     * Use an encapsulated object of form Map<String,Block> to store this.
     * The prefix will be created using the ID given during construction during processing.
     */
    public void addMigration(String old, Block newBlock)
    {
        this.remapBlock.put(this.modID + ":" + old, newBlock);
    }

    /*
     * Register an old ID that's been removed entirely. This isn't suitable for renaming operations!
     * Store this in a List<String> seperate to the rename mapping.
     * @param id ID to be removed (without ID prefix!).
     */
    public void addRemoval(String id)
    {
        this.ignore.add(this.modID + ":" + id);
    }

    /*
     * This must be called from your mod's main class as it requires @EventHandler notation
     * This works on the assumption the FML event will have a list of string IDs that are missing.
     * During this phase, check for old IDs and remap as needed, or destroy ones marked for removal.
     */
    public void processMigrationEvent(FMLMissingMappingsEvent event)
    {
        if ((!this.remapBlock.isEmpty() || !this.remapItem.isEmpty()))
        {
            event.applyModContainer(this.mod);
            List<MissingMapping> l = event.get();
            if (l != null)
            {
                for (MissingMapping miss : event.get())
                {
                    if (this.remapItem.containsKey(miss.name))
                    {
                        miss.remap(this.remapItem.get(miss.name));
                    }
                    else if (this.remapBlock.containsKey(miss.name))
                    {
                        miss.remap(this.remapBlock.get(miss.name));
                    }
                    else if (this.ignore.contains(miss.name))
                    {
                        miss.ignore();
                    }
                }
            }
            else
            {
                logger.error("migration list from FML empty for mod " + this.modID);
            }

        }
    }
}
