package slimeknights.mantle.compat.minecraft.commands;

import net.minecraft.network.chat.Component;

/** Compatibility runtime exception for old client-only command helpers. */
public class CommandRuntimeException extends RuntimeException {
  private final Component component;

  public CommandRuntimeException(Component component) {
    super(component.getString());
    this.component = component;
  }

  public Component getComponent() {
    return component;
  }
}
