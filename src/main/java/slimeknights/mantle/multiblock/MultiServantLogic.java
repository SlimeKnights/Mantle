package slimeknights.mantle.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
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

  public MultiServantLogic(TileEntityType<?> tileEntityTypeIn) {
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

    assert this.level != null;
    if (this.level.getBlockState(this.master).getBlock() == this.masterBlock && this.level.getBlockState(this.master) == this.state) {
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
    assert this.level != null;
    this.hasMaster = true;
    this.master = pos;
    this.state = this.level.getBlockState(this.master);
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
    assert this.level != null;
    return this.master.equals(pos) && this.level.getBlockState(pos) == this.state
           && this.level.getBlockState(pos).getBlock() == this.masterBlock;
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
      assert this.level != null;
      IMasterLogic logic = (IMasterLogic) this.level.getBlockEntity(this.master);
      logic.notifyChange(this, this.worldPosition);
    }
  }

  public void readCustomNBT(CompoundNBT tags) {
    this.hasMaster = tags.getBoolean("hasMaster");
    if (this.hasMaster) {
      int xCenter = tags.getInt("xCenter");
      int yCenter = tags.getInt("yCenter");
      int zCenter = tags.getInt("zCenter");
      this.master = new BlockPos(xCenter, yCenter, zCenter);
      this.masterBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tags.getString("MasterBlockName")));
      this.state = Block.stateById(tags.getInt("masterState"));
    }
  }

  public CompoundNBT writeCustomNBT(CompoundNBT tags) {
    tags.putBoolean("hasMaster", this.hasMaster);
    if (this.hasMaster) {
      tags.putInt("xCenter", this.master.getX());
      tags.putInt("yCenter", this.master.getY());
      tags.putInt("zCenter", this.master.getZ());
      tags.putString("MasterBlockName", Objects.requireNonNull(this.masterBlock.getRegistryName()).toString());
      tags.putInt("masterState", Block.getId(this.state));
    }
    return tags;
  }

  @Override
  public void load(BlockState blockState, CompoundNBT tags) {
    super.load(blockState, tags);
    this.readCustomNBT(tags);
  }

  @Override
  public CompoundNBT save(CompoundNBT tags) {
    tags = super.save(tags);
    return this.writeCustomNBT(tags);
  }

  /* Packets */
  @Override
  public CompoundNBT getUpdateTag() {
    CompoundNBT tag = new CompoundNBT();
    this.writeCustomNBT(tag);
    return tag;
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
    this.readCustomNBT(packet.getTag());
    //this.world.notifyLightSet(this.pos);
    assert level != null;
    BlockState state = level.getBlockState(this.worldPosition);
    this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
  }

  @Deprecated
  public boolean setMaster(BlockPos pos) {
    assert this.level != null;
    if (!this.hasMaster || this.level.getBlockState(this.master) != this.state || (this.level.getBlockState(this.master).getBlock() != this.masterBlock)) {
      this.overrideMaster(pos);
      return true;
    }
    else {
      return false;
    }
  }

}
