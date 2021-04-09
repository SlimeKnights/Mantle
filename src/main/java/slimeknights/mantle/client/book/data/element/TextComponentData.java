package slimeknights.mantle.client.book.data.element;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextComponentData {

  public static final TextComponentData LINEBREAK = new TextComponentData("\n");

  public ITextComponent text;

  public boolean isParagraph = false;
  public boolean dropShadow = false;
  public float scale = 1.F;
  public String action = "";
  public ITextComponent[] tooltips = null;

  public TextComponentData(String text) {
    this(new StringTextComponent(text));
  }

  public TextComponentData(ITextComponent text) {
    this.text = text;
  }


}
