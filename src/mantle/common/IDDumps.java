package mantle.common;

import static mantle.lib.CoreRepo.logger;
import mantle.lib.CoreConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.BiomeGenBase;

public class IDDumps
{
    public static void dump(){
        if (CoreConfig.dumpBiomeIDs)
        {
            for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
            {
                if (b != null && b.biomeName != null)
                {
                    logger.info("Biome ID & Name: " + b.biomeID + " " + b.biomeName);
                }
            }
        }
        if (CoreConfig.dumpPotionIDs)
        {
            for (Potion p : Potion.potionTypes)
            {
                if (p != null && p.getName() != null)
                {

                    logger.info("Potion ID & name: " + p.getId() + " " + p.getName());
                }
            }
        }
        if (CoreConfig.dumpEnchantIDs)
        {
            for (Enchantment e : Enchantment.enchantmentsList)
            {
                if (e != null && e.getName() != null)
                {

                    logger.info("Enchantment ID & name: " + e.effectId + " " + e.getName());
                }
            }
        }
    }

}
