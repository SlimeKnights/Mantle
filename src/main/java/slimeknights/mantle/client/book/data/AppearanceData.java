package slimeknights.mantle.client.book.data;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.screen.book.Textures;
import slimeknights.mantle.util.LogicHelper;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public class AppearanceData implements IDataItem {

  public int coverColor = 0x8B4631;
  public int arrowColor = 0xFFFFD3;
  public int arrowColorHover = 0xFF541C;
  public int hoverColor = 0x77EE541C;
  public int slotColor = 0xFF844C;
  public int lockedSectionColor = 0x000000;
  public float scale = 0.5F;
  public String title = "";
  public String subtitle = "";
  public boolean drawPageNumbers = true;
  public boolean drawSectionListText = false;
  public int structureButtonColor = 0xe3E3BC;
  public int structureButtonColorHovered = 0x76D1E8;
  public int structureButtonColorToggled = 0x67C768;
  @Nullable
  private ResourceLocation coverTexture;
  @Nullable
  private ResourceLocation bookTexture;

  /** Gets the book cover texture */
  public ResourceLocation getCoverTexture() {
    return LogicHelper.defaultIfNull(coverTexture, Textures.TEX_BOOKFRONT);
  }

  /** Gets texture for book pages and elements */
  public ResourceLocation getBookTexture() {
    return LogicHelper.defaultIfNull(bookTexture, Textures.TEX_BOOK);
  }

  @Override
  public void load() {

  }
}
