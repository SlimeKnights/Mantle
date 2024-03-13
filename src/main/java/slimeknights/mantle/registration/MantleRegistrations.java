package slimeknights.mantle.registration;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/**
 * Various objects registered under Mantle
 */
public class MantleRegistrations {
  private MantleRegistrations() {}

  @ObjectHolder(registryName = "minecraft:block_entity_type", value = Mantle.modId+":sign")
  public static final BlockEntityType<MantleSignBlockEntity> SIGN = injected();
}
