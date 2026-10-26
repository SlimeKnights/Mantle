package slimeknights.mantle.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.fluid.MobEffectLiquidBlock;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.item.ConstantFluidHandlerItem;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.FlowingFluidObject;
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
  /** Deferred register instance for registering honey */
  private static final FluidDeferredRegister HONEY_REGISTER = new FluidDeferredRegister(Mantle.modId);
  /** Deferred register instance for registering honey */
  private static final FluidDeferredRegister STEW_REGISTER = new FluidDeferredRegister(Mantle.modId);

  /** Starts a builder for a Mantle fluid */
  private static FluidType.Properties mantle(String name) {
    return FluidType.Properties.create().descriptionId(Mantle.makeDescriptionId("fluid", name));
  }

  /** Creates a builder for a stew */
  private static FluidType.Properties stew(String name) {
    return mantle(name).motionScale(0.002).canExtinguish(true).temperature(400)
      .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
      .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
  }

  /** Potion fluid, using {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS} to determine type. Use {@link #registerPotion(IEventBus)} to enable. */
  public static final FluidObject<UnplaceableFluid> potion = POTION_REGISTER.register("potion").type(() -> new PotionFluidType(FluidType.Properties.create()
    .descriptionId("item.minecraft.potion.effect.empty").temperature(315)
    .sound(SoundActions.BUCKET_FILL, SoundEvents.BOTTLE_FILL)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BOTTLE_EMPTY)
  )).bucket(fluid -> new PotionBucketItem(fluid, RegistrationHelper.BUCKET_PROPS)).commonTag().unplacable();

  /** Powder snow fluid for {@link Items#POWDER_SNOW_BUCKET}. Use {@link #registerPowderSnow(IEventBus)} to enable. */
  public static final FluidObject<UnplaceableFluid> powderSnow = POWDER_SNOW_REGISTER.register("powder_snow").bucket(() -> Items.POWDER_SNOW_BUCKET).type(mantle("powder_snow")
    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW)
    .temperature(270)
  ).commonTag().unplacable();

  /** Honey fluid for {@link Items#HONEY_BOTTLE}. Use {@link #registerHoney(IEventBus)} to enable. */
  public static FlowingFluidObject<BaseFlowingFluid> honey = HONEY_REGISTER.register("honey").type(mantle("honey")
    .sound(SoundActions.BUCKET_FILL, SoundEvents.HONEY_BLOCK_BREAK)
    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.HONEY_BLOCK_PLACE)
    .temperature(301).motionScale(0.002).canExtinguish(true)
  ).tickRate(50).bucket().block(MobEffectLiquidBlock.createEffect(MapColor.COLOR_ORANGE, 0, () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5*20, 1))).commonTag().flowing();

  /** Beetroot soup fluid for {@link Items#BEETROOT_SOUP}. Use {@link #registerStew(IEventBus)} to enable. */
  public static FlowingFluidObject<BaseFlowingFluid> beetrootSoup = STEW_REGISTER.register("beetroot_soup").type(mantle("beetroot_soup")).bucket().block(MapColor.COLOR_RED, 0).commonTag().flowing();
  /** Mushroom stew fluid for {@link Items#MUSHROOM_STEW}. Use {@link #registerStew(IEventBus)} to enable. */
  public static FlowingFluidObject<BaseFlowingFluid> mushroomStew = STEW_REGISTER.register("mushroom_stew").type(mantle("mushroom_stew")).bucket().block(MapColor.DIRT,      0).commonTag().flowing();
  /** Rabbit stew fluid for {@link Items#RABBIT_STEW}. Use {@link #registerStew(IEventBus)} to enable. */
  public static FlowingFluidObject<BaseFlowingFluid> rabbitStew   = STEW_REGISTER.register("rabbit_stew"  ).type(mantle("rabbit_stew"  )).bucket().block(MapColor.PODZOL,    0).commonTag().flowing();


  /* Registration */
  private static boolean potionUnregistered = true;
  private static boolean powderSnowUnregistered = true;
  private static boolean honeyUnregistered = true;
  private static boolean stewUnregistered = true;

  /** Registers the potion fluid */
  public static void registerPotion(IEventBus bus) {
    if (potionUnregistered) {
      potionUnregistered = false;
      POTION_REGISTER.register(bus);
    }
  }

  /** Registers the powder snow fluid. Uses {@link Items#POWDER_SNOW_BUCKET} as its bucket. */
  public static void registerPowderSnow(IEventBus bus) {
    if (powderSnowUnregistered) {
      powderSnowUnregistered = false;
      POWDER_SNOW_REGISTER.register(bus);
    }
  }

  /** Registers the honey fluid. Supports fluid transfer from {@link Items#HONEY_BOTTLE}. */
  public static void registerHoney(IEventBus bus) {
    if (honeyUnregistered) {
      honeyUnregistered = false;
      HONEY_REGISTER.register(bus);
    }
  }

  /** Registers fluids for {@link Items#MUSHROOM_STEW}, {@link Items#RABBIT_STEW}, and {@link Items#BEETROOT_SOUP}. */
  public static void registerStew(IEventBus bus) {
    if (stewUnregistered) {
      stewUnregistered = false;
      STEW_REGISTER.register(bus);
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

  /** Registers dispenser behavior for the given fluid */
  private static void registerDispenser(FlowingFluidObject<?> fluid) {
    if (fluid.isBound()) {
      DispenserBlock.registerBehavior(fluid, DISPENCE_BUCKET);
    }
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      registerDispenser(honey);
      registerDispenser(beetrootSoup);
      registerDispenser(mushroomStew);
      registerDispenser(rabbitStew);
    });
  }

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

  @SubscribeEvent
  static void registerCauldrons(RegisterCauldronFluidContentEvent event) {
    if (powderSnow.isBound() && Config.POWDER_SNOW_CAULDRON_FLUID.getAsBoolean()) {
      try {
        event.register(Blocks.POWDER_SNOW_CAULDRON, powderSnow.get(), 1000, LayeredCauldronBlock.LEVEL);
      } catch (IllegalArgumentException e) {
        Mantle.logger.debug("Skipping registering powder snow as the fluid for powder snow cauldron as another mod already registered a fluid.");
      }
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
    // most buckets make most sense in tools and utilities as they cannot be directly consumed
    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      if (honey.isBound()) event.accept(honey);
      if (beetrootSoup.isBound()) event.accept(beetrootSoup);
      if (mushroomStew.isBound()) event.accept(mushroomStew);
      if (rabbitStew.isBound()) event.accept(rabbitStew);
    }
  }
}
