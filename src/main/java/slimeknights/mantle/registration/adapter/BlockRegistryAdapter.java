package slimeknights.mantle.registration.adapter;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowingFluidBlock;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.block.WoodenDoorBlock;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides utility registration methods when registering blocks.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BlockRegistryAdapter extends EnumRegistryAdapter<Block> {

  /** @inheritDoc */
  public BlockRegistryAdapter(IForgeRegistry<Block> registry) {
    super(registry);
  }

  /** @inheritDoc */
  public BlockRegistryAdapter(IForgeRegistry<Block> registry, String modid) {
    super(registry, modid);
  }

  /**
   * Registers a block override based on the given block
   * @param constructor  Override constructor
   * @param base         Base block
   * @param <T>          Block type
   * @return  Registered block
   */
  public <T extends Block> T registerOverride(Function<Properties, T> constructor, Block base) {
    return register(constructor.apply(AbstractBlock.Properties.copy(base)), base);
  }

  /* Building */

  /**
   * Registers the given block as well as a slab and a stair variant for it.
   * Uses the vanilla slab and stair blocks. Uses the passed blocks properties for both.
   * Slabs and stairs are registered with a "_slab" and "_stairs" prefix
   *
   * @param block  The main block to register and whose properties to use
   * @param name   The registry name to use for the block and as base for the slab and stairs
   * @return  BuildingBlockObject for the given block
   */
  public BuildingBlockObject registerBuilding(Block block, String name) {
    return new BuildingBlockObject(
      this.register(block, name),
      this.register(new SlabBlock(AbstractBlock.Properties.copy(block)), name + "_slab"),
      this.register(new StairsBlock(block::defaultBlockState, AbstractBlock.Properties.copy(block)), name + "_stairs")
    );
  }

  /**
   * Same as {@link #registerBuilding(Block, String)}, but also includes a wall variant
   *
   * @param block  The main block to register and whose properties to use
   * @param name   The registry name to use for the block and as base for the slab and stairs
   * @return  BuildingBlockObject for the given block
   */
  public WallBuildingBlockObject registerWallBuilding(Block block, String name) {
    return new WallBuildingBlockObject(
      registerBuilding(block, name),
      this.register(new WallBlock(AbstractBlock.Properties.copy(block)), name + "_wall")
    );
  }

  /**
   * Same as {@link #registerBuilding(Block, String)}, but also includes a fence variant
   *
   * @param block  The main block to register and whose properties to use
   * @param name   The registry name to use for the block and as base for the slab and stairs
   * @return  BuildingBlockObject for the given block
   */
  public FenceBuildingBlockObject registerFenceBuilding(Block block, String name) {
    return new FenceBuildingBlockObject(
      registerBuilding(block, name),
      this.register(new FenceBlock(AbstractBlock.Properties.copy(block)), name + "_fence")
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
    Item.Properties itemProps = new Item.Properties().tab(group);

    // planks
    AbstractBlock.Properties planksProps = AbstractBlock.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(2.0f, 3.0f).sound(plankSound);
    BuildingBlockObject planks = registerBuilding(new Block(planksProps), name + "_planks");
    FenceBlock fence = register(new FenceBlock(Properties.copy(planks.get())), name + "_fence");
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(AbstractBlock.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(2.0f).sound(plankSound));
    RotatedPillarBlock strippedLog = register(stripped.get(), "stripped_" + name + "_log");
    RotatedPillarBlock strippedWood = register(stripped.get(), "stripped_" + name + "_wood");
    RotatedPillarBlock log = register(new StrippableLogBlock(strippedLog.delegate, AbstractBlock.Properties.of(
      barkMaterial, state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? planksColor : barkColor)
        .harvestTool(ToolType.AXE).strength(2.0f).sound(barkSound)), name + "_log");
    RotatedPillarBlock wood = register(new StrippableLogBlock(strippedWood.delegate, AbstractBlock.Properties.of(barkMaterial, barkColor).harvestTool(ToolType.AXE).strength(2.0f).sound(barkSound)), name + "_wood");

    // doors
    DoorBlock door = register(new WoodenDoorBlock(AbstractBlock.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(3.0F).sound(plankSound).noOcclusion()), name + "_door");
    TrapDoorBlock trapdoor = register(new TrapDoorBlock(AbstractBlock.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Blocks::never)), name + "_trapdoor");
    FenceGateBlock fenceGate = register(new FenceGateBlock(planksProps), name + "_fence_gate");
    // redstone
    AbstractBlock.Properties redstoneProps = AbstractBlock.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).noCollission().strength(0.5F).sound(plankSound);
    PressurePlateBlock pressurePlate = register(new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), name + "_pressure_plate");
    WoodButtonBlock button = register(new WoodButtonBlock(redstoneProps), name + "_button");
    // signs
    StandingSignBlock standingSign = register(new StandingSignBlock(AbstractBlock.Properties.of(planksMaterial, planksColor).noCollission().strength(1.0F).sound(plankSound), woodType), name + "_sign");
    WallSignBlock wallSign = register(new WallSignBlock(AbstractBlock.Properties.of(planksMaterial, planksColor).noCollission().strength(1.0F).sound(plankSound).lootFrom(standingSign.delegate), woodType), name + "_wall_sign");
    // tell mantle to inject these into the TE
    RegistrationHelper.registerSignBlock(standingSign.delegate);
    RegistrationHelper.registerSignBlock(wallSign.delegate);
    // finally, return
    return new WoodBlockObject(getResource(name), woodType, planks, log, strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button, standingSign, wallSign);
  }

  /* Fluid */

  /**
   * Registers a fluid block from a fluid
   * @param fluid       Fluid supplier
   * @param material    Fluid material
   * @param lightLevel  Fluid light level
   * @param name        Fluid name, unfortunately no way to fetch from the fluid as it does not exist yet
   * @return  Fluid block instance
   */
  public FlowingFluidBlock registerFluidBlock(Supplier<? extends ForgeFlowingFluid> fluid, Material material, int lightLevel, String name) {
    return register(
        new FlowingFluidBlock(fluid, AbstractBlock.Properties.of(material)
                                                     .noCollission()
                                                     .strength(100.0F)
                                                     .noDrops()
                                                     .lightLevel((state) -> lightLevel)),
        name + "_fluid");
  }
}
