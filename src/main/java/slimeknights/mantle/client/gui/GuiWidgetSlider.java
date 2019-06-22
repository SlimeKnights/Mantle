package slimeknights.mantle.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// a vertical slider!
@OnlyIn(Dist.CLIENT)
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
  private boolean useMouseWheel;

  public GuiWidgetSlider(GuiElement slider, GuiElement sliderHighlighted, GuiElement sliderDisabled, GuiElement slideBarTop, GuiElement slideBarBottom, GuiElementScalable slideBar) {
    this.slider = slider;
    this.sliderHighlighted = sliderHighlighted;
    this.sliderDisabled = sliderDisabled;
    this.slideBar = slideBar;
    this.slideBarTop = slideBarTop;
    this.slideBarBottom = slideBarBottom;

    this.height = slideBar.h;
    this.width = slideBar.w;
    this.currentValue = this.minValue = 0;
    this.maxValue = slideBar.h;
    this.increment = 1;

    this.sliderOffset = MathHelper.abs(slideBar.w - slider.w) / 2;

    this.isScrolling = false;
    this.isHighlighted = false;

    this.enabled = true;
    this.hidden = false;
    this.useMouseWheel = false;
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
    this.setSliderValue(this.currentValue);
  }

  public int getValue() {
    if(this.isHidden()) {
      return 0;
    }
    return Math.min(this.maxValue, Math.max(this.minValue, this.currentValue));
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
    if(this.hidden) {
      return;
    }

    // slidebar background
    this.slideBarTop.draw(this.xPos, this.yPos);
    this.slideBar.drawScaledY(this.xPos, this.yPos + this.slideBarTop.h, this.getUsableSlidebarHeight());
    this.slideBarBottom.draw(this.xPos, this.yPos + this.height - this.slideBarBottom.h);

    int x = this.xPos + this.sliderOffset;
    int y = this.yPos + this.getSliderTop();

    // the slider depending on state
    if(this.enabled) {
      if(this.isScrolling) {
        this.sliderDisabled.draw(x, y);
      }
      else if(this.isHighlighted) {
        this.sliderHighlighted.draw(x, y);
      }
      else {
        this.slider.draw(x, y);
      }
    }
    else {
      this.sliderDisabled.draw(x, y);
    }
  }

  public void update(int mouseX, int mouseY, boolean useMouseWheel) {
    if(!this.enabled || this.hidden) {
      return;
    }

    boolean mouseDown = Minecraft.getInstance().mouseHelper.isLeftDown(); // left mouse button
    this.useMouseWheel = useMouseWheel;

    // relative position inside the widget
    int x = mouseX - this.xPos;
    int y = mouseY - this.yPos;

    // reset click data
    if(!mouseDown && this.clickedBar) {
      this.clickedBar = false;
    }

    // button not pressed and scrolling -> stop scrolling
    if(!mouseDown && this.isScrolling) {
      this.isScrolling = false;
    }
    // button pressed and scrolling -> update position of slider
    else if(this.isScrolling) {
      float d = this.maxValue - this.minValue;
      float val = (float) (y - this.clickY) / (float) (this.getUsableSlidebarHeight() - this.slider.h);
      val *= d;

      if(val < (float) this.increment / 2f) {
        // < 1/2 increment
        this.setSliderValue(this.minValue);
      }
      else if(val > this.maxValue - ((float) this.increment / 2f)) {
        // > max-1/2 increment
        this.setSliderValue(this.maxValue);
      }
      else {
        // in between
        this.setSliderValue((int) (this.minValue + (float) this.increment * Math.round(val)));
      }
    }
    // not scrolling yet but possibly inside the slider
    else if(x >= 0 && y >= this.getSliderTop() &&
            x - this.sliderOffset <= this.slider.w && y <= this.getSliderTop() + this.slider.h) {
      this.isHighlighted = true;
      if(mouseDown) {
        this.isScrolling = true;
        this.clickX = x - this.sliderOffset;
        this.clickY = y - this.getSliderTop();
      }
    }
    // not on the slider but clicked on the bar
    else if(mouseDown && !this.clickedBar &&
            x >= 0 && y >= 0 &&
            x <= this.slideBar.w && y <= this.height) {
      if(y < this.getSliderTop()) {
        this.decrement();
      }
      else {
        this.increment();
      }

      this.clickedBar = true;
    }
    else {
      this.isHighlighted = false;
    }
  }

  @SubscribeEvent
  public void onGuiMouseEvent(GuiScreenEvent.MouseScrollEvent.Pre event) {
    double dWheel = event.getScrollDelta();
    double mouseX = event.getMouseX();
    double mouseY = event.getMouseY();

    if(this.useMouseWheel) {
      if(dWheel > 0.0) {
        this.decrement();
        event.setCanceled(true);
        return;
      }
      else if(dWheel < 0.0) {
        this.increment();
        event.setCanceled(true);
        return;
      }
    }
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
    if(val > this.maxValue) {
      val = this.maxValue;
    }
    else if(val < this.minValue) {
      val = this.minValue;
    }

    this.currentValue = val;
    return this.currentValue;
  }

  private int getSliderTop() {
    float d = this.maxValue - this.minValue;
    d = (float) (this.currentValue - this.minValue) / d;
    d *= this.getUsableSlidebarHeight() - this.slider.h;

    return (int) d + this.slideBarTop.h;
  }

  private int getUsableSlidebarHeight() {
    return this.height - this.slideBarTop.h - this.slideBarBottom.h;
  }
}
