package slimeknights.mantle.client.screen.widget;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import slimeknights.mantle.client.screen.element.ScreenElement;

/** Creates a vertical slider widget */
@SuppressWarnings({"UnusedReturnValue", "unused"})  // API
public class SliderWidget extends Widget {

  // gui info
  public final ScreenElement slider;
  public final ScreenElement sliderHighlighted;
  public final ScreenElement sliderDisabled;
  public final ScreenElement slideBarTop;
  public final ScreenElement slideBarBottom;
  public final ScreenElement slideBar;

  // slider info
  protected int minValue;
  protected int maxValue;
  protected int increment;

  // positioning info
  protected int currentValue;
  public int sliderOffset; // x-offset of the slider to the left edge of the slideBar
  @Getter @Setter
  protected boolean enabled;
  @Getter @Setter
  protected boolean hidden;

  protected boolean isScrolling;
  protected boolean isHighlighted;

  // where the slider was clicked on the slider itself (not on the bar, on the thing that slides)
  private int clickX;
  private int clickY;
  private boolean clickedBar; // if the bar has already been clicked and not released
  private boolean leftMouseDown = false;

  public SliderWidget(ScreenElement slider, ScreenElement sliderHighlighted, ScreenElement sliderDisabled, ScreenElement slideBarTop, ScreenElement slideBarBottom, ScreenElement slideBar) {
    this.slider = slider;
    this.sliderHighlighted = sliderHighlighted;
    this.sliderDisabled = sliderDisabled;
    this.slideBar = slideBar;
    this.slideBarTop = slideBarTop;
    this.slideBarBottom = slideBarBottom;

    this.height = slideBar.height();
    this.width = slideBar.width();
    this.currentValue = this.minValue = 0;
    this.maxValue = slideBar.height();
    this.increment = 1;

    this.sliderOffset = Mth.abs(slideBar.width() - slider.width()) / 2;

    this.isScrolling = false;
    this.isHighlighted = false;

    this.enabled = true;
    this.hidden = false;
  }

  /**
   * Sets the height of the whole slider and slidebar
   */
  public void setSize(int height) {
    this.height = height;
  }

  /**
   * specifies the values that the slider represents
   */
  public void setSliderParameters(int min, int max, int stepsize) {
    this.minValue = min;
    this.maxValue = max;
    this.increment = stepsize;

    // just in case
    this.setSliderValue(this.currentValue);
  }

  public int getValue() {
    if (this.isHidden()) {
      return 0;
    }
    return Math.min(this.maxValue, Math.max(this.minValue, this.currentValue));
  }

  public void enable() {
    this.enabled = true;
  }

  public void disable() {
    this.enabled = false;
  }

  public void hide() {
    this.hidden = true;
  }

  public void show() {
    this.hidden = false;
  }

  @Override
  public void draw(GuiGraphics graphics) {
    if (this.hidden) {
      return;
    }

    // slide bar background
    this.slideBarTop.draw(graphics, this.xPos, this.yPos);
    this.slideBar.drawScaledDown(graphics, this.xPos, this.yPos + this.slideBarTop.height(), 0, this.getUsableSlidebarHeight());
    this.slideBarBottom.draw(graphics, this.xPos, this.yPos + this.height - this.slideBarBottom.height());

    int x = this.xPos + this.sliderOffset;
    int y = this.yPos + this.getSliderTop();

    // the slider depending on state
    if (this.enabled) {
      if (this.isScrolling) {
        this.sliderDisabled.draw(graphics, x, y);
      } else if (this.isHighlighted) {
        this.sliderHighlighted.draw(graphics, x, y);
      } else {
        this.slider.draw(graphics, x, y);
      }
    } else {
      this.sliderDisabled.draw(graphics, x, y);
    }
  }

  public void update(int mouseX, int mouseY) {
    if (!this.enabled || this.hidden) {
      return;
    }

    // relative position inside the widget
    int x = mouseX - this.xPos;
    int y = mouseY - this.yPos;

    // reset click data
    if (!this.leftMouseDown && this.clickedBar) {
      this.clickedBar = false;
    }

    // button not pressed and scrolling -> stop scrolling
    if (!this.leftMouseDown && this.isScrolling) {
      this.isScrolling = false;
    }
    // button pressed and scrolling -> update position of slider
    else if (this.isScrolling) {
      float d = this.maxValue - this.minValue;
      float val = (float) (y - this.clickY) / (float) (this.getUsableSlidebarHeight() - this.slider.height());
      val *= d;

      if (val < (float) this.increment / 2f) {
        // < 1/2 increment
        this.setSliderValue(this.minValue);
      } else if (val > this.maxValue - ((float) this.increment / 2f)) {
        // > max-1/2 increment
        this.setSliderValue(this.maxValue);
      } else {
        // in between
        this.setSliderValue((int) (this.minValue + (float) this.increment * Math.round(val)));
      }
    }
    // not scrolling yet but possibly inside the slider
    else if (x >= 0 && y >= this.getSliderTop() &&
      x - this.sliderOffset <= this.slider.width() && y <= this.getSliderTop() + this.slider.height()) {
      this.isHighlighted = true;
      if (this.leftMouseDown) {
        this.isScrolling = true;
        this.clickX = x - this.sliderOffset;
        this.clickY = y - this.getSliderTop();
      }
    }
    // not on the slider but clicked on the bar
    else if (this.leftMouseDown && !this.clickedBar &&
      x >= 0 && y >= 0 &&
      x <= this.slideBar.width() && y <= this.height) {
      if (y < this.getSliderTop()) {
        this.decrement();
      } else {
        this.increment();
      }

      this.clickedBar = true;
    } else {
      this.isHighlighted = false;
    }
  }

  @Override
  public void handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (mouseButton == 0) {
      this.leftMouseDown = true;
    }
  }

  @Override
  public void handleMouseReleased() {
    this.leftMouseDown = false;
  }

  // Call this via Screen.mouseScrolled on your screen if you wish to use scroll data.
  public boolean mouseScrolled(double scrollData, boolean useMouseWheel) {
    if (useMouseWheel) {
      if (scrollData > 0.0) {
        this.decrement();
        return true;
      } else if (scrollData < 0.0) {
        this.increment();
        return true;
      }
    }

    return true;
  }

  public int increment() {
    this.setSliderValue(this.currentValue + this.increment);
    return this.currentValue;
  }

  public int decrement() {
    this.setSliderValue(this.currentValue - this.increment);
    return this.currentValue;
  }

  public int setSliderValue(int val) {
    if (val > this.maxValue) {
      val = this.maxValue;
    } else if (val < this.minValue) {
      val = this.minValue;
    }

    this.currentValue = val;
    return this.currentValue;
  }

  private int getSliderTop() {
    float d = this.maxValue - this.minValue;
    d = (float) (this.currentValue - this.minValue) / d;
    d *= this.getUsableSlidebarHeight() - this.slider.height();

    return (int) d + this.slideBarTop.height();
  }

  private int getUsableSlidebarHeight() {
    return this.height - this.slideBarTop.height() - this.slideBarBottom.height();
  }
}
