package slimeknights.mantle.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import slimeknights.mantle.util.TranslationHelper;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class EdibleItem extends Item {

  /** if false, does not display effects of food in tooltip */
  private boolean displayEffectsTooltip;

  public EdibleItem(FoodComponent foodIn, ItemGroup itemGroup) {
    this(foodIn, itemGroup, true);
  }

  public EdibleItem(FoodComponent foodIn, ItemGroup itemGroup, boolean displayEffectsTooltip) {
    super(new Settings().food(foodIn).group(itemGroup));
    this.displayEffectsTooltip = displayEffectsTooltip;
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);

    if (this.displayEffectsTooltip) {
      for (Pair<StatusEffectInstance, Float> pair : stack.getItem().getFoodComponent().getStatusEffects()) {
        if (pair.getFirst() != null) {
          tooltip.add(new LiteralText(I18n.translate(pair.getFirst().getTranslationKey()).trim()).formatted(Formatting.GRAY));
        }
      }
    }

    super.appendTooltip(stack, worldIn, tooltip, flagIn);
  }
}
