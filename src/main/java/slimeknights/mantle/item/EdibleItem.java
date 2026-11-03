package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class EdibleItem extends Item {
  private final UseAnim useAnim;
  public EdibleItem(FoodProperties foodIn) {
    this(new Properties().food(foodIn));
  }

  public EdibleItem(UseAnim useAnim, Item.Properties properties) {
    super(properties);
    this.useAnim = useAnim;
    Objects.requireNonNull(foodProperties, "Must set food to make an EdibleItem");
  }

  public EdibleItem(Item.Properties properties) {
    this(UseAnim.EAT, properties);
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return useAnim;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    ContainerFoodItem.addEffectTooltip(Objects.requireNonNull(getFoodProperties(stack, null)), tooltip);
  }
}
