package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.TextDataRenderer;

@SideOnly(Side.CLIENT)
public class ElementText extends SizedBookElement {
  public TextData[] text;
  private List<String> tooltip = new ArrayList<String>();

  private boolean doAction = false;

  public ElementText(int x, int y, int width, int height, String text) {
    this(x, y, width, height, new TextData(text));
  }

  public ElementText(int x, int y, int width, int height, Collection<TextData> text) {
    this(x, y, width, height, text.toArray(new TextData[text.size()]));
  }

  public ElementText(int x, int y, int width, int height, TextData... text) {
    super(x, y, width, height);

    this.text = text;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    String action = TextDataRenderer.drawText(x, y, width, height, text, mouseX, mouseY, fontRenderer, tooltip);

    if(doAction) {
      doAction = false;
      StringActionProcessor.process(action, parent);
    }
  }

  @Override
  public void drawTooltips(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if(tooltip.size() > 0){
      TextDataRenderer.drawTooltip(tooltip, mouseX, mouseY, fontRenderer);
      tooltip.clear();
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if(mouseButton == 0) {
      doAction = true;
    }
  }
}
