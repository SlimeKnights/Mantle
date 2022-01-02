package slimeknights.mantle.registration.deferred;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.PressurePlateBlock.Sensitivity;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodButtonBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SignItem;
import net.minecraft.item.TallBlockItem;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.block.WoodenDoorBlock;
import slimeknights.mantle.item.BurnableBlockItem;
import slimeknights.mantle.item.BurnableSignItem;
import slimeknights.mantle.item.BurnableTallBlockItem;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

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
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param planksMaterial   Material for the planks
   * @param planksColor      Map color for the planks
   * @param plankSound       Sound for the planks
   * @param planksTool       Tool for the planks
   * @param barkMaterial     Bark material
   * @param barkColor        Map color for the bark
   * @param barkSound        Sound for the bark
   * @param group            Item group
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Material planksMaterial, MaterialColor planksColor, SoundType plankSound, ToolType planksTool, Material barkMaterial, MaterialColor barkColor, SoundType barkSound, ItemGroup group) {
    WoodType woodType = WoodType.create(resourceName(name));
    RegistrationHelper.registerWoodType(woodType);
    Item.Properties itemProps = new Item.Properties().group(group);

    // many of these are already burnable via tags, but simplier to set them all here
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableItem;
    Function<? super Block, ? extends BlockItem> burnableTallItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableSignItem;
    Item.Properties signProps = new Item.Properties().maxStackSize(16).group(group);
    if (barkMaterial.isFlammable()) {
      burnableItem     = burnTime -> block -> new BurnableBlockItem(block, itemProps, burnTime);
      burnableTallItem = block -> new BurnableTallBlockItem(block, itemProps, 200);
      burnableSignItem = (standing, wall) -> new BurnableSignItem(signProps, standing, wall, 200);
    } else {
      Function<? super Block, ? extends BlockItem> defaultItemBlock = block -> new BlockItem(block, itemProps);
      burnableItem = burnTime -> defaultItemBlock;
      burnableTallItem = block -> new TallBlockItem(block, itemProps);
      burnableSignItem = (standing, wall) -> new SignItem(signProps, standing, wall);
    }

    // planks
    Function<? super Block, ? extends BlockItem> burnable300 = burnableItem.apply(300);
    AbstractBlock.Properties planksProps = AbstractBlock.Properties.create(planksMaterial, planksColor).harvestTool(planksTool).hardnessAndResistance(2.0f, 3.0f).sound(plankSound);
    BuildingBlockObject planks = registerBuilding(name + "_planks", planksProps, block -> burnableItem.apply(block instanceof SlabBlock ? 150 : 300).apply(block));
    ItemObject<FenceBlock> fence = register(name + "_fence", () -> new FenceBlock(Properties.from(planks.get())), burnable300);
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(AbstractBlock.Properties.create(planksMaterial, planksColor).harvestTool(planksTool).hardnessAndResistance(2.0f).sound(plankSound));
    ItemObject<RotatedPillarBlock> strippedLog = register("stripped_" + name + "_log", stripped, burnable300);
    ItemObject<RotatedPillarBlock> strippedWood = register("stripped_" + name + "_wood", stripped, burnable300);
    ItemObject<RotatedPillarBlock> log = register(name + "_log", () -> new StrippableLogBlock(strippedLog,
      AbstractBlock.Properties.create(barkMaterial, state -> state.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? planksColor : barkColor)
                              .harvestTool(ToolType.AXE).hardnessAndResistance(2.0f).sound(barkSound)), burnable300);
    ItemObject<RotatedPillarBlock> wood = register(name + "_wood", () -> new StrippableLogBlock(strippedWood, AbstractBlock.Properties.create(barkMaterial, barkColor).harvestTool(ToolType.AXE).hardnessAndResistance(2.0f).sound(barkSound)), burnable300);

    // doors
    ItemObject<DoorBlock> door = register(name + "_door", () -> new WoodenDoorBlock(AbstractBlock.Properties.create(planksMaterial, planksColor).harvestTool(planksTool).hardnessAndResistance(3.0F).sound(plankSound).notSolid()), burnableTallItem);
    ItemObject<TrapDoorBlock> trapdoor = register(name + "_trapdoor", () -> new TrapDoorBlock(AbstractBlock.Properties.create(planksMaterial, planksColor).harvestTool(planksTool).hardnessAndResistance(3.0F).sound(SoundType.WOOD).notSolid().setAllowsSpawn(Blocks::neverAllowSpawn)), burnable300);
    ItemObject<FenceGateBlock> fenceGate = register(name + "_fence_gate", () -> new FenceGateBlock(planksProps), burnable300);
    // redstone
    AbstractBlock.Properties redstoneProps = AbstractBlock.Properties.create(planksMaterial, planksColor).harvestTool(planksTool).doesNotBlockMovement().hardnessAndResistance(0.5F).sound(plankSound);
    ItemObject<PressurePlateBlock> pressurePlate = register(name + "_pressure_plate", () -> new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), burnable300);
    ItemObject<WoodButtonBlock> button = register(name + "_button", () -> new WoodButtonBlock(redstoneProps), burnableItem.apply(100));
    // signs
    RegistryObject<StandingSignBlock> standingSign = registerNoItem(name + "_sign", () -> new StandingSignBlock(AbstractBlock.Properties.create(planksMaterial, planksColor).doesNotBlockMovement().hardnessAndResistance(1.0F).sound(plankSound), woodType));
    RegistryObject<WallSignBlock> wallSign = registerNoItem(name + "_wall_sign", () -> new WallSignBlock(AbstractBlock.Properties.create(planksMaterial, planksColor).doesNotBlockMovement().hardnessAndResistance(1.0F).sound(plankSound).lootFrom(standingSign), woodType));
    // tell mantle to inject these into the TE
    RegistrationHelper.registerSignBlock(standingSign);
    RegistrationHelper.registerSignBlock(wallSign);
    // sign is included automatically in asItem of the standing sign
    this.itemRegister.register(name + "_sign", () -> burnableSignItem.apply(standingSign.get(), wallSign.get()));
    // finally, return
    return new WoodBlockObject(resource(name), woodType, planks, log, strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button, standingSign, wallSign);
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
}
