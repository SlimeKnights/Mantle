package slimeknights.mantle.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.function.BooleanSupplier;

/**
 * Same as {@link CachedValue}, but implements {@link BooleanSupplier}
 */
public class CachedBoolean extends CachedValue<Boolean> implements BooleanSupplier {
  /**
   * Creates a new class instance using a boolean supplier
   * @param supplier  Boolean supplier
   */
  public CachedBoolean(BooleanSupplier supplier) {
    super(supplier::getAsBoolean);
  }

  /**
   * Creates a new instance from a boolean value
   * @param config  Boolean value
   */
  public CachedBoolean(ConfigValue<Boolean> config) {
    super(config);
  }

  @Override
  public boolean getAsBoolean() {
    // map null to false
    return get() == Boolean.TRUE;
  }
}
