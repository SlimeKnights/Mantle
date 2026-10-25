package slimeknights.mantle.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.Mantle;

/** List of all tags used directly by mantle */
public class MantleTags {
  public static class Blocks {
    /** Blocks in this tag will show fluid tooltips when targeted */
    public static final TagKey<Block> GAUGES = tag("gauges");
    /**
     * Blocks in this tag will show the fluid of attached block.
     * Must have {@link net.minecraft.world.level.block.state.properties.BlockStateProperties#FACING}.
     * @see slimeknights.mantle.block.GaugeBlock
     */
    public static final TagKey<Block> ATTACHED_GAUGES = tag("gauges/attached");
    /** Blocks in this tag will show the fluid contained. Must have a fluid handler capability. */
    public static final TagKey<Block> GAUGE_TANKS = tag("gauges/tank");
    /** Any blocks in this tag will not show any gauge information when a gauge is placed on them */
    public static final TagKey<Block> GAUGE_BLACKLIST = tag("gauge_blacklist");
    /**
     * Any block entities in this tag will show just the fluid name, no capacity when viewed in a gauge.
     * Useful for blocks that don't fully sync the fluid to client, such as channels.
     */
    public static final TagKey<Block> HIDES_GAUGE_AMOUNT = tag("hides_gauge_amount");

    /** Adds a mantle domain tag */
    private static TagKey<Block> tag(String name) {
      return TagKey.create(Registries.BLOCK, Mantle.getResource(name));
    }
  }

  public static class Items {
    /** Tag of empty glass bottles that would contain a splash potion */
    public static final TagKey<Item> SPLASH_BOTTLE = common("bottles/splash");
    /** Tag of empty glass bottles that would contain a lingering potion */
    public static final TagKey<Item> LINGERING_BOTTLE = common("bottles/lingering");
    /** Items in this tag remain on the player after death */
    public static final TagKey<Item> SOULBOUND = tag("soulbound");
    /** Items in this tag will have a fluid transfer recipe generated using {@link IFluidHandlerItem} to fill it. */
    public static final TagKey<Item> EMPTY_FLUID_CONTAINERS = tag("fluid_containers/empty");
    /** Items in this tag will have a fluid transfer recipe generated using {@link IFluidHandlerItem} to empty it. */
    public static final TagKey<Item> FILLED_FLUID_CONTAINERS = tag("fluid_containers/filled");

    /** Adds a common domain tag */
    private static TagKey<Item> common(String name) {
      return TagKey.create(Registries.ITEM, Mantle.commonResource(name));
    }

    /** Adds a common domain tag */
    private static TagKey<Item> tag(String name) {
      return TagKey.create(Registries.ITEM, Mantle.getResource(name));
    }
  }

  public static class Fluids {
    // common fluids with Mantle compat
    /** Anything classified as a soup, notably used for tooltips */
    public static final TagKey<Fluid> SOUP = tag("soup");

    /** Fluids in this tag won't show in lists populated from all fluids such as bucket transfer recipes */
    public static final TagKey<Fluid> HIDE_IN_DISPLAY = tag("hide_in_display");


    /** Adds a mantle domain tag */
    private static TagKey<Fluid> tag(String name) {
      return TagKey.create(Registries.FLUID, Mantle.getResource(name));
    }
  }

  public static class MenuTypes {
    /** Any menus in this tag allow closing during {@link Item#overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction, Player, SlotAccess)} to open another menu or screen. */
    public static final TagKey<MenuType<?>> REPLACEABLE = TagKey.create(Registries.MENU, Mantle.getResource("replaceable"));
  }
}
