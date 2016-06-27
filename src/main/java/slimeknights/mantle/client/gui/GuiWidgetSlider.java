package slimeknights.mantle.client.gui;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

// a vertical slider!
@SideOnly(Side.CLIENT)
public class GuiWidgetSlider extends GuiWidget {

  // gui info
  public final GuiElement slider;
  public final GuiElement sliderHighlighted;
  public final GuiElement sliderDisabled;
  public final GuiElement slideBarTop;
  public final GuiElement slideBarBottom;
  public final GuiElementScalable slideBar;

  // slider info
  protected int minValue;
  protected int maxValue;
  protected int increment;

  // positioning info
  protected int currentValue;
  public int sliderOffset; // x-offset of the slider to the left edge of the slideBar
  protected boolean enabled;
  protected boolean hidden;

  protected boolean isScrolling;
  protected boolean isHighlighted;
  // where the slider was clicked on the slider itself (not on the bar, on the thing that slides)
  private int clickX;
  private int clickY;
  private boolean clickedBar; // if the bar has already been clicked and not released

  public GuiWidgetSlider(GuiElement slider, GuiElement sliderHighlighted, GuiElement sliderDisabled, GuiElement slideBarTop, GuiElement slideBarBottom, GuiElementScalable slideBar) {
    this.slider = slider;
    this.sliderHighlighted = sliderHighlighted;
    this.sliderDisabled = sliderDisabled;
    this.slideBar = slideBar;
    this.slideBarTop = slideBarTop;
    this.slideBarBottom = slideBarBottom;

    height = slideBar.h;
    width = slideBar.w;
    currentValue = minValue = 0;
    maxValue = slideBar.h;
    increment = 1;

    sliderOffset = MathHelper.abs_int(slideBar.w - slider.w) / 2;

    isScrolling = false;
    isHighlighted = false;

    enabled = true;
    hidden = false;
  }

  /** Sets the height of the whole slider and slidebar */
  public void setSize(int height) {
    this.height = height;
  }

  /** specifies the values that the slider represents */
  public void setSliderParameters(int min, int max, int stepsize) {
    this.minValue = min;
    this.maxValue = max;
    this.increment = stepsize;

    // just in case
    setSliderValue(currentValue);
  }

  public int getValue() {
    if(isHidden()) {
      return 0;
    }
    return Math.min(maxValue, Math.max(minValue, currentValue));
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void enable() {
    this.enabled = true;
  }

  public void disable() {
    this.enabled = false;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public void hide() {
    this.hidden = true;
  }

  public void show() {
    this.hidden = false;
  }

  public boolean isHidden() {
    return this.hidden;
  }

  @Override
  public void draw() {
    if(hidden) {
      return;
    }

    // slidebar background
    slideBarTop.draw(xPos, yPos);
    slideBar.drawScaledY(xPos, yPos + slideBarTop.h, getUsableSlidebarHeight());
    slideBarBottom.draw(xPos, yPos + height - slideBarBottom.h);

    int x = xPos + sliderOffset;
    int y = yPos + getSliderTop();

    // the slider depending on state
    if(enabled) {
      if(isScrolling) {
        sliderDisabled.draw(x, y);
      }
      else if(isHighlighted) {
        sliderHighlighted.draw(x, y);
      }
      else {
        slider.draw(x, y);
      }
    }
    else {
      sliderDisabled.draw(x, y);
    }
  }

  public void update(int mouseX, int mouseY, boolean useMouseWheel) {
    if(!enabled || hidden) {
      return;
    }

    boolean mouseDown = Mouse.isButtonDown(0); // left mouse button
    int wheel = Mouse.getDWheel();

    if(useMouseWheel) {
      if(wheel > 0) {
        decrement();
        return;
      }
      else if(wheel < 0) {
        increment();
        return;
      }
    }

    // relative position inside the widget
    int x = mouseX - xPos;
    int y = mouseY - yPos;

    // reset click data
    if(!mouseDown && clickedBar) {
      clickedBar = false;
    }

    // button not pressed and scrolling -> stop scrolling
    if(!mouseDown && isScrolling) {
      isScrolling = false;
    }
    // button pressed and scrolling -> update position of slider
    else if(isScrolling) {
      float d = maxValue - minValue;
      float val = (float) (y - clickY) / (float) (getUsableSlidebarHeight() - slider.h);
      val *= d;

      if(val < (float) increment / 2f) {
        // < 1/2 increment
        setSliderValue(minValue);
      }
      else if(val > maxValue - ((float) increment / 2f)) {
        // > max-1/2 increment
        setSliderValue(maxValue);
      }
      else {
        // in between
        setSliderValue((int) (minValue + (float) increment * Math.round(val)));
      }
    }
    // not scrolling yet but possibly inside the slider
    else if(x >= 0 && y >= getSliderTop() &&
            x - sliderOffset <= slider.w && y <= getSliderTop() + slider.h) {
      isHighlighted = true;
      if(mouseDown) {
        isScrolling = true;
        clickX = x - sliderOffset;
        clickY = y - getSliderTop();
      }
    }
    // not on the slider but clicked on the bar
    else if(mouseDown && !clickedBar &&
            x >= 0 && y >= 0 &&
            x <= slideBar.w && y <= height) {
      if(y < getSliderTop()) {
        decrement();
      }
      else {
        increment();
      }

      clickedBar = true;
    }
    else {
      isHighlighted = false;
    }
  }

  public int increment() {
    setSliderValue(currentValue + increment);
    return currentValue;
  }

  public int decrement() {
    setSliderValue(currentValue - increment);
    return currentValue;
  }

  public int setSliderValue(int val) {
    if(val > maxValue) {
      val = maxValue;
    }
    else if(val < minValue) {
      val = minValue;
    }

    currentValue = val;
    return currentValue;
  }

  private int getSliderTop() {
    float d = maxValue - minValue;
    d = (float) (currentValue - minValue) / d;
    d *= getUsableSlidebarHeight() - slider.h;

    return (int) d + slideBarTop.h;
  }

  private int getUsableSlidebarHeight() {
    return height - slideBarTop.h - slideBarBottom.h;
  }
}
