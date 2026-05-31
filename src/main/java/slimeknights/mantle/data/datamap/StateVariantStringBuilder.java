package slimeknights.mantle.data.datamap;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

/** Builder for a block state string used in {@link BlockStateDataMapLoader} */
public class StateVariantStringBuilder {
  private final Block owner;
  private final Collection<Property<?>> properties;
  private final SortedMap<Property<?>, Comparable<?>> setStates = new TreeMap<>(Comparator.comparing(Property::getName));

  public StateVariantStringBuilder(Block owner) {
    this.owner = owner;
    this.properties = owner.getStateDefinition().getProperties();
  }

  /** Sets a property in the builder */
  public <T extends Comparable<T>> StateVariantStringBuilder when(Property<T> prop, T value) {
    // property must be valid
    if (!properties.contains(prop)) {
      throw new IllegalArgumentException("Property " + prop + " is not valid for " + BuiltInRegistries.BLOCK.getKey(owner));
    }
    // property must not be set already
    Comparable<?> oldValue = setStates.putIfAbsent(prop, value);
    if (oldValue != null) {
      throw new IllegalArgumentException("Property " + prop + " has already been set");
    }
    return this;
  }

  /** Builds the final string. Based on {@link net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder#toString()}*/
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})  // not another good way to handle it
  public String toString() {
    StringBuilder ret = new StringBuilder();
    for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
      if (ret.length() > 0) {
        ret.append(',');
      }
      ret.append(entry.getKey().getName())
         .append('=')
         .append(((Property) entry.getKey()).getName(entry.getValue()));
    }
    return ret.toString();
  }

  /* Helper */

  private static final Splitter COMMA_SPLITTER = Splitter.on(',');
  private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);

  /** Clone of {@link net.minecraft.client.resources.model.ModelBakery#predicate(StateDefinition, String)} as I wish to use it in possibly non-client contexts */
  public static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> container, String pVariant) {
    Map<Property<?>, Comparable<?>> map = Maps.newHashMap();

    for(String propString : COMMA_SPLITTER.split(pVariant)) {
      Iterator<String> iterator = EQUAL_SPLITTER.split(propString).iterator();
      if (iterator.hasNext()) {
        String propName = iterator.next();
        Property<?> property = container.getProperty(propName);
        if (property != null && iterator.hasNext()) {
          String valueString = iterator.next();
          Comparable<?> comparable = property.getValue(valueString).orElse(null);
          if (comparable == null) {
            throw new RuntimeException("Unknown value: '" + valueString + "' for blockstate property: '" + propName + "' " + property.getPossibleValues());
          }

          map.put(property, comparable);
        } else if (!propName.isEmpty()) {
          throw new RuntimeException("Unknown blockstate property: '" + propName + "'");
        }
      }
    }

    Block block = container.getOwner();
    return state -> {
      if (state == null || !state.is(block)) {
        return false;
      }
      for(Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
        if (!Objects.equals(state.getValue(entry.getKey()), entry.getValue())) {
          return false;
        }
      }
      return true;
    };
  }
}
