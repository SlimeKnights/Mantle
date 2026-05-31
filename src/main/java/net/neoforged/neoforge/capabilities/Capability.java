package slimeknights.mantle.compat.neoforged.neoforge.capabilities;

import slimeknights.mantle.compat.neoforged.neoforge.common.util.LazyOptional;

/** Compatibility shim for older Forge capability signatures. */
public class Capability<T> {
  public <U> LazyOptional<U> orEmpty(Capability<U> capability, LazyOptional<T> value) {
    return this == capability ? value.cast() : LazyOptional.empty();
  }
}
