package slimeknights.mantle.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Handles all custom data component types and attachments added by Mantle */
public class MantleData {
  private static final DataComponentTypeDeferredRegister DATA_COMPONENTS = new DataComponentTypeDeferredRegister(Registries.DATA_COMPONENT_TYPE, Mantle.modId);
  private static final AttachmentTypeDeferredRegister ATTACHMENTS = new AttachmentTypeDeferredRegister(Mantle.modId);

  private MantleData() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
    ATTACHMENTS.register(bus);
  }

  /** Component used by {@link slimeknights.mantle.util.RetexturedHelper} to set the block texture. */
  public static final DeferredDataComponent<Block> BLOCK_TEXTURE = DATA_COMPONENTS.registerPersistent("block_texture", BuiltInRegistries.BLOCK.byNameCodec(), MantleStreamCodecs.BLOCK);

  /**
   * Component used by {@link slimeknights.mantle.MantleEvents} to temporarily store the soulbound slot on an item. Is not serialized.
   * May be used by dependencies mods in {@link LivingDeathEvent} to make items soulbound for other reasons.
   */
  public static final DeferredDataComponent<Integer> SOULBOUND_SLOT = DATA_COMPONENTS.registerVolatile("soulbound_slot", ByteBufCodecs.VAR_INT);

  /** Component used by {@link slimeknights.mantle.client.book.BookHelper} to store the last viewed page. */
  public static final DeferredDataComponent<String> BOOK_PAGE = DATA_COMPONENTS.registerPersistent("book_page", Codec.STRING, ByteBufCodecs.STRING_UTF8);

  /** Attachment used by {@link slimeknights.mantle.util.OffhandCooldownTracker} to store the cooldown amount. */
  public static final DeferredAttachment<OffhandCooldownTracker> OFFHAND_COOLDOWN = ATTACHMENTS.registerType("offhand_cooldown", () -> AttachmentType.builder(OffhandCooldownTracker::new));
}
