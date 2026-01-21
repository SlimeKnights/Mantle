package slimeknights.mantle.client.screen.book.element;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;

/** Link elements for {@link slimeknights.mantle.client.book.data.content.ContentPageIconList} */
public class PageIconLinkElement extends SizedBookElement implements IHTML {

  public PageData pageData;
  public SizedBookElement displayElement;
  public TextData link;
  public String action;
  public Component name;

  public PageIconLinkElement(int x, int y, SizedBookElement displayElement, Component name, PageData pageData) {
    this(x, y, displayElement.width, displayElement.height, displayElement, name, pageData);
  }

  public PageIconLinkElement(int x, int y, int w, int h, SizedBookElement displayElement, Component name, PageData pageData) {
    super(x, y, w, h);
    this.displayElement = displayElement;
    this.pageData = pageData;

    this.action = "mantle:go-to-page-rtn " + pageData.parent.name + "." + pageData.name;

    this.name = name;
  }

  @Override
  public void setParent(BookScreen parent) {
    super.setParent(parent);
    displayElement.setParent(parent);
  }

  @Override
  public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    boolean hover = this.isHovered(mouseX, mouseY);

    if (hover) {
      graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, this.parent.book.appearance.hoverColor | (0x77 << 24));
    }

    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    this.displayElement.draw(graphics, mouseX, mouseY, partialTicks, fontRenderer);
  }

  @Override
  public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.name != null && !this.name.getString().isEmpty() && this.isHovered(mouseX, mouseY)) {
      this.drawTooltip(graphics, ImmutableList.of(name), mouseX, mouseY, fontRenderer);
    }
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.isHovered(mouseX, mouseY)) {
      StringActionProcessor.process(this.action, this.parent);
    }
  }

  @Override
  public String toHTML(BookData book) {
    // basically just ignores 'mantle:go-to-page-rtn '
    String location = action.substring(action.indexOf(StringActionProcessor.PROTOCOL_SEPARATOR) + StringActionProcessor.PROTOCOL_SEPARATOR.length());
    int bookPage = book.findPageNumber(location);

    return String.format("""
      <div data-minetip-title="%s">
      <a href="../page-%d/#%s"><img src="/assets/images/book/icons/blank.png" alt=""></a>
      </div>""",
      book.findPage(bookPage - 1, null).getTitle(),
      bookPage / 2,
      location
    );
  }
}
