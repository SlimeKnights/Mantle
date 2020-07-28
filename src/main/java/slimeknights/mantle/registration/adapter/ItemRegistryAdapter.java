package slimeknights.mantle.registration.adapter;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.item.TooltipItem;
import slimeknights.mantle.registration.ItemProperties;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides utility registration methods when registering itemblocks.
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class ItemRegistryAdapter extends EnumRegistryAdapter<Item> {
  private final Item.Properties defaultProps;

  /**
   * Registers a new item registry adapter with default mod ID and item properties
   * @param registry  Item registry instance
   */
  public ItemRegistryAdapter(IForgeRegistry<Item> registry) {
    this(registry, null);
  }

  /**
   * Registers a new item registry adapter with default mod ID
   * @param registry      Item registry instance
   * @param defaultProps  Default item properties
   */
  public ItemRegistryAdapter(IForgeRegistry<Item> registry, @Nullable Item.Properties defaultProps) {
    super(registry);
    if (defaultProps == null) {
      this.defaultProps = new Item.Properties();
    } else {
      this.defaultProps = defaultProps;
    }
  }

  /**
   * Registers a new item registry adapter with a specific mod ID
   * @param registry      Item registry instance
   * @param modid         Mod ID override
   * @param defaultProps  Default item properties
   */
  public ItemRegistryAdapter(IForgeRegistry<Item> registry, String modid, @Nullable Item.Properties defaultProps) {
    super(registry, modid);
    if (defaultProps == null) {
      this.defaultProps = new Item.Properties();
    } else {
      this.defaultProps = defaultProps;
    }
  }

  /* Item helpers */

  /**
   * Registers a generic tooltip item using the default props
   * @param name  Item name
   * @return  Registered item
   */
  public TooltipItem register(String name) {
    return register(defaultProps, name);
  }

  /**
   * Registers a generic tooltip item from the given props
   * @param props  Item properties
   * @param name   Item name
   * @return  Registered item
   */
  public TooltipItem register(Item.Properties props, String name) {
    return register(new TooltipItem(props), name);
  }

  /**
   * Registers an item with the default properties
   * @param constructor  Item constructor
   * @param name         Item name
   * @param <T>          Item type
   * @return  Registered item
   */
  public <T extends Item> T register(Function<Properties,T> constructor, String name) {
    return register(constructor.apply(defaultProps), name);
  }


  /* Standard block items */

  /**
   * Registers a generic item block for a block.
   * If your block does not have its own item, just use this method to make it available as an item.
   * The item uses the same name as the block for registration.
   * The registered BlockItem has tooltip support by default, see {@link BlockTooltipItem}
   * It will be added to the creative itemgroup passed in in the constructor. If you want a different one, use the method with a ItemGroup parameter.
   *
   * @param block The block you want to have an item for
   * @return The registered item for the block
   */
  public BlockItem registerBlockItem(Block block) {
    return registerBlockItem(block, defaultProps);
  }

  /**
   * Registers a block item with default properties using the given constructor
   * @param block        Block instance
   * @param constructor  Constructor
   * @param <T>          Result block item type
   * @return  Registered block item
   */
  public <T extends BlockItem> T registerBlockItem(Block block, BiFunction<Block,Properties,T> constructor) {
    return register(constructor.apply(block, defaultProps), block);
  }

  /**
   * Same as the variant without ItemGroup, but registers it for the given itemgroup.
   * @param block  The block you want to have an item for
   * @param props  Item properties for the block
   */
  public BlockItem registerBlockItem(Block block, Item.Properties props) {
    return register(new BlockTooltipItem(block, props), block);
  }

  /**
   * Shortcut method to register your own BlockItem, registering with the same name as the block it represents.
   * @param blockItem Item block instance to register
   * @return Registered item block, should be the same as teh one passed in.
   */
  public <T extends BlockItem> T registerBlockItem(T blockItem) {
    return register(blockItem, blockItem.getBlock());
  }

  /* Block wrappers */

  /**
   * Registers block items for all entries in a building block object
   * @param object  Building block object instance
   */
  public void registerBlockItem(BuildingBlockObject object) {
    registerBlockItem(object.get());
    registerBlockItem(object.getSlab());
    registerBlockItem(object.getStairs());
  }

  /**
   * Registers block items for all entries in a wall building block object
   * @param object  Building block object instance
   */
  public void registerBlockItem(WallBuildingBlockObject object) {
    registerBlockItem((BuildingBlockObject)object);
    registerBlockItem(object.getWall());
  }

  /**
   * Registers block items for all entries in a fence building block object
   * @param object  Building block object instance
   */
  public void registerBlockItem(FenceBuildingBlockObject object) {
    registerBlockItem((BuildingBlockObject)object);
    registerBlockItem(object.getFence());
  }

  /**
   * Registers block items for an enum object
   * @param enumObject  Enum object instance
   */
  public void registerBlockItem(EnumObject<?, ? extends Block> enumObject) {
    enumObject.values().forEach(this::registerBlockItem);
  }


  /* Misc */

  /**
   * Registers the bucket for a fluid
   * @param fluid     Fluid supplier
   * @param baseName  Fluid name, unfortunately cannot be fetched from the fluid as it does not exist yet
   * @return  Bucket instance
   */
  public BucketItem registerBucket(Supplier<? extends Fluid> fluid, String baseName) {
    return register(new BucketItem(fluid, ItemProperties.BUCKET_PROPS), baseName + "_bucket");
  }

  /**
   * Registers a spawn egg for the entity type
   * @param entity     Entity type. Will be unregistered at this point
   * @param primary    Primary color
   * @param secondary  Secondary color
   * @param baseName   Entity name, as it may or may not be present in the entity type
   * @return  Spawn egg item instance
   */
  public SpawnEggItem registerSpawnEgg(EntityType<?> entity, int primary, int secondary, String baseName) {
    return register(new SpawnEggItem(entity, primary, secondary, ItemProperties.EGG_PROPS), baseName + "_spawn_egg");
  }
}
