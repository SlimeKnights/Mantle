package slimeknights.mantle.registration.adapter;

import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
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
    return register(constructor.apply(BlockBehaviour.Properties.copy(base)), base);
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
      this.register(new SlabBlock(BlockBehaviour.Properties.copy(block)), name + "_slab"),
      this.register(new StairBlock(block::defaultBlockState, BlockBehaviour.Properties.copy(block)), name + "_stairs")
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
      this.register(new WallBlock(BlockBehaviour.Properties.copy(block)), name + "_wall")
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
      this.register(new FenceBlock(BlockBehaviour.Properties.copy(block)), name + "_fence")
    );
  }


  /**
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param planksMaterial   Material for the planks
   * @param planksColor      Map color for the planks
   * @param plankSound       Sound for the planks
   * @param barkMaterial     Bark material
   * @param barkColor        Map color for the bark
   * @param barkSound        Sound for the bark
   * @param group            Item group
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Material planksMaterial, MaterialColor planksColor, SoundType plankSound, Material barkMaterial, MaterialColor barkColor, SoundType barkSound, CreativeModeTab group) {
    WoodType woodType = WoodType.create(resourceName(name));
    RegistrationHelper.registerWoodType(woodType);
    Item.Properties itemProps = new Item.Properties().tab(group);

    // planks
    BlockBehaviour.Properties planksProps = BlockBehaviour.Properties.of(planksMaterial, planksColor).strength(2.0f, 3.0f).sound(plankSound);
    BuildingBlockObject planks = registerBuilding(new Block(planksProps), name + "_planks");
    FenceBlock fence = register(new FenceBlock(Properties.copy(planks.get())), name + "_fence");
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).strength(2.0f).sound(plankSound));
    RotatedPillarBlock strippedLog = register(stripped.get(), "stripped_" + name + "_log");
    RotatedPillarBlock strippedWood = register(stripped.get(), "stripped_" + name + "_wood");
    RotatedPillarBlock log = register(new StrippableLogBlock(strippedLog.delegate, BlockBehaviour.Properties.of(
      barkMaterial, state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? planksColor : barkColor)
        .strength(2.0f).sound(barkSound)), name + "_log");
    RotatedPillarBlock wood = register(new StrippableLogBlock(strippedWood.delegate, BlockBehaviour.Properties.of(barkMaterial, barkColor).strength(2.0f).sound(barkSound)), name + "_wood");

    // doors
    DoorBlock door = register(new WoodenDoorBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).strength(3.0F).sound(plankSound).noOcclusion()), name + "_door");
    TrapDoorBlock trapdoor = register(new TrapDoorBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Blocks::never)), name + "_trapdoor");
    FenceGateBlock fenceGate = register(new FenceGateBlock(planksProps), name + "_fence_gate");
    // redstone
    BlockBehaviour.Properties redstoneProps = BlockBehaviour.Properties.of(planksMaterial, planksColor).noCollission().strength(0.5F).sound(plankSound);
    PressurePlateBlock pressurePlate = register(new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), name + "_pressure_plate");
    WoodButtonBlock button = register(new WoodButtonBlock(redstoneProps), name + "_button");
    // signs
    StandingSignBlock standingSign = register(new StandingSignBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).noCollission().strength(1.0F).sound(plankSound), woodType), name + "_sign");
    WallSignBlock wallSign = register(new WallSignBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).noCollission().strength(1.0F).sound(plankSound).lootFrom(standingSign.delegate), woodType), name + "_wall_sign");
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
  public LiquidBlock registerFluidBlock(Supplier<? extends ForgeFlowingFluid> fluid, Material material, int lightLevel, String name) {
    return register(
        new LiquidBlock(fluid, BlockBehaviour.Properties.of(material)
                                                     .noCollission()
                                                     .strength(100.0F)
                                                     .noDrops()
                                                     .lightLevel((state) -> lightLevel)),
        name + "_fluid");
  }
}
