package slimeknights.mantle.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

import slimeknights.mantle.tileentity.TileInventory;

public class ContainerSimpleChest extends BaseContainer<TileInventory> {

  public ContainerSimpleChest(ContainerType<?> containerType, int windowId, TileInventory tile, int rows, int columns, PlayerInventory playerInventory) {
    super(containerType, windowId, tile);

    int index = 0;

    // chest inventory
    for(int i = 0; i < rows; ++i) {
      for(int j = 0; j < columns; ++j) {
        // safety
        if(index > tile.getSizeInventory()) {
          break;
        }

        this.addSlot(this.createSlot(tile, index, 8 + j * 18, 18 + i * 18));
        index++;
      }
    }

    // player inventory
    this.addPlayerInventory(playerInventory, 17, 86);
  }

  protected Slot createSlot(IInventory inventory, int index, int x, int y) {
    return new Slot(inventory, index, x, y);
  }
}
