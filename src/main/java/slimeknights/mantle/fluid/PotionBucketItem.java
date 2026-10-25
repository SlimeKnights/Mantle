package slimeknights.mantle.fluid;

import lombok.Getter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;
import java.util.Optional;

/** Implements filling a bucket with an NBT fluid */
@Getter
public class PotionBucketItem extends PotionItem {
  private final Fluid fluid;
  public PotionBucketItem(Fluid fluid, Properties builder) {
    super(builder);
    this.fluid = fluid;
  }

  @Override
  public String getDescriptionId(ItemStack stack) {
    String baseKey = getDescriptionId();
    String bucketKey = Potion.getName(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), baseKey + ".effect.");
    if (TranslationHelper.canTranslate(bucketKey)) {
      return bucketKey;
    }
    return baseKey;
  }

  @Override
  public Component getName(ItemStack stack) {
    String baseKey = getDescriptionId();
    Optional<Holder<Potion>> potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
    String bucketKey = Potion.getName(potion, baseKey + ".effect.");
    if (TranslationHelper.canTranslate(bucketKey)) {
      return Component.translatable(bucketKey);
    }
    // default to filling with the contents
    return Component.translatable(baseKey + ".contents", Component.translatable(Potion.getName(potion, "item.minecraft.potion.effect.")));
  }

  @Override
  public ItemStack getDefaultInstance() {
    ItemStack stack = new ItemStack(this);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD));
    return stack;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    Player player = living instanceof Player p ? p : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    // effects are 2x duration
    if (!level.isClientSide) {
      PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      contents.forEachEffect(instance -> {
        MobEffect effect = instance.getEffect().value();
        if (effect.isInstantenous()) {
          effect.applyInstantenousEffect(player, player, living, instance.getAmplifier(), 2.5D);
        } else {
          MobEffectInstance newEffect = new MobEffectInstance(instance);
          newEffect.duration = newEffect.duration * 5 / 2;
          living.addEffect(newEffect);
        }
      });
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      stack.consume(1, player);
    }

    if (player == null || !player.hasInfiniteMaterials()) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.BUCKET);
      }
      if (player != null) {
        player.getInventory().add(new ItemStack(Items.BUCKET));
      }
    }
    living.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      contents.addPotionTooltip(tooltip::add, 2.5f, context.tickRate());
    }
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 96; // 3x duration of potion bottles
  }


  /** Fluid handler capability for the potion bucket */
  public static class PotionBucketWrapper extends FluidBucketWrapper {
    private final Fluid fluid;
    public PotionBucketWrapper(Fluid fluid, ItemStack container) {
      super(container);
      this.fluid = fluid;
    }

    @Override
    public FluidStack getFluid() {
      FluidStack fluid = new FluidStack(this.fluid, FluidType.BUCKET_VOLUME);
      fluid.applyComponents(container.getComponentsPatch());
      return fluid;
    }
  }
}
