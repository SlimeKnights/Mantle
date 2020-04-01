package slimeknights.mantle.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import slimeknights.mantle.tileentity.InventoryTileEntity;

/*
NEVER USED, PENDING REMOVAL?
 */
public class SimpleChestContainer extends BaseContainer<InventoryTileEntity> {

  public SimpleChestContainer(int windowId, PlayerInventory playerInventory, InventoryTileEntity tile, int rows, int columns) {
    super(null, windowId, playerInventory, tile);

    int index = 0;

    // chest inventory
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < columns; ++j) {
        // safety
        if (index > tile.getSizeInventory()) {
          break;
        }

        this.addSlot(this.createSlot(tile, index, 8 + j * 18, 18 + i * 18));
        index++;
      }
    }

    // player inventory
    this.addInventorySlots();
  }

  public SimpleChestContainer(int id, PlayerInventory inv, PacketBuffer buf) {
    this(id, inv, getTileEntityFromBuf(buf, InventoryTileEntity.class), 0, 0);
  }

  protected Slot createSlot(IInventory inventory, int index, int x, int y) {
    return new Slot(inventory, index, x, y);
  }

  protected int getInventoryXOffset() {
    return 17;
  }

  protected int getInventoryYOffset() {
    return 86;
  }
}
