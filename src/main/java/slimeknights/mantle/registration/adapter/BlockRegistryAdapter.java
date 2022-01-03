package slimeknights.mantle.registration.adapter;

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
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.block.MantleStandingSignBlock;
import slimeknights.mantle.block.MantleWallSignBlock;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.block.WoodenDoorBlock;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

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
   * @param behaviorCreator  Logic to create the behavior
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Function<WoodVariant,BlockBehaviour.Properties> behaviorCreator) {
    WoodType woodType = WoodType.create(resourceName(name));
    RegistrationHelper.registerWoodType(woodType);

    // planks
    BlockBehaviour.Properties planksProps = behaviorCreator.apply(WoodVariant.PLANKS).strength(2.0f, 3.0f);
    BuildingBlockObject planks = registerBuilding(new Block(planksProps), name + "_planks");
    FenceBlock fence = register(new FenceBlock(Properties.copy(planks.get())), name + "_fence");
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(behaviorCreator.apply(WoodVariant.PLANKS).strength(2.0f));
    RotatedPillarBlock strippedLog = register(stripped.get(), "stripped_" + name + "_log");
    RotatedPillarBlock strippedWood = register(stripped.get(), "stripped_" + name + "_wood");
    RotatedPillarBlock log = register(new StrippableLogBlock(strippedLog.delegate, behaviorCreator.apply(WoodVariant.LOG).strength(2.0f)), name + "_log");
    RotatedPillarBlock wood = register(new StrippableLogBlock(strippedWood.delegate, behaviorCreator.apply(WoodVariant.WOOD).strength(2.0f)), name + "_wood");

    // doors
    DoorBlock door = register(new WoodenDoorBlock(behaviorCreator.apply(WoodVariant.PLANKS).strength(3.0F).noOcclusion()), name + "_door");
    TrapDoorBlock trapdoor = register(new TrapDoorBlock(behaviorCreator.apply(WoodVariant.PLANKS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)), name + "_trapdoor");
    FenceGateBlock fenceGate = register(new FenceGateBlock(planksProps), name + "_fence_gate");
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodVariant.PLANKS).noCollission().strength(0.5F);
    PressurePlateBlock pressurePlate = register(new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), name + "_pressure_plate");
    WoodButtonBlock button = register(new WoodButtonBlock(redstoneProps), name + "_button");
    // signs
    StandingSignBlock standingSign = register(new MantleStandingSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).noCollission().strength(1.0F), woodType), name + "_sign");
    WallSignBlock wallSign = register(new MantleWallSignBlock(behaviorCreator.apply(WoodVariant.PLANKS).noCollission().strength(1.0F).lootFrom(standingSign.delegate), woodType), name + "_wall_sign");
    // tell mantle to inject these into the TE
    MantleSignBlockEntity.registerSignBlock(standingSign.delegate);
    MantleSignBlockEntity.registerSignBlock(wallSign.delegate);
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
