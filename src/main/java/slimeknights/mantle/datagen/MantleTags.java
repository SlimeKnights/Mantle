package slimeknights.mantle.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.Mantle;

/** List of all tags used directly by mantle */
public class MantleTags {
  public static void init() {
    Blocks.init();
    Items.init();
    Fluids.init();
    BlockEntities.init();
  }

  public static class Blocks {
    private static void init() {}
    /** Blocks in this tag will show fluid tooltips when targeted */
    public static final TagKey<Block> GAUGES = tag("gauges");
    /**
     * Blocks in this tag will show the fluid of attached block.
     * Must have {@link net.minecraft.world.level.block.state.properties.BlockStateProperties#FACING}.
     * @see slimeknights.mantle.block.GaugeBlock
     */
    public static final TagKey<Block> ATTACHED_GAUGES = tag("gauges/attached");
    /** Blocks in this tag will show the fluid contained. Must have a block entity with a fluid handler capability. */
    public static final TagKey<Block> GAUGE_TANKS = tag("gauges/tank");

    /** Adds a mantle domain tag */
    private static TagKey<Block> tag(String name) {
      return TagKey.create(Registries.BLOCK, Mantle.getResource(name));
    }
  }

  public static class Items {
    private static void init() {}
    /** Tag of empty glass bottles that would contain a splash potion */
    public static final TagKey<Item> SPLASH_BOTTLE = common("bottles/splash");
    /** Tag of empty glass bottles that would contain a lingering potion */
    public static final TagKey<Item> LINGERING_BOTTLE = common("bottles/lingering");
    /** Items in this tag remain on the player after death */
    public static final TagKey<Item> SOULBOUND = ItemTags.create(Mantle.getResource("soulbound"));

    /** Adds a common domain tag */
    private static TagKey<Item> common(String name) {
      return TagKey.create(Registries.ITEM, Mantle.commonResource(name));
    }
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

    // common fluids with Mantle compat
    /** Anything classified as a soup, notably used for tooltips */
    public static final TagKey<Fluid> SOUP = tag("soup");
    /** Fluid inside honey bottles, at 250mb per bottle */
    public static final TagKey<Fluid> HONEY = common("honey");
    /** Fluid inside beetroot soup bowls, at 250mb per bowl */
    public static final TagKey<Fluid> BEETROOT_SOUP = common("beetroot_soup");
    /** Fluid inside mushroom stew bowls, at 250mb per bowl */
    public static final TagKey<Fluid> MUSHROOM_STEW = common("mushroom_stew");
    /** Fluid inside rabbit stew bowls, at 250mb per bowl */
    public static final TagKey<Fluid> RABBIT_STEW = common("rabbit_stew");
    /** Fluid inside potion bottles, at 250mb per bottle */
    public static final TagKey<Fluid> POTION = common("potion");


    /** Adds a mantle domain tag */
    private static TagKey<Fluid> tag(String name) {
      return TagKey.create(Registries.FLUID, Mantle.getResource(name));
    }

    /** Adds a common domain tag */
    private static TagKey<Fluid> common(String name) {
      return TagKey.create(Registries.FLUID, Mantle.commonResource(name));
    }
  }

  public static class BlockEntities {
    private static void init() {}

    /**
     * Any block entities in this tag will show just the fluid name, no capacity when viewed in a gauge.
     * Useful for blocks that don't fully sync the fluid to client, such as channels.
     */
    public static final TagKey<BlockEntityType<?>> HIDES_GAUGE_AMOUNT = tag("hides_gauge_amount");

    /** Any block entities in this tag will not show any gauge information. */
    public static final TagKey<BlockEntityType<?>> GAUGE_BLACKLIST = tag("gauge_blacklist");


    /** Adds a mantle domain tag */
    private static TagKey<BlockEntityType<?>> tag(String name) {
      return TagKey.create(Registries.BLOCK_ENTITY_TYPE, Mantle.getResource(name));
    }
  }

  public static class MenuTypes {
    private static void init() {}

    /** Any menus in this tag allow closing during {@link Item#overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction, Player, SlotAccess)} to open another menu or screen. */
    public static final TagKey<MenuType<?>> REPLACEABLE = TagKey.create(Registries.MENU, Mantle.getResource("replaceable"));
  }
}
