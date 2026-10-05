package slimeknights.mantle.registration.adapter;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

import javax.annotation.CheckForNull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides utility registration methods when registering blocks.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BlockRegistryAdapter extends EnumRegistryAdapter<Block> {

  /** @inheritDoc */
  public BlockRegistryAdapter(@CheckForNull Registry<Block> registry) {
    super(registry);
  }

  /** @inheritDoc */
  public BlockRegistryAdapter(@CheckForNull Registry<Block> registry, String modid) {
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
    return register(constructor.apply(BlockBehaviour.Properties.ofLegacyCopy(base)), base);
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
      this.register(new SlabBlock(BlockBehaviour.Properties.ofLegacyCopy(block)), name + "_slab"),
      this.register(new StairBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofLegacyCopy(block)), name + "_stairs")
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
      this.register(new WallBlock(BlockBehaviour.Properties.ofLegacyCopy(block)), name + "_wall")
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
      this.register(new FenceBlock(BlockBehaviour.Properties.ofLegacyCopy(block)), name + "_fence")
    );
  }


  /**
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param behaviorCreator  Logic to create the behavior
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Function<WoodVariant,BlockBehaviour.Properties> behaviorCreator) {
    BlockSetType setType = new BlockSetType(resourceName(name));
    WoodType woodType = new WoodType(resourceName(name), setType);
    BlockSetType.register(setType);
    WoodType.register(woodType);
    RegistrationHelper.registerWoodType(woodType);

    // planks
    BlockBehaviour.Properties planksProps = behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f);
    BuildingBlockObject planks = registerBuilding(new Block(planksProps), name + "_planks");
    FenceBlock fence = register(new FenceBlock(Properties.ofLegacyCopy(planks.get()).forceSolidOn()), name + "_fence");
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f));
    RotatedPillarBlock strippedLog = register(stripped.get(), "stripped_" + name + "_log");
    RotatedPillarBlock strippedWood = register(stripped.get(), "stripped_" + name + "_wood");
    RotatedPillarBlock log = register(new StrippableLogBlock(() -> strippedLog, behaviorCreator.apply(WoodVariant.LOG).instrument(NoteBlockInstrument.BASS).strength(2.0f)), name + "_log");
    RotatedPillarBlock wood = register(new StrippableLogBlock(() -> strippedWood, behaviorCreator.apply(WoodVariant.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f)), name + "_wood");

    // doors
    DoorBlock door = register(new DoorBlock(setType, behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)), name + "_door");
    TrapDoorBlock trapdoor = register(new TrapDoorBlock(setType, behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)), name + "_trapdoor");
    FenceGateBlock fenceGate = register(new FenceGateBlock(woodType, Properties.ofLegacyCopy(fence)), name + "_fence_gate");
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().pushReaction(PushReaction.DESTROY).strength(0.5F);
    PressurePlateBlock pressurePlate = register(new PressurePlateBlock(setType, redstoneProps), name + "_pressure_plate");
    ButtonBlock button = register(new ButtonBlock(setType, 30, redstoneProps), name + "_button");
    // signs
    StandingSignBlock standingSign = register(new StandingSignBlock(woodType, behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)), name + "_sign");
    WallSignBlock wallSign = register(new WallSignBlock(woodType, behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(standingSign)), name + "_wall_sign");
    CeilingHangingSignBlock hangingSign = register(new CeilingHangingSignBlock(woodType, behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F)), name + "_hanging_sign");
    WallHangingSignBlock wallHangingSign = register(new WallHangingSignBlock(woodType, behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(hangingSign)), name + "_wall_hanging_sign");
    // tell mantle to inject these into the TE
    RegistrationHelper.registerSignBlock(() -> standingSign);
    RegistrationHelper.registerSignBlock(() -> wallSign);
    RegistrationHelper.registerHangingSignBlock(() -> hangingSign);
    RegistrationHelper.registerHangingSignBlock(() -> wallHangingSign);
    // finally, return
    return new WoodBlockObject(getResource(name), woodType, planks, log, strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button, standingSign, wallSign, hangingSign, wallHangingSign);
  }

  /* Fluid */

  /**
   * Registers a fluid block from a fluid
   * @param fluid       Fluid supplier
   * @param color       Fluid color
   * @param lightLevel  Fluid light level
   * @param name        Fluid name, unfortunately no way to fetch from the fluid as it does not exist yet
   * @return  Fluid block instance
   */
  public LiquidBlock registerFluidBlock(BaseFlowingFluid fluid, MapColor color, int lightLevel, String name) {
    return register(new LiquidBlock(fluid, FluidDeferredRegister.createProperties(color, lightLevel)), name + "_fluid");
  }
}
