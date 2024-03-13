package slimeknights.mantle.registration.deferred;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.fluid.TextureFluidType;
import slimeknights.mantle.registration.DelayedSupplier;
import slimeknights.mantle.registration.FluidBuilder;
import slimeknights.mantle.registration.ItemProperties;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register solving the nightmare that is registering fluids with Forge
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FluidDeferredRegister extends DeferredRegisterWrapper<Fluid> {
  private final SynchronizedDeferredRegister<FluidType> fluidTypeRegister;
  private final SynchronizedDeferredRegister<Block> blockRegister;
  private final SynchronizedDeferredRegister<Item> itemRegister;

  public FluidDeferredRegister(String modID) {
    super(Registry.FLUID_REGISTRY, modID);
    this.fluidTypeRegister = SynchronizedDeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, modID);
    this.blockRegister = SynchronizedDeferredRegister.create(Registry.BLOCK_REGISTRY, modID);
    this.itemRegister = SynchronizedDeferredRegister.create(Registry.ITEM_REGISTRY, modID);
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    fluidTypeRegister.register(bus);
    blockRegister.register(bus);
    itemRegister.register(bus);
  }

  /**
   * Registers a fluid type to the registry
   * @param name  Name of the fluid to register
   * @param sup   Fluid supplier
   * @param <I>   Fluid type
   * @return  Fluid to supply
   */
  public <I extends FluidType> RegistryObject<I> registerType(String name, Supplier<? extends I> sup) {
    return fluidTypeRegister.register(name, sup);
  }

  /**
   * Registers a fluid to the registry
   * @param name  Name of the fluid to register
   * @param sup   Fluid supplier
   * @param <I>   Fluid type
   * @return  Fluid to supply
   */
  public <I extends Fluid> RegistryObject<I> registerFluid(String name, Supplier<? extends I> sup) {
    return register.register(name, sup);
  }

  /** Starts a builder for a fluid */
  public Builder register(String name) {
    return new Builder(name);
  }

  @Accessors(fluent = true)
  @Setter
  public class Builder extends FluidBuilder<Builder> {
    private final String name;
    private final DelayedSupplier<FlowingFluid> stillDelayed = new DelayedSupplier<>();
    private final DelayedSupplier<FlowingFluid> flowingDelayed = new DelayedSupplier<>();
    /** Name to use for the tag, defaults to the fluid name */
    private String tagName;
    /** Fluid type, must be set to build */
    private Supplier<? extends FluidType> type;

    private Builder(String name) {
      this.name = name;
      this.tagName = name;
    }

    /* Fluid type */

    /** Registers the passed fluid type */
    public Builder type(Supplier<? extends FluidType> type) {
      if (this.type != null) {
        throw new IllegalStateException("Type already created for " + name);
      }
      this.type = fluidTypeRegister.register(name, type);
      return this;
    }

    /** Registers a fluid with the given properties, using the texture fluid type */
    public Builder type(FluidType.Properties properties) {
      return type(() -> new TextureFluidType(properties));
    }

    /** Registers a fluid with the given properties, using the texture fluid type */
    public Builder type() {
      return type(FluidType.Properties.create());
    }


    /* Bucket */

    /** Creates the bucket using the given supplier */
    public Builder bucket(Function<Supplier<? extends FlowingFluid>, Item> constructor) {
      if (this.bucket != null) {
        throw new IllegalStateException("Bucket already created for " + name);
      }
      return bucket(itemRegister.register(name + "_bucket", () -> constructor.apply(stillDelayed)));
    }

    /** Creates the default bucket */
    public Builder bucket() {
      return bucket(itemRegister.register(name + "_bucket", () -> new BucketItem(stillDelayed, ItemProperties.BUCKET_PROPS)));
    }


    /* Block */

    /** Creates the block form using the given supplier */
    public Builder block(Function<Supplier<? extends FlowingFluid>, LiquidBlock> constructor) {
      if (this.block != null) {
        throw new IllegalStateException("Block already created for " + name);
      }
      return block(blockRegister.register(name + "_fluid", () -> constructor.apply(stillDelayed)));
    }

    /** Creates the default block from the given material and light level */
    public Builder block(Material material, int lightLevel) {
      return block(blockRegister.register(name + "_fluid", () -> new LiquidBlock(stillDelayed, BlockBehaviour.Properties.of(material).lightLevel(state -> lightLevel).noCollission().strength(100.0F).noLootTable())));
    }

    /** Creates the default block from the given material */
    public Builder block(Material material) {
      return block(material, material == Material.LAVA ? 15 : 0);
    }


    /* Final fluid */

    /** Builds the final instance with the default constructors */
    public FluidObject<ForgeFlowingFluid> build() {
      return build(ForgeFlowingFluid.Source::new, ForgeFlowingFluid.Flowing::new);
    }

    /** Builds the final instance with the given constructors */
    public <F extends ForgeFlowingFluid> FluidObject<F> build(Function<Properties,? extends F> createStill, Function<Properties,? extends F> createFlowing) {
      if (type == null) {
        this.type();
      }

      // create props with the suppliers
      Properties props = build(type, stillDelayed, flowingDelayed);

      // create fluids now that we have props
      Supplier<F> still = registerFluid(name, () -> createStill.apply(props));
      stillDelayed.setSupplier(still);
      Supplier<F> flowing = registerFluid("flowing_" + name, () -> createFlowing.apply(props));
      flowingDelayed.setSupplier(flowing);

      // return the final nice object
      return new FluidObject<>(resource(name), tagName, type, still, flowing, this.block);
    }
  }
}
