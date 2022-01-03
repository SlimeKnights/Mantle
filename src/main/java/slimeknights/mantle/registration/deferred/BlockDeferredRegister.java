package slimeknights.mantle.registration.deferred;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.block.MantleStandingSignBlock;
import slimeknights.mantle.block.MantleWallSignBlock;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.block.WoodenDoorBlock;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.item.BurnableBlockItem;
import slimeknights.mantle.item.BurnableSignItem;
import slimeknights.mantle.item.BurnableTallBlockItem;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

import java.util.function.BiFunction;
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
  public RegistryObject<Block> registerNoItem(String name, BlockBehaviour.Properties props) {
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
  public ItemObject<Block> register(String name, BlockBehaviour.Properties blockProps, Function<? super Block, ? extends BlockItem> item) {
    return register(name, () -> new Block(blockProps), item);
  }


  /* Building */

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
        this.register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(blockObj.get())), item),
        this.register(name + "_stairs", () -> new StairBlock(() -> blockObj.get().defaultBlockState(), BlockBehaviour.Properties.copy(blockObj.get())), item));
  }

  /**
   * Registers a block with slab, and stairs
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  BuildingBlockObject class that returns different block types
   */
  public BuildingBlockObject registerBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    ItemObject<Block> blockObj = register(name, props, item);
    return new BuildingBlockObject(blockObj,
      register(name + "_slab", () -> new SlabBlock(props), item),
      register(name + "_stairs", () -> new StairBlock(() -> blockObj.get().defaultBlockState(), props), item)
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
    return new WallBuildingBlockObject(obj, this.register(name + "_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(obj.get())), item));
  }

  /**
   * Registers a block with slab, stairs, and wall
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  StoneBuildingBlockObject class that returns different block types
   */
  public WallBuildingBlockObject registerWallBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
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
    return new FenceBuildingBlockObject(obj, this.register(name + "_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(obj.get())), item));
  }

  /**
   * Registers a block with slab, stairs, and fence
   * @param name      Name of the block
   * @param props     Block properties
   * @param item      Function to get an item from the block
   * @return  WoodBuildingBlockObject class that returns different block types
   */
  public FenceBuildingBlockObject registerFenceBuilding(String name, BlockBehaviour.Properties props, Function<? super Block, ? extends BlockItem> item) {
    return new FenceBuildingBlockObject(
      registerBuilding(name, props, item),
      register(name + "_fence", () -> new FenceBlock(props), item)
    );
  }

  /**
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param behaviorCreator  Logic to create the behavior
   * @param flammable        If true, this wood type is flammable
   * @param group            Item group
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Function<WoodVariant,BlockBehaviour.Properties> behaviorCreator, boolean flammable, CreativeModeTab group) {
    WoodType woodType = WoodType.create(resourceName(name));
    RegistrationHelper.registerWoodType(woodType);
    Item.Properties itemProps = new Item.Properties().tab(group);

    // many of these are already burnable via tags, but simplier to set them all here
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableItem;
    Function<? super Block, ? extends BlockItem> burnableTallItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableSignItem;
    Item.Properties signProps = new Item.Properties().stacksTo(16).tab(group);
    if (flammable) {
      burnableItem     = burnTime -> block -> new BurnableBlockItem(block, itemProps, burnTime);
      burnableTallItem = block -> new BurnableTallBlockItem(block, itemProps, 200);
      burnableSignItem = (standing, wall) -> new BurnableSignItem(signProps, standing, wall, 200);
    } else {
      Function<? super Block, ? extends BlockItem> defaultItemBlock = block -> new BlockItem(block, itemProps);
      burnableItem = burnTime -> defaultItemBlock;
      burnableTallItem = block -> new DoubleHighBlockItem(block, itemProps);
      burnableSignItem = (standing, wall) -> new SignItem(signProps, standing, wall);
    }

    // planks
    Function<? super Block, ? extends BlockItem> burnable300 = burnableItem.apply(300);
    BlockBehaviour.Properties planksProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).strength(2.0f, 3.0f);
    BuildingBlockObject planks = registerBuilding(name + "_planks", planksProps, block -> burnableItem.apply(block instanceof SlabBlock ? 150 : 300).apply(block));
    ItemObject<FenceBlock> fence = register(name + "_fence", () -> new FenceBlock(Properties.copy(planks.get())), burnable300);
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).strength(2.0f));
    ItemObject<RotatedPillarBlock> strippedLog = register("stripped_" + name + "_log", stripped, burnable300);
    ItemObject<RotatedPillarBlock> strippedWood = register("stripped_" + name + "_wood", stripped, burnable300);
    ItemObject<RotatedPillarBlock> log = register(name + "_log", () -> new StrippableLogBlock(strippedLog, behaviorCreator.apply(WoodBlockObject.WoodVariant.LOG).strength(2.0f)), burnable300);
    ItemObject<RotatedPillarBlock> wood = register(name + "_wood", () -> new StrippableLogBlock(strippedWood, behaviorCreator.apply(WoodBlockObject.WoodVariant.WOOD).strength(2.0f)), burnable300);

    // doors
    ItemObject<DoorBlock> door = register(name + "_door", () -> new WoodenDoorBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).strength(3.0F).noOcclusion()), burnableTallItem);
    ItemObject<TrapDoorBlock> trapdoor = register(name + "_trapdoor", () -> new TrapDoorBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)), burnable300);
    ItemObject<FenceGateBlock> fenceGate = register(name + "_fence_gate", () -> new FenceGateBlock(planksProps), burnable300);
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).noCollission().strength(0.5F);
    ItemObject<PressurePlateBlock> pressurePlate = register(name + "_pressure_plate", () -> new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), burnable300);
    ItemObject<WoodButtonBlock> button = register(name + "_button", () -> new WoodButtonBlock(redstoneProps), burnableItem.apply(100));
    // signs
    RegistryObject<StandingSignBlock> standingSign = registerNoItem(name + "_sign", () -> new MantleStandingSignBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).noCollission().strength(1.0F), woodType));
    RegistryObject<WallSignBlock> wallSign = registerNoItem(name + "_wall_sign", () -> new MantleWallSignBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).noCollission().strength(1.0F).lootFrom(standingSign), woodType));
    // tell mantle to inject these into the TE
    MantleSignBlockEntity.registerSignBlock(standingSign);
    MantleSignBlockEntity.registerSignBlock(wallSign);
    // sign is included automatically in asItem of the standing sign
    this.itemRegister.register(name + "_sign", () -> burnableSignItem.apply(standingSign.get(), wallSign.get()));
    // finally, return
    return new WoodBlockObject(resource(name), woodType, planks, log, strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button, standingSign, wallSign);
  }


  /* Enum */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values    Enum values to use for this block
   * @param name      Name of the block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T,B> registerEnum(
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
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T,B> registerEnum(
      String name, T[] values, Function<T,? extends B> mapper, Function<? super B, ? extends BlockItem> item) {
    return registerEnum(name, values, (fullName, value) -> register(fullName, () -> mapper.apply(value), item));
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values  Enum value list
   * @param name    Suffix after value name
   * @param mapper  Function to map types to blocks
   * @param <T>  Type of enum
   * @param <B>  Type of block
   * @return  Enum object
   */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T, B> registerEnumNoItem(T[] values, String name, Function<T, ? extends B> mapper) {
    return registerEnum(values, name, (fullName, value) -> registerNoItem(fullName, () -> mapper.apply(value)));
  }


  /* Metal */

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
  public MetalItemObject registerMetal(String name, String tagName, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
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
  public MetalItemObject registerMetal(String name, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockProps, blockItem, itemProps);
  }
}
