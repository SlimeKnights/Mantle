package slimeknights.mantle.registration.adapter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
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
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.core.Registry;
import slimeknights.mantle.block.MantleCeilingHangingSignBlock;
import slimeknights.mantle.block.MantleStandingSignBlock;
import slimeknights.mantle.block.MantleWallHangingSignBlock;
import slimeknights.mantle.block.MantleWallSignBlock;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.block.entity.MantleHangingSignBlockEntity;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

import java.util.function.Function;
import java.util.function.Supplier;

import static slimeknights.mantle.util.RegistryHelper.getHolder;

/**
 * Provides utility registration methods when registering blocks.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BlockRegistryAdapter extends EnumRegistryAdapter<Block> {

  /** @inheritDoc */
  public BlockRegistryAdapter(Registry<Block> registry) {
    super(registry);
  }

  /** @inheritDoc */
  public BlockRegistryAdapter(Registry<Block> registry, String modid) {
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
    return register(constructor.apply(BlockBehaviour.Properties.ofFullCopy(base)), base);
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
      this.register(new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block)), name + "_slab"),
      this.register(new StairBlock(block.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block)), name + "_stairs")
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
      this.register(new WallBlock(BlockBehaviour.Properties.ofFullCopy(block)), name + "_wall")
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
      this.register(new FenceBlock(BlockBehaviour.Properties.ofFullCopy(block)), name + "_fence")
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
    FenceBlock fence = register(new FenceBlock(Properties.ofFullCopy(planks.get()).forceSolidOn()), name + "_fence");
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f));
    RotatedPillarBlock strippedLog = register(stripped.get(), "stripped_" + name + "_log");
    RotatedPillarBlock strippedWood = register(stripped.get(), "stripped_" + name + "_wood");
    RotatedPillarBlock log = register(new StrippableLogBlock(getHolder(BuiltInRegistries.BLOCK, strippedLog), behaviorCreator.apply(WoodVariant.LOG).instrument(NoteBlockInstrument.BASS).strength(2.0f)), name + "_log");
    RotatedPillarBlock wood = register(new StrippableLogBlock(getHolder(BuiltInRegistries.BLOCK, strippedWood), behaviorCreator.apply(WoodVariant.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f)), name + "_wood");

    // doors
    DoorBlock door = register(new DoorBlock(setType, behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)), name + "_door");
    TrapDoorBlock trapdoor = register(new TrapDoorBlock(setType, behaviorCreator.apply(WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)), name + "_trapdoor");
    FenceGateBlock fenceGate = register(new FenceGateBlock(woodType, Properties.ofFullCopy(fence)), name + "_fence_gate");
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().pushReaction(PushReaction.DESTROY).strength(0.5F);
    PressurePlateBlock pressurePlate = register(new PressurePlateBlock(setType, redstoneProps), name + "_pressure_plate");
    ButtonBlock button = register(new ButtonBlock(setType, 30, redstoneProps), name + "_button");
    // signs
    StandingSignBlock standingSign = register(new MantleStandingSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F), woodType), name + "_sign");
    WallSignBlock wallSign = register(new MantleWallSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(standingSign), woodType), name + "_wall_sign");
    MantleCeilingHangingSignBlock hangingSign = register(new MantleCeilingHangingSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F), woodType), name + "_hanging_sign");
    MantleWallHangingSignBlock wallHangingSign = register(new MantleWallHangingSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).dropsLike(hangingSign), woodType), name + "_wall_hanging_sign");
    // tell mantle to inject these into the TE
    MantleSignBlockEntity.registerSignBlock(() -> standingSign);
    MantleSignBlockEntity.registerSignBlock(() -> wallSign);
    MantleHangingSignBlockEntity.registerSignBlock(() -> hangingSign);
    MantleHangingSignBlockEntity.registerSignBlock(() -> wallHangingSign);
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
  public LiquidBlock registerFluidBlock(Supplier<? extends BaseFlowingFluid> fluid, MapColor color, int lightLevel, String name) {
    return register(new LiquidBlock(fluid.get(), FluidDeferredRegister.createProperties(color, lightLevel)), name + "_fluid");
  }
}
