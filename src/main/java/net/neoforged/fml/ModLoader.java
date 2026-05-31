package slimeknights.mantle.compat.neoforged.fml;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/** Compatibility shim for old Forge ModLoader helpers. */
public final class ModLoader {
  private static final ModLoader INSTANCE = new ModLoader();

  private ModLoader() {}

  public static ModLoader get() {
    return INSTANCE;
  }

  public static boolean hasErrors() {
    return false;
  }

  public static boolean isLoadingStateValid() {
    return true;
  }

  public <T extends Event> T postEvent(T event) {
    NeoForge.EVENT_BUS.post(event);
    return event;
  }
}
