package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationHelper {
  /**
   * Used to mark injected registry objects, as despite being set to null they will be nonnull at runtime.
   * @param <T>  Class type
   * @return  Null, its a lie
   */
  @SuppressWarnings("ConstantConditions")
  public static <T> T injected() {
    return null;
  }
}
