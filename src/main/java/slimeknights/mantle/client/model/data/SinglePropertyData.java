package slimeknights.mantle.client.model.data;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.model.IModelData;
import slimeknights.mantle.util.ModelProperty;

/**
 * IModelData instance that holds a single model data property. Will be more efficient than the map implementation in cases without more properties.
 * @param <D>  Property type, for data validation
 */
public class SinglePropertyData<D> implements IModelData {
  private final ModelProperty<D> property;
  private D data;

  public SinglePropertyData(ModelProperty<D> property) {
    this.property = property;
  }

  /**
   * Creates an instance with a property and preset data
   * @param property  Property for this instance
   * @param data      Original data
   */
  public SinglePropertyData(ModelProperty<D> property, D data) {
    Preconditions.checkArgument(property.test(data), "Value is invalid for this property");
    this.property = property;
    this.data = data;
  }

  @Override
  public boolean hasProperty(ModelProperty<?> prop) {
    return prop == this.property;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getData(ModelProperty<T> prop) {
    if (prop == this.property) {
      return (T) data;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T setData(ModelProperty<T> prop, T data) {
    Preconditions.checkArgument(prop.test(data), "Value is invalid for this property");
    if (prop == this.property) {
      this.data = (D) data;
      return data;
    }
    return null;
  }
}
