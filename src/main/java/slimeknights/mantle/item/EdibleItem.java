package slimeknights.mantle.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

// TODO: make this more consistent with modern item classes, properties in constructor
public class EdibleItem extends Item {

  /** if false, does not display effects of food in tooltip */
  private final boolean displayEffectsTooltip;

  public EdibleItem(FoodProperties foodIn) {
    this(new Properties().food(foodIn), true);
  }

  public EdibleItem(Item.Properties properties, boolean displayEffectsTooltip) {
    super(properties);
    Objects.requireNonNull(getFoodProperties(), "Must set food to make an EdibleItem");
    this.displayEffectsTooltip = displayEffectsTooltip;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);

    if (this.displayEffectsTooltip) {
      for (Pair<MobEffectInstance, Float> pair : stack.getItem().getFoodProperties(stack, null).getEffects()) {
        if (pair.getFirst() != null) {
          tooltip.add(Component.literal(I18n.get(pair.getFirst().getDescriptionId()).trim()).withStyle(ChatFormatting.GRAY));
        }
      }
    }

    super.appendHoverText(stack, worldIn, tooltip, flagIn);
  }
}
