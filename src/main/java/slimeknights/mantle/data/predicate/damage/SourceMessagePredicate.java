package slimeknights.mantle.data.predicate.damage;

import net.minecraft.world.damagesource.DamageSource;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/** Predicate that matches a named source */
public record SourceMessagePredicate(String message) implements DamageSourcePredicate {
  public static final RecordLoadable<SourceMessagePredicate> LOADER = RecordLoadable.create(StringLoadable.DEFAULT.requiredField("message", SourceMessagePredicate::message), SourceMessagePredicate::new);

  public SourceMessagePredicate(DamageSource source) {
    this(source.getMsgId());
  }

  @Override
  public boolean matches(DamageSource source) {
    return message.equals(source.getMsgId());
  }

  @Override
  public IGenericLoader<? extends DamageSourcePredicate> getLoader() {
    return LOADER;
  }
}
