package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/**
 * Predicate that checks if the given entity has the given enchantment on any of their equipment
 */
public record HasEnchantmentEntityPredicate(Enchantment enchantment) implements LivingEntityPredicate {
  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(Loadables.ENCHANTMENT.requiredField("enchantment", HasEnchantmentEntityPredicate::enchantment), HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    return EnchantmentHelper.getEnchantmentLevel(enchantment, entity) > 0;
  }

  @Override
  public IGenericLoader<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
