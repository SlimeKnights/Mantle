package slimeknights.mantle.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;

import javax.annotation.Nonnull;

public class MultiServantLogic extends TileEntity implements IServantLogic {

  boolean hasMaster;

  BlockPos master;

  Block masterBlock;

  IBlockState state;

  public boolean canUpdate() {
    return false;
  }

  public boolean getHasMaster() {
    return this.hasMaster;
  }

  public boolean hasValidMaster() {
    if(!this.hasMaster) {
      return false;
    }

    if(this.world.getBlockState(this.master).getBlock() == this.masterBlock && this.world
                                                                                      .getBlockState(this.master) == this.state) {
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
    this.hasMaster = true;
    this.master = pos;
    this.state = this.world.getBlockState(this.master);
    this.masterBlock = this.state.getBlock();
  }

  public void removeMaster() {
    this.hasMaster = false;
    this.master = null;
    this.masterBlock = null;
    this.state = null;
  }

  @Override
  public boolean setPotentialMaster(IMasterLogic master, World w, BlockPos pos) {
    return !this.hasMaster;
  }

  @Deprecated
  public boolean verifyMaster(IMasterLogic logic, BlockPos pos) {
    return this.master.equals(pos) && this.world.getBlockState(pos) == this.state && this.world.getBlockState(pos)
                                                                                                     .getBlock() == this.masterBlock;
  }

  @Override
  public boolean verifyMaster(IMasterLogic logic, World world, BlockPos pos) {
    if(this.hasMaster) {
      return this.hasValidMaster();
    }
    else {
      this.overrideMaster(pos);
      return true;
    }
  }

  @Override
  public void invalidateMaster(IMasterLogic master, World w, BlockPos pos) {
    this.hasMaster = false;
    master = null;
  }

  @Override
  public void notifyMasterOfChange() {
    if(this.hasValidMaster()) {
      IMasterLogic logic = (IMasterLogic) this.world.getTileEntity(this.master);
      logic.notifyChange(this, this.pos);
    }
  }

  public void readCustomNBT(NBTTagCompound tags) {
    this.hasMaster = tags.getBoolean("hasMaster");
    if(this.hasMaster) {
      int xCenter = tags.getInteger("xCenter");
      int yCenter = tags.getInteger("yCenter");
      int zCenter = tags.getInteger("zCenter");
      this.master = new BlockPos(xCenter, yCenter, zCenter);
      this.masterBlock = GameData.getBlockRegistry().getObject(new ResourceLocation(tags.getString("MasterBlockName")));
      this.state = Block.getStateById(tags.getInteger("masterState"));
    }
  }

  public NBTTagCompound writeCustomNBT(NBTTagCompound tags) {
    tags.setBoolean("hasMaster", this.hasMaster);
    if(this.hasMaster) {
      tags.setInteger("xCenter", this.master.getX());
      tags.setInteger("yCenter", this.master.getY());
      tags.setInteger("zCenter", this.master.getZ());
      tags.setString("MasterBlockName", GameData.getBlockRegistry().getNameForObject(this.masterBlock).toString());
      tags.setInteger("masterState", Block.getStateId(this.state));
    }
    return tags;
  }

  @Override
  public void readFromNBT(NBTTagCompound tags) {
    super.readFromNBT(tags);
    this.readCustomNBT(tags);
  }

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound tags) {
    tags = super.writeToNBT(tags);
    return this.writeCustomNBT(tags);
  }

  /* Packets */
  @Nonnull
  @Override
  public NBTTagCompound getUpdateTag() {
    NBTTagCompound tag = new NBTTagCompound();
    this.writeCustomNBT(tag);
    return tag;
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
    this.readCustomNBT(packet.getNbtCompound());
    this.world.notifyLightSet(this.pos);
    IBlockState state = world.getBlockState(this.pos);
    this.world.notifyBlockUpdate(this.pos, state, state, 3);
  }

  @Nonnull
  @Override
  public World getWorld() {
    return this.world;
  }

  @Deprecated
  public boolean setMaster(BlockPos pos) {
    if(!this.hasMaster || this.world.getBlockState(this.master) != this.state || (this.world
                                                                                         .getBlockState(this.master)
                                                                                         .getBlock() != this.masterBlock)) {
      this.overrideMaster(pos);
      return true;
    }
    else {
      return false;
    }
  }

}
