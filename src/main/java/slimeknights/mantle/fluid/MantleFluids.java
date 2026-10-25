package slimeknights.mantle.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.FluidObject;

/**
 * Handles fluids registered by Mantle.
 * By default, all of these fluids are only enabled if the relevant register method is called by a Mantle dependent.
 * The register methods are safe to call multiple times.
 */
@SuppressWarnings("unused") // API
@EventBusSubscriber(modid = Mantle.modId)
public class MantleFluids {
  /** Deferred register instance for registering potions */
  private static final FluidDeferredRegister POTION_REGISTER = new FluidDeferredRegister(Mantle.modId);

  /** Potion fluid, using {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS} to determine type. Use {@link #registerPotion(IEventBus)} to enable. */
  public static final FluidObject<UnplaceableFluid> potion = POTION_REGISTER.register("potion").type(() -> new PotionFluidType(FluidType.Properties.create()
    .descriptionId("item.minecraft.potion.effect.empty")
    .density(1100).viscosity(1100).temperature(315).motionScale(0.0023333333333333335D).canExtinguish(true)
    .sound(SoundActions.BUCKET_FILL, SoundEvents.BOTTLE_FILL)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BOTTLE_EMPTY)
  )).bucket(fluid -> new PotionBucketItem(fluid, RegistrationHelper.BUCKET_PROPS)).commonTag().unplacable();

  /* Method to enable registration */
  private static boolean potionUnregistered = true;

  /** Registers the potion fluid */
  public static void registerPotion(IEventBus bus) {
    if (potionUnregistered) {
      potionUnregistered = false;
      POTION_REGISTER.register(bus);
    }
  }

  @SubscribeEvent
  static void registerCapabilities(RegisterCapabilitiesEvent event) {
    if (potion.isBound()) {
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new PotionBucketItem.PotionBucketWrapper(potion.get(), stack), potion.asItem());
    }
  }

  /** Adds fluids to the relevant creative tabs */
  @SubscribeEvent
  static void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS && potion.isBound()) {
      BuiltInRegistries.POTION.holders()
        .filter(potion -> !potion.equals(Potions.WATER))
        .forEachOrdered(holder -> event.accept(PotionFluidType.potionBucket(holder)));
    }
  }
}
