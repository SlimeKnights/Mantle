package slimeknights.mantle.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import slimeknights.mantle.tileentity.MantleTileEntity;

import java.util.Objects;

/**
 * @deprecated  Slated for removal in 1.17. If you used this, talk to one of the devs and we can pull the updated verson from Tinkers Construct back
 */
@Deprecated
public class MultiServantLogic extends MantleTileEntity implements IServantLogic {

  boolean hasMaster;
  BlockPos master;
  Block masterBlock;
  BlockState state;

  public MultiServantLogic(BlockEntityType<?> tileEntityTypeIn) {
    super(tileEntityTypeIn);
  }

  public boolean canUpdate() {
    return false;
  }

  public boolean getHasMaster() {
    return this.hasMaster;
  }

  public boolean hasValidMaster() {
    if (!this.hasMaster) {
      return false;
    }

    assert this.world != null;
    if (this.world.getBlockState(this.master).getBlock() == this.masterBlock && this.world.getBlockState(this.master) == this.state) {
      return true;
    }
    else {
      this.hasMaster = false;
      this.master = null;
      return false;
    }
  }

  @Override
  public BlockPos getMasterPosition() {
    return this.master;
  }

  public void overrideMaster(BlockPos pos) {
    assert this.world != null;
    this.hasMaster = true;
    this.master = pos;
    this.state = this.world.getBlockState(this.master);
    this.masterBlock = this.state.getBlock();
    this.markDirtyFast();
  }

  public void removeMaster() {
    this.hasMaster = false;
    this.master = null;
    this.masterBlock = null;
    this.state = null;
    this.markDirtyFast();
  }

  @Override
  public boolean setPotentialMaster(IMasterLogic master, World w, BlockPos pos) {
    return !this.hasMaster;
  }

  @Deprecated
  public boolean verifyMaster(IMasterLogic logic, BlockPos pos) {
    assert this.world != null;
    return this.master.equals(pos) && this.world.getBlockState(pos) == this.state
           && this.world.getBlockState(pos).getBlock() == this.masterBlock;
  }

  @Override
  public boolean verifyMaster(IMasterLogic logic, World world, BlockPos pos) {
    if (this.hasMaster) {
      return this.hasValidMaster();
    }
    else {
      this.overrideMaster(pos);
      return true;
    }
  }

  @Override
  public void invalidateMaster(IMasterLogic master, World w, BlockPos pos) {
    this.removeMaster();
  }

  @Override
  public void notifyMasterOfChange() {
    if (this.hasValidMaster()) {
      assert this.world != null;
      IMasterLogic logic = (IMasterLogic) this.world.getBlockEntity(this.master);
      logic.notifyChange(this, this.pos);
    }
  }

  public void readCustomNBT(CompoundTag tags) {
    this.hasMaster = tags.getBoolean("hasMaster");
    if (this.hasMaster) {
      int xCenter = tags.getInt("xCenter");
      int yCenter = tags.getInt("yCenter");
      int zCenter = tags.getInt("zCenter");
      this.master = new BlockPos(xCenter, yCenter, zCenter);
      this.masterBlock = Registry.BLOCK.get(new Identifier(tags.getString("MasterBlockName")));
      this.state = Block.getStateFromRawId(tags.getInt("masterState"));
    }
  }

  public CompoundTag writeCustomNBT(CompoundTag tags) {
    tags.putBoolean("hasMaster", this.hasMaster);
    if (this.hasMaster) {
      tags.putInt("xCenter", this.master.getX());
      tags.putInt("yCenter", this.master.getY());
      tags.putInt("zCenter", this.master.getZ());
      tags.putString("MasterBlockName", Objects.requireNonNull(Registry.BLOCK.getId(this.masterBlock)).toString());
      tags.putInt("masterState", Block.getRawIdFromState(this.state));
    }
    return tags;
  }

  @Override
  public void fromTag(BlockState blockState, CompoundTag tags) {
    super.fromTag(blockState, tags);
    this.readCustomNBT(tags);
  }

  @Override
  public CompoundTag toTag(CompoundTag tags) {
    tags = super.toTag(tags);
    return this.writeCustomNBT(tags);
  }

  /* Packets */
  @Override
  public CompoundTag toInitialChunkDataTag() {
    CompoundTag tag = new CompoundTag();
    this.writeCustomNBT(tag);
    return tag;
  }

//  @Override
//  public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket packet) {
//    this.readCustomNBT(packet.getCompoundTag());
//    //this.world.notifyLightSet(this.pos);
//    assert world != null;
//    BlockState state = world.getBlockState(this.pos);
//    this.world.updateListeners(this.pos, state, state, 3);
//  }

  @Deprecated
  public boolean setMaster(BlockPos pos) {
    assert this.world != null;
    if (!this.hasMaster || this.world.getBlockState(this.master) != this.state || (this.world.getBlockState(this.master).getBlock() != this.masterBlock)) {
      this.overrideMaster(pos);
      return true;
    }
    else {
      return false;
    }
  }

}
