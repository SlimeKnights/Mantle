package slimeknights.mantle.registration.deferred;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.BooleanAttribute;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

/** Deferred register for making attribute instances */
@SuppressWarnings("unused")  // API
public class AttributeDeferredRegister extends DeferredRegister<Attribute> {
  public AttributeDeferredRegister(String modID) {
    super(Registries.ATTRIBUTE, modID);
  }

  /** Creates a tooltip string for the given name. */
  protected String descriptionId(String name) {
    return "attribute.name." + getNamespace() + "." + name;
  }

  /** Creates a builder for the given name */
  public Builder register(String name) {
    return new Builder(name);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public class Builder {
    private final String name;
    private boolean syncable = false;
    @Setter @Accessors(fluent = true)
    private Attribute.Sentiment sentiment = Attribute.Sentiment.POSITIVE;

    /** Makes the attribute sync to client */
    public Builder sync() {
      syncable = true;
      return this;
    }

    /** Builds the final attribute for an arbitrary constructor. */
    public DeferredHolder<Attribute,Attribute> build(Function<String,? extends Attribute> constructor) {
      return register(name, () -> constructor.apply("attribute.name." + getNamespace() + "." + name).setSyncable(syncable).setSentiment(sentiment));
    }

    /** Builds a ranged attribute with the given min and max. */
    public DeferredHolder<Attribute,Attribute> ranged(float defaultValue, float min, float max) {
      return build(tooltip -> new RangedAttribute(tooltip, defaultValue, min, max));
    }

    /** Builds a percent attribute with the given min and max. */
    public DeferredHolder<Attribute,Attribute> percent(float defaultValue, float min, float max) {
      return build(tooltip -> new PercentageAttribute(tooltip, defaultValue, min, max));
    }

    /** Builds a percent attribute between 0 and 100%. */
    public DeferredHolder<Attribute,Attribute> percent(float defaultValue) {
      return percent(defaultValue, 0, 1);
    }

    /** Builds a percent attribute between 0 and 100 with a default of 1. */
    public DeferredHolder<Attribute,Attribute> multiplier(float defaultValue) {
      return percent(1, 0, 100);
    }

    /** Builds a boolean attribute with the given default value. */
    public DeferredHolder<Attribute,Attribute> bool(boolean defaultValue) {
      return build(tooltip -> new BooleanAttribute(tooltip, defaultValue));
    }
  }
}
