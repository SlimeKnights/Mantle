package slimeknights.mantle.property;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;

public class PropertyUnlistedDirection extends DirectionProperty implements IUnlistedProperty<Direction> {

  public PropertyUnlistedDirection(String name, Collection<Direction> values) {
    super(name, values);
  }

  public PropertyUnlistedDirection(String name, Predicate<Direction> filter) {
    this(name, Collections2.filter(Lists.newArrayList(Direction.values()), filter));
  }

  @Override
  public boolean isValid(Direction value) {
    return true;
  }

  @Override
  public Class<Direction> getType() {
    return this.getValueClass();
  }

  @Override
  public String valueToString(Direction value) {
    return getName(value);
  }

}
