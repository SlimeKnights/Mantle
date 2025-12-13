package slimeknights.mantle.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Food item with a container that is returned when the item is consumed. Supports eating stackable items with containers.
 * Technically also works for items with no container.
 */
@SuppressWarnings("unused") // API
public class ContainerFoodItem extends Item {
  private final UseAnim useAnim;
  public ContainerFoodItem(Properties props, UseAnim useAnim) {
    super(props);
    this.useAnim = useAnim;
  }

  public ContainerFoodItem(Properties props) {
    this(props, UseAnim.DRINK);
  }

  @Override
  public UseAnim getUseAnimation(ItemStack pStack) {
    return useAnim;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(FoodProperties food, List<Component> tooltip) {
    // add effects to the tooltip, code based on potion items
    for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
      MobEffectInstance effect = pair.getFirst();
      if (effect != null) {
        MutableComponent mutable = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
          mutable = Component.translatable("potion.withAmplifier", mutable, Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        if (effect.getDuration() > 20) {
          mutable = Component.translatable("potion.withDuration", mutable, MobEffectUtil.formatDuration(effect, 1.0f));
        }
        tooltip.add(mutable.withStyle(effect.getEffect().getCategory().getTooltipFormatting()));
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    FoodProperties food = stack.getFoodProperties(null);
    if (food != null) {
      addEffectTooltip(food, tooltip);
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    ItemStack container = stack.getCraftingRemainder();
    ItemStack result = super.finishUsingItem(stack, level, living);
    Player player = living instanceof Player p ? p : null;
    if (!container.isEmpty() && (player == null || !player.getAbilities().instabuild)) {
      container = container.copy();
      if (result.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return result;
  }

  /** Fluid containing variant of {@link ContainerFoodItem} */
  public static class FluidContainerFoodItem extends ContainerFoodItem {
    private static final Set<FluidContainerFoodItem> REGISTERED_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Supplier<FluidStack> fluid;
    public FluidContainerFoodItem(Properties props, Supplier<FluidStack> fluid) {
      super(props);
      this.fluid = fluid;
      REGISTERED_ITEMS.add(this);
    }

    /** Registers the needed item capabilities for all instances of this item class. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      for (FluidContainerFoodItem item : REGISTERED_ITEMS) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new ConstantFluidContainerWrapper(item.fluid.get(), stack), item);
      }
    }
  }
}
