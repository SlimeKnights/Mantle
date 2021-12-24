package slimeknights.mantle.client.screen.book.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import slimeknights.mantle.client.book.data.element.TextData;

/** Element within a {@link slimeknights.mantle.client.book.data.content.ContentListing} */
public class ListingLeftElement extends TextElement {
  private final boolean isClickable;
  private final int textStart;
  public ListingLeftElement(int x, int y, int width, int height, boolean subSection, TextData... text) {
    super(x, y, width, height, text);
    // determine if there should be hover text
    boolean isClickable = false;
    for (TextData data : text) {
      if (!data.action.isEmpty()) {
        isClickable = true;
        break;
      }
    }
    this.isClickable = isClickable;
    // subsections leave out the bullet
    this.textStart = subSection ? 0 : 1;
    if (!subSection) {
      this.text = Lists.asList(new TextData(), this.text).toArray(new TextData[this.text.length + 1]);
      this.text[0].color = "dark red";
      this.text[0].text = "- ";
    }
  }

  @Override
  public void draw(PoseStack matrices, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (isClickable) {
      if (this.isHovered(mouseX, mouseY)) {
        // update bullet
        if (textStart == 1) {
          this.text[0].text = "> ";
        }
        // update color and style
        for (int i = textStart; i < this.text.length; i++) {
          this.text[i].color = "dark red";
          this.text[i].underlined = true;
        }
      } else {
        // restore bullet
        if (textStart == 1) {
          this.text[0].text = "- ";
        }
        // restore color and style
        for (int i = textStart; i < this.text.length; i++) {
          this.text[i].color = "black";
          this.text[i].underlined = false;
        }
      }
    }

    super.draw(matrices, mouseX, mouseY, partialTicks, fontRenderer);
  }
}
