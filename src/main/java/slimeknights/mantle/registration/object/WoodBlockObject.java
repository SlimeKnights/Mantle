package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.List;

/** Extension of the fence object with all other wood blocks */
@Getter
public class WoodBlockObject extends FenceBuildingBlockObject {
  private final WoodType woodType;
  // basic
  private final Holder<Block> logHolder;
  private final Holder<Block> strippedLogHolder;
  private final Holder<Block> woodHolder;
  private final Holder<Block> strippedWoodHolder;
  // doors
  private final Holder<Block> fenceGateHolder;
  private final Holder<Block> doorHolder;
  private final Holder<Block> trapdoorHolder;
  // redstone
  private final Holder<Block> pressurePlateHolder;
  private final Holder<Block> buttonHolder;
  // signs
  private final Holder<Block> signHolder;
  private final Holder<Block> wallSignHolder;
  private final Holder<Block> hangingSignHolder;
  private final Holder<Block> wallHangingSignHolder;
  // tags
  private final BlockItemTagKey logTag;

  public WoodBlockObject(ResourceLocation name, WoodType woodType, BuildingBlockObject planks,
                         Holder<Block> log, Holder<Block> strippedLog, Holder<Block> wood, Holder<Block> strippedWood,
                         Holder<Block> fence, Holder<Block> fenceGate, Holder<Block> door, Holder<Block> trapdoor,
                         Holder<Block> pressurePlate, Holder<Block> button,
                         Holder<Block> sign, Holder<Block> wallSign,
                         Holder<Block> hangingSign, Holder<Block> wallHangingSign) {
    super(planks, fence);
    this.woodType = woodType;
    this.logHolder = log;
    this.strippedLogHolder = strippedLog;
    this.woodHolder = wood;
    this.strippedWoodHolder = strippedWood;
    this.fenceGateHolder = fenceGate;
    this.doorHolder = door;
    this.trapdoorHolder = trapdoor;
    this.pressurePlateHolder = pressurePlate;
    this.buttonHolder = button;
    this.signHolder = sign;
    this.wallSignHolder = wallSign;
    this.hangingSignHolder = hangingSign;
    this.wallHangingSignHolder = wallHangingSign;
    this.logTag = BlockItemTagKey.create(name.withSuffix("_logs"));
  }

  /** Gets the log for this wood type */
  public Block getLog() {
    return logHolder.value();
  }

  /** Gets the stripped log for this wood type */
  public Block getStrippedLog() {
    return strippedLogHolder.value();
  }

  /** Gets the wood for this wood type */
  public Block getWood() {
    return woodHolder.value();
  }

  /** Gets the stripped wood for this wood type */
  public Block getStrippedWood() {
    return strippedWoodHolder.value();
  }

  /* Doors */

  /** Gets the fence gate for this wood type */
  public FenceGateBlock getFenceGate() {
    return (FenceGateBlock) fenceGateHolder.value();
  }

  /** Gets the door for this wood type */
  public DoorBlock getDoor() {
    return (DoorBlock) doorHolder.value();
  }

  /** Gets the trapdoor for this wood type */
  public TrapDoorBlock getTrapdoor() {
    return (TrapDoorBlock) trapdoorHolder.value();
  }

  /* Redstone */

  /** Gets the pressure plate for this wood type */
  public PressurePlateBlock getPressurePlate() {
    return (PressurePlateBlock) pressurePlateHolder.value();
  }

  /** Gets the button for this wood type */
  public ButtonBlock getButton() {
    return (ButtonBlock) buttonHolder.value();
  }

  /* Signs */

  /* Gets the sign for this wood type, can also be used to get the item */
  public StandingSignBlock getSign() {
    return (StandingSignBlock) signHolder.value();
  }

  /* Gets the wall sign for this wood type */
  public WallSignBlock getWallSign() {
    return (WallSignBlock) wallSignHolder.value();
  }

  /* Gets the hanging sign for this wood type */
  public CeilingHangingSignBlock getHangingSign() {
    return (CeilingHangingSignBlock) hangingSignHolder.value();
  }

  /* Gets the wall hanging sign for this wood type */
  public WallHangingSignBlock getWallHangingSign() {
    return (WallHangingSignBlock) wallHangingSignHolder.value();
  }

  @Override
  public List<Block> values() {
    return List.of(
      get(), getSlab(), getStairs(), getFence(),
      getLog(), getStrippedLog(), getWood(), getStrippedWood(),
      getFenceGate(), getDoor(), getTrapdoor(),
      getPressurePlate(), getButton(),
      getSign(), getWallSign(), getHangingSign(), getWallHangingSign());
  }

  @Override
  public List<Holder<Block>> holders() {
    return List.of(
      holder, slabHolder, stairsHolder, fenceHolder,
      logHolder, strippedLogHolder, woodHolder, strippedWoodHolder,
      fenceGateHolder, doorHolder, trapdoorHolder,
      pressurePlateHolder, buttonHolder,
      signHolder, wallSignHolder, hangingSignHolder, wallHangingSignHolder
    );
  }

  /** Variants of wood for the register function */
	public enum WoodVariant { LOG, WOOD, PLANKS }
}
