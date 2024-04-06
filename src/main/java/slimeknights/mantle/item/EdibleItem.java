package slimeknights.mantle.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class EdibleItem extends Item {
  public EdibleItem(FoodProperties foodIn) {
    this(new Properties().food(foodIn));
  }

  public EdibleItem(Item.Properties properties) {
    super(properties);
    Objects.requireNonNull(getFoodProperties(), "Must set food to make an EdibleItem");
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    for (Pair<MobEffectInstance, Float> pair : Objects.requireNonNull(stack.getItem().getFoodProperties(stack, null)).getEffects()) {
      if (pair.getFirst() != null) {
        tooltip.add(Component.literal(I18n.get(pair.getFirst().getDescriptionId()).trim()).withStyle(ChatFormatting.GRAY));
      }
    }
  }
}
