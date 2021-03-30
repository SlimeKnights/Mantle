package slimeknights.mantle.registration.deferred;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register to handle registering blocks with possible item forms
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BlockDeferredRegister extends DeferredRegisterWrapper<Block> {
  protected final DeferredRegister<Item> itemRegister;
  public BlockDeferredRegister(String modID) {
    super(ForgeRegistries.BLOCKS, modID);
    this.itemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, modID);
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    itemRegister.register(bus);
  }


  /* Blocks with no items */

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block registry object
   */
  public <B extends Block> RegistryObject<B> registerNoItem(String name, Supplier<? extends B> block) {
    return register.register(name, block);
  }

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param props  Block properties
   * @return  Block registry object
   */
  public RegistryObject<Block> registerNoItem(String name, Block.Properties props) {
    return registerNoItem(name, () -> new Block(props));
  }


  /* Block item pairs */

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name   Block ID
   * @param block  Block supplier
   * @param item   Function to create a BlockItem from a Block
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  public <B extends Block> ItemObject<B> register(String name, Supplier<? extends B> block, final Function<? super B, ? extends BlockItem> item) {
    RegistryObject<B> blockObj = registerNoItem(name, block);
    itemRegister.register(name, () -> item.apply(blockObj.get()));
    return new ItemObject<>(blockObj);
  }

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name        Block ID
   * @param blockProps  Block supplier
   * @param item        Function to create a BlockItem from a Block
   * @return  Block item registry object pair
   */
  public ItemObject<Block> register(String name, Block.Properties blockProps, Function<? super Block, ? extends BlockItem> item) {
    return register(name, () -> new Block(blockProps), item);
  }


  /* Specialty */

  /**
   * Registers a building block with slabs and stairs, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public BuildingBlockObject registerBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    ItemObject<Block> blockObj = register(name, block, item);
    return new BuildingBlockObject(
        blockObj,
        this.register(name + "_slab", () -> new SlabBlock(AbstractBlock.Properties.from(blockObj.get())), item),
        this.register(name + "_stairs", () -> new StairsBlock(() -> blockObj.get().getDefaultState(), AbstractBlock.Properties.from(blockObj.get())), item));
  }

  /**
   * Registers a block with slab, and stairs
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  BuildingBlockObject class that returns different block types
   */
  public BuildingBlockObject registerBuilding(String name, Block.Properties props, Function<? super Block, ? extends BlockItem> item) {
    ItemObject<Block> blockObj = register(name, props, item);
    return new BuildingBlockObject(blockObj,
      register(name + "_slab", () -> new SlabBlock(props), item),
      register(name + "_stairs", () -> new StairsBlock(() -> blockObj.get().getDefaultState(), props), item)
    );
  }

  /**
   * Registers a building block with slabs, stairs and wall, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public WallBuildingBlockObject registerWallBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject obj = this.registerBuilding(name, block, item);
    return new WallBuildingBlockObject(obj, this.register(name + "_wall", () -> new WallBlock(AbstractBlock.Properties.from(obj.get())), item));
  }

  /**
   * Registers a block with slab, stairs, and wall
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  StoneBuildingBlockObject class that returns different block types
   */
  public WallBuildingBlockObject registerWallBuilding(String name, Block.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return new WallBuildingBlockObject(
      registerBuilding(name, props, item),
      register(name + "_wall", () -> new WallBlock(props), item)
    );
  }

  /**
   * Registers a building block with slabs, stairs and wall, using a custom block
   * @param name   Block name
   * @param block  Block supplier
   * @param item   Item block, used for all variants
   * @return  Building block object
   */
  public FenceBuildingBlockObject registerFenceBuilding(String name, Supplier<? extends Block> block, Function<? super Block, ? extends BlockItem> item) {
    BuildingBlockObject obj = this.registerBuilding(name, block, item);
    return new FenceBuildingBlockObject(obj, this.register(name + "_fence", () -> new FenceBlock(AbstractBlock.Properties.from(obj.get())), item));
  }

  /**
   * Registers a block with slab, stairs, and fence
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  WoodBuildingBlockObject class that returns different block types
   */
  public FenceBuildingBlockObject registerFenceBuilding(String name, Block.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return new FenceBuildingBlockObject(
      registerBuilding(name, props, item),
      register(name + "_fence", () -> new FenceBlock(props), item)
    );
  }

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values    Enum values to use for this block
   * @param name      Name of the block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <T extends Enum<T> & IStringSerializable, B extends Block> EnumObject<T,B> registerEnum(
      T[] values, String name, Function<T,? extends B> mapper, Function<? super B, ? extends BlockItem> item) {
    return registerEnum(values, name, (fullName, value) -> register(fullName, () -> mapper.apply(value), item));
  }

  /**
   * Registers a block with multiple variants, suffixing the name with the value name
   * @param name      Name of the block
   * @param values    Enum values to use for this block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <T extends Enum<T> & IStringSerializable, B extends Block> EnumObject<T,B> registerEnum(
      String name, T[] values, Function<T,? extends B> mapper, Function<? super B, ? extends BlockItem> item) {
    return registerEnum(name, values, (fullName, value) -> register(fullName, () -> mapper.apply(value), item));
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param tagName        Name to use for tags for this block
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    ItemObject<Block> block = register(name + "_block", blockSupplier, blockItem);
    Supplier<Item> itemSupplier = () -> new Item(itemProps);
    RegistryObject<Item> ingot = itemRegister.register(name + "_ingot", itemSupplier);
    RegistryObject<Item> nugget = itemRegister.register(name + "_nugget", itemSupplier);
    return new MetalItemObject(tagName, block, ingot, nugget);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockSupplier, blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param tagName     Name to use for tags for this block
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, AbstractBlock.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, tagName, () -> new Block(blockProps), blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, AbstractBlock.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockProps, blockItem, itemProps);
  }
}
