package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.ConstantFluidHandlerItem;
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
  /** Deferred register instance for registering powder snow */
  private static final FluidDeferredRegister POWDER_SNOW_REGISTER = new FluidDeferredRegister(Mantle.modId);

  /** Potion fluid, using {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS} to determine type. Use {@link #registerPotion(IEventBus)} to enable. */
  public static final FluidObject<UnplaceableFluid> potion = POTION_REGISTER.register("potion").type(() -> new PotionFluidType(FluidType.Properties.create()
    .descriptionId("item.minecraft.potion.effect.empty")
    .density(1100).viscosity(1100).temperature(315).motionScale(0.0023333333333333335D).canExtinguish(true)
    .sound(SoundActions.BUCKET_FILL, SoundEvents.BOTTLE_FILL)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BOTTLE_EMPTY)
  )).bucket(fluid -> new PotionBucketItem(fluid, RegistrationHelper.BUCKET_PROPS)).commonTag().unplacable();

  /** Powder snow fluid for {@link Items#POWDER_SNOW_BUCKET}. Use {@link #registerPowderSnow(IEventBus)} to enable. */
  public static final FluidObject<UnplaceableFluid> powderSnow = POWDER_SNOW_REGISTER.register("powder_snow").bucket(() -> Items.POWDER_SNOW_BUCKET).type(FluidType.Properties.create()
    .descriptionId(Mantle.makeDescriptionId("fluid", "powder_snow"))
    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW)
    .temperature(270)
  ).commonTag().unplacable();

  /* Method to enable registration */
  private static boolean potionUnregistered = true;
  private static boolean powderSnowUnregistered = true;

  /** Registers the potion fluid */
  public static void registerPotion(IEventBus bus) {
    if (potionUnregistered) {
      potionUnregistered = false;
      POTION_REGISTER.register(bus);
    }
  }

  /** Registers the powder snow fluid. Uses {@link Items#POWDER_SNOW_BUCKET} as it's bucket. */
  public static void registerPowderSnow(IEventBus bus) {
    if (powderSnowUnregistered) {
      powderSnowUnregistered = false;
      POWDER_SNOW_REGISTER.register(bus);
    }
  }


  /* Events */

  /** Dispenser behavior for a bucket. */
  public static DispenseItemBehavior DISPENCE_BUCKET = new DefaultDispenseItemBehavior() {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {
      DispensibleContainerItem container = (DispensibleContainerItem)stack.getItem();
      BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
      Level level = source.level();
      if (container.emptyContents(null, level, pos, null, stack)) {
        container.checkExtraContent(null, level, stack, pos);
        return new ItemStack(Items.BUCKET);
      } else {
        return this.defaultDispenseItemBehavior.dispense(source, stack);
      }
    }
  };
  @SubscribeEvent
  static void registerCapabilities(RegisterCapabilitiesEvent event) {
    if (potion.isBound()) {
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new PotionBucketItem.PotionBucketWrapper(potion.get(), stack), potion.asItem());
    }
    // powder snow adds a capability to the vanilla powder snow bucket
    if (powderSnow.isBound()) {
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new ConstantFluidHandlerItem(new FluidStack(powderSnow.get(), FluidType.BUCKET_VOLUME), stack), powderSnow.asItem());
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
