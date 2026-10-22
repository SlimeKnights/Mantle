package slimeknights.mantle.registration.deferred;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttribute;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link Attribute}. Has a builder for common attribute types. */
@SuppressWarnings("unused")  // API
public class AttributeDeferredRegister extends DeferredRegister<Attribute> {
  public AttributeDeferredRegister(String modID) {
    super(Registries.ATTRIBUTE, modID);
  }

  /** Creates a tooltip string for the given name. */
  protected String descriptionId(String name) {
    return "attribute.name." + getNamespace() + "." + name;
  }

  @Override
  protected <T extends Attribute> DeferredHolder<Attribute, T> createHolder(ResourceKey<? extends Registry<Attribute>> registryKey, ResourceLocation key) {
    return DeferredAttribute.createAttribute(ResourceKey.create(registryKey, key));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <I extends Attribute> DeferredAttribute<I> register(String name, Supplier<? extends I> sup) {
    return (DeferredAttribute<I>) super.register(name, sup);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <I extends Attribute> DeferredAttribute<I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return (DeferredAttribute<I>) super.register(name, func);
  }

  /** Creates a builder for the given name */
  public Builder register(String name) {
    return new Builder(name);
  }

  /** Builder to create an attribute */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public class Builder {
    private final String name;
    private boolean syncable = false;
    /** Effect sentiment, changing the tooltip colors for positive and negative. Defaults to {@link Attribute.Sentiment#POSITIVE} */
    @Setter @Accessors(fluent = true)
    private Attribute.Sentiment sentiment = Attribute.Sentiment.POSITIVE;

    /** Makes the attribute sync to client */
    public Builder sync() {
      syncable = true;
      return this;
    }

    /** Marks this as a neutral sentiment effect */
    public Builder neutral() {
      return sentiment(Attribute.Sentiment.NEUTRAL);
    }

    /** Marks this as a negative sentiment effect */
    public Builder negative() {
      return sentiment(Attribute.Sentiment.NEGATIVE);
    }

    /** Builds the final attribute for an arbitrary constructor. */
    public <T extends Attribute> DeferredAttribute<T> build(Function<String,? extends T> constructor) {
      return register(name, () -> {
        T instance = constructor.apply("attribute.name." + getNamespace() + "." + name);
        instance.setSyncable(syncable).setSentiment(sentiment);
        return instance;
      });
    }

    /** Builds a ranged attribute with the given min and max. */
    public DeferredAttribute<RangedAttribute> ranged(float defaultValue, float min, float max) {
      return build(tooltip -> new RangedAttribute(tooltip, defaultValue, min, max));
    }

    /** Builds a percent attribute with the given min and max. */
    public DeferredAttribute<PercentageAttribute> percent(float defaultValue, float min, float max) {
      return build(tooltip -> new PercentageAttribute(tooltip, defaultValue, min, max));
    }

    /** Builds a percent attribute between 0 and 100%. */
    public DeferredAttribute<PercentageAttribute> percent(float defaultValue) {
      return percent(defaultValue, 0, 1);
    }

    /** Builds a percent attribute between 0 and 100 with a default of 1. */
    public DeferredAttribute<PercentageAttribute> multiplier() {
      return percent(1, 0, 100);
    }

    /** Builds a boolean attribute with the given default value. */
    public DeferredAttribute<BooleanAttribute> bool(boolean defaultValue) {
      return build(tooltip -> new BooleanAttribute(tooltip, defaultValue));
    }
  }
}
