package slimeknights.mantle.client.screen.book.element;

public abstract class SizedBookElement extends BookElement {

  public int width, height;

  public SizedBookElement(int x, int y, int width, int height) {
    super(x, y);

    this.width = width;
    this.height = height;
  }

  public boolean isHovered(double mouseX, double mouseY) {
    return mouseX > this.x && mouseY > this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
  }

  /**
   * Changes the scale of this element.
   * Used notably by {@link slimeknights.mantle.client.book.data.content.ContentPageIconList}
   */
  public void scale(float scale) {
    this.width = (int) (this.width * scale);
    this.height = (int) (this.height * scale);
  }
}
