package slimeknights.mantle.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.PossibleEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;
import java.util.Objects;

/** Food item that includes food effects in the tooltip and easily overriding the food animation. */
@SuppressWarnings("unused")  // API
public class EdibleItem extends Item {
  private final UseAnim useAnim;
  public EdibleItem(UseAnim useAnim, Item.Properties properties) {
    super(properties);
    this.useAnim = useAnim;
    Objects.requireNonNull(components().get(DataComponents.FOOD), "Must set food to make an EdibleItem");
  }

  public EdibleItem(Item.Properties properties) {
    this(UseAnim.EAT, properties);
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return useAnim;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip) {
    FoodProperties food = stack.getFoodProperties(null);
    if (food == null) {
      return;
    }
    float ticksPerSecond = 20;
    Level level = context.level();
    if (level != null) {
      ticksPerSecond = level.tickRateManager().tickrate();
    }

    // add effects to the tooltip, code based on potion items
    for (PossibleEffect possibleEffect : food.effects()) {
      MobEffectInstance effect = possibleEffect.effect();
      MutableComponent mutable = Component.translatable(effect.getDescriptionId());
      if (effect.getAmplifier() > 0) {
        mutable = Component.translatable("potion.withAmplifier", mutable, Component.translatable("potion.potency." + effect.getAmplifier()));
      }
      if (effect.getDuration() > 20) {
        mutable = Component.translatable("potion.withDuration", mutable, MobEffectUtil.formatDuration(effect, 1, ticksPerSecond));
      }
      tooltip.add(mutable.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting()));
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    addEffectTooltip(stack, context, tooltip);
  }
}
