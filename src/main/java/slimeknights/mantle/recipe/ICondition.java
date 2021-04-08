package slimeknights.mantle.recipe;

import net.minecraft.util.Identifier;

public interface ICondition
{
  Identifier getID();

  boolean test();
}