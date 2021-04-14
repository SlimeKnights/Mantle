package slimeknights.mantle.client.book.data;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AppearanceData implements IDataItem {

  public int coverColor = 0x8B4631;
  public int arrowColor = 0xFFFFD3;
  public int arrowColorHover = 0xFF541C;
  public int hoverColor = 0x77EE541C;
  public int slotColor = 0xFF844C;
  public int lockedSectionColor = 0x000000;
  public int structureButtonColor = 0xe3E3BC;
  public int structureButtonColorHovered = 0x76D1E8;
  public int structureButtonColorToggled = 0x67C768;

  public float scale = 0.5F;

  public IFormattableTextComponent title = new StringTextComponent("");
  public IFormattableTextComponent subtitle = new StringTextComponent("");

  public boolean drawPageNumbers = true;
  public boolean drawSectionListText = false;

  public ResourceLocation bookFrontTexture = new ResourceLocation("mantle:textures/gui/book/bookfront.png");
  public ResourceLocation bookTexture = new ResourceLocation("mantle:textures/gui/book/book.png");

  @Override
  public void load() {

  }
}
