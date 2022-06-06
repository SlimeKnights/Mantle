package slimeknights.mantle.datagen;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.Mantle;

/** List of all tags used directly by mantle */
public class MantleTags {
  public static void init() {
    Fluids.init();
  }

  public static class Fluids {
    private static void init() {}

    /**
     * This tag represents vanilla water, but is not used by vanilla logic.
     * Means it's not going to be filled with random mod entries that are not water making it safe for recipes
     */
    public static final TagKey<Fluid> WATER = tag("water");
    /**
     * This tag represents vanilla lava, but is not used by vanilla logic.
     * Means it's not going to be filled with random mod entries that are not water making it safe for recipes
     */
    public static final TagKey<Fluid> LAVA = tag("lava");

    private static TagKey<Fluid> tag(String name) {
      return TagKey.create(Registry.FLUID_REGISTRY, Mantle.getResource(name));
    }
  }
}
