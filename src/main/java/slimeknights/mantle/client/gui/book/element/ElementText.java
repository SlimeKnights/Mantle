package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.TextDataRenderer;

@OnlyIn(Dist.CLIENT)
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
    String action = TextDataRenderer.drawText(this.x, this.y, this.width, this.height, this.text, mouseX, mouseY, fontRenderer, this.tooltip);

    if(this.doAction) {
      this.doAction = false;
      StringActionProcessor.process(action, this.parent);
    }
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if(this.tooltip.size() > 0){
      TextDataRenderer.drawTooltip(this.tooltip, mouseX, mouseY, fontRenderer);
      this.tooltip.clear();
    }
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if(mouseButton == 0) {
      this.doAction = true;
    }
  }
}
