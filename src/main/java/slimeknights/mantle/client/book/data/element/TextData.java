package slimeknights.mantle.client.book.data.element;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class TextData {

  public static final TextData LINEBREAK = new TextData(new StringTextComponent("\n"));
  public ITextComponent text;

  public boolean isParagraph = false;
  public boolean dropShadow = false;
  public float scale = 1.F;
  public String action = "";
  public ArrayList<ITextComponent> tooltips = null;

  public TextData() {
  }

  public TextData(ITextComponent text) {
    this.text = text;
  }
}
