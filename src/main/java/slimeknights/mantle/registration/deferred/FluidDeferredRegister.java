package slimeknights.mantle.registration.deferred;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.block.fluid.BurningLiquidBlock;
import slimeknights.mantle.block.fluid.MobEffectLiquidBlock;
import slimeknights.mantle.fluid.InvertedFluid;
import slimeknights.mantle.fluid.UnplaceableFluid;
import slimeknights.mantle.registration.DelayedSupplier;
import slimeknights.mantle.registration.FluidBuilder;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register solving the nightmare that is registering fluids with Forge
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FluidDeferredRegister extends DeferredRegister<Fluid> {
  protected final DeferredRegister<FluidType> fluidTypes;
  protected final DeferredRegister<Block> blocks;
  protected final DeferredRegister<Item> items;

  /** Creates an instance from the given registers */
  public FluidDeferredRegister(String modID, DeferredRegister<FluidType> fluidTypes, DeferredRegister<Block> blocks, DeferredRegister<Item> items) {
    super(Registries.FLUID, modID);
    this.fluidTypes = fluidTypes;
    this.blocks = blocks;
    this.items = items;
  }

  /** Creates an instance with default registers */
  public FluidDeferredRegister(String modID) {
    this(modID,
      DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, modID),
      DeferredRegister.create(Registries.BLOCK, modID),
      DeferredRegister.create(Registries.ITEM, modID)
    );
  }

  /** Registers all registers with the bus */
  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    fluidTypes.register(bus);
    blocks.register(bus);
    items.register(bus);
  }

  /**
   * Gets a resource location object for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected ResourceLocation resource(String name) {
    return ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
  }

  /** Adds an alias for the given fluid and fluid type */
  public void addAliasWithType(ResourceLocation from, ResourceLocation to, boolean bucket) {
    addAlias(from, to);
    fluidTypes.addAlias(from, to);
    if (bucket) {
      String suffix = "_bucket";
      items.addAlias(from.withSuffix(suffix), to.withSuffix(suffix));
    }
  }

  /** Adds an alias for the given fluid, flowing fluid, and fluid type */
  public void addFlowingAlias(ResourceLocation from, ResourceLocation to, boolean bucket) {
    addAliasWithType(from, to, bucket);
    String flowing = "flowing_";
    addAlias(from.withPrefix(flowing), to.withPrefix(flowing));
    String block = "_fluid";
    blocks.addAlias(from.withSuffix(block), to.withSuffix(block));
  }


  /**
   * Registers a fluid type to the registry
   * @param name  Name of the fluid to register
   * @param sup   Fluid supplier
   * @param <I>   Fluid type
   * @return  Fluid to supply
   */
  public <I extends FluidType> DeferredHolder<FluidType,I> registerType(String name, Supplier<? extends I> sup) {
    return fluidTypes.register(name, sup);
  }

  /** Starts a builder for a fluid */
  public Builder register(String name) {
    return new Builder(name);
  }

  @Accessors(fluent = true)
  @Setter
  public class Builder extends FluidBuilder<Builder> {
    private final String name;
    private final DelayedSupplier<Fluid> stillDelayed = new DelayedSupplier<>();
    /** Name for the common tag, if unset will only get a local tag */
    @Nullable
    private String commonTag = null;

    private Builder(String name) {
      this.name = name;
    }

    /** Adds a common tag to the builder */
    public Builder commonTag() {
      return this.commonTag(name);
    }

    /* Fluid type */

    /** Registers the passed fluid type */
    public Builder type(Supplier<? extends FluidType> type) {
      if (this.type != null) {
        throw new IllegalStateException("Type already created for " + name);
      }
      this.type = fluidTypes.register(name, type);
      return this;
    }

    /** Registers a fluid with the given properties, using the texture fluid type */
    public Builder type(FluidType.Properties properties) {
      return type(() -> new FluidType(properties));
    }

    /** Registers a fluid with the given properties, using the texture fluid type */
    public Builder type() {
      return type(FluidType.Properties.create());
    }


    /* Bucket */

    /** Creates the bucket using the given supplier */
    public Builder bucket(Function<Supplier<? extends Fluid>, Item> constructor) {
      if (this.bucket != null) {
        throw new IllegalStateException("Bucket already created for " + name);
      }
      return bucket(items.register(name + "_bucket", () -> constructor.apply(stillDelayed)));
    }

    /** Creates the default bucket */
    public Builder bucket() {
      return bucket(still -> new BucketItem(still.get(), RegistrationHelper.BUCKET_PROPS));
    }


    /* Block */

    /** Creates the block form using the given supplier */
    public Builder block(Function<FlowingFluid, LiquidBlock> constructor) {
      if (this.block != null) {
        throw new IllegalStateException("Block already created for " + name);
      }
      return block(blocks.register(name + "_fluid", () -> constructor.apply((FlowingFluid) stillDelayed.get())));
    }

    /** Creates the default block from the given material and light level */
    public Builder block(MapColor color, int lightLevel) {
      return block(fluid -> new LiquidBlock(fluid, createProperties(color, lightLevel)));
    }

    /** Creates a block that lights entities on fire and damages them over time */
    public Builder burningBlock(MapColor color, int lightLevel, int burnTime, float damage) {
      return block(BurningLiquidBlock.createBurning(color, lightLevel, burnTime, damage));
    }

    /** Creates a block that applies an effect to the target entity */
    public Builder mobEffectBlock(MapColor color, int lightLevel, Supplier<MobEffectInstance> effect) {
      return block(MobEffectLiquidBlock.createEffect(color, lightLevel, effect));
    }


    /* Final fluid */

    /** Builds an unplacable fluid with the default constructor */
    public FluidObject<UnplaceableFluid> unplacable() {
      return unplacable(UnplaceableFluid::new);
    }

    /**
     * Builds an unplacable fluid with the passed constructor
     * @param constructor  Constructor taking a fluid type and bucket supplier. Note the bucket supplier may be null.
     * @param <F> Resulting fluid type
     * @return  Fluid object instance
     */
    public <F extends Fluid> FluidObject<F> unplacable(Function<FluidBuilder<?>,F> constructor) {
      if (block != null) {
        throw new IllegalStateException("Cannot build an unplacable fluid with a block form");
      }
      if (type == null) {
        this.type();
      }
      DeferredHolder<Fluid,F> fluid = register(name, () -> constructor.apply(this));
      stillDelayed.setSupplier(fluid);
      return new FluidObject<>(resource(name), commonTag, type, fluid);
    }

    /** Builds a flowing fluid with the default constructors */
    public FlowingFluidObject<BaseFlowingFluid> flowing() {
      return flowing(BaseFlowingFluid.Source::new, BaseFlowingFluid.Flowing::new);
    }

    /** Builds a flowing fluid with the default constructors */
    public FlowingFluidObject<InvertedFluid> invertedFlowing() {
      return flowing(InvertedFluid.Source::new, InvertedFluid.Flowing::new);
    }

    /**
     * Builds a flowing fluid with the given constructors
     * @param createStill     Still constructor taking forge fluid properties, will contain the type, bucket, block, and flowing forms
     * @param createFlowing   Flowing constructor taking forge fluid properties, will contain the type, bucket, block, and still forms
     * @param <F>  Type of fluids being created
     * @return  Flowing fluid object instance
     */
    public <F extends FlowingFluid> FlowingFluidObject<F> flowing(Function<Properties,? extends F> createStill, Function<Properties,? extends F> createFlowing) {
      if (type == null) {
        this.type();
      }

      // create props with the suppliers
      DelayedSupplier<FlowingFluid> flowingDelayed = new DelayedSupplier<>();
      Properties props = build(type, stillDelayed, flowingDelayed);

      // create fluids now that we have props
      Supplier<F> still = register(name, () -> createStill.apply(props));
      stillDelayed.setSupplier(still);
      Supplier<F> flowing = register("flowing_" + name, () -> createFlowing.apply(props));
      flowingDelayed.setSupplier(flowing);

      // return the final nice object
      return new FlowingFluidObject<>(resource(name), commonTag, type, still, flowing, this.block);
    }
  }

  /** Creates properties for a fluid */
  public static BlockBehaviour.Properties createProperties(MapColor color, int lightLevel) {
    return BlockBehaviour.Properties.of().mapColor(color).replaceable().noCollission().randomTicks().strength(100.0F).lightLevel(state -> lightLevel).pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY);
  }
}
