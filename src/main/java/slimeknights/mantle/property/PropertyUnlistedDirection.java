package slimeknights.mantle.property;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import net.minecraft.state.DirectionProperty;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;

public class PropertyUnlistedDirection extends DirectionProperty implements IUnlistedProperty<EnumFacing> {

  public PropertyUnlistedDirection(String name, Collection<EnumFacing> values) {
    super(name, values);
  }

  public PropertyUnlistedDirection(String name, Predicate<EnumFacing> filter) {
    this(name, Collections2.filter(Lists.newArrayList(EnumFacing.values()), filter));
  }

  @Override
  public boolean isValid(EnumFacing value) {
    return true;
  }

  @Override
  public Class<EnumFacing> getType() {
    return this.getValueClass();
  }

  @Override
  public String valueToString(EnumFacing value) {
    return getName(value);
  }

}
