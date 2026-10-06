package slimeknights.mantle.registration.deferred;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.block.StrippableLogBlock;
import slimeknights.mantle.item.burnable.BurnableBlockItem;
import slimeknights.mantle.item.burnable.BurnableHangingSignItem;
import slimeknights.mantle.item.burnable.BurnableSignItem;
import slimeknights.mantle.item.burnable.BurnableTallBlockItem;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register to handle registering blocks with possible item forms.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BlockDeferredRegister extends EnumDeferredRegister<Block> {
  /** Default item properties instance for the fallback */
  private static final Item.Properties DEFAULT_ITEM_PROPERTIES = new Item.Properties();
  /** Default block item using the default properties. */
  public final Function<Block,BlockItem> BLOCK_ITEM = (b) -> new BlockItem(b, DEFAULT_ITEM_PROPERTIES);

  private static final BlockBehaviour.Properties POTTED_PROPS = BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);

  protected final DeferredRegister<Item> itemRegister;
  @Setter @Getter
  protected Function<Block,BlockItem> defaultBlockItem = BLOCK_ITEM;

  public BlockDeferredRegister(String modID, DeferredRegister<Item> itemRegister) {
    super(Registries.BLOCK, modID);
    this.itemRegister = itemRegister;
  }

  public BlockDeferredRegister(String modID) {
    this(modID, DeferredRegister.create(Registries.ITEM, modID));
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    itemRegister.register(bus);
  }

  @Override
  protected <I extends Block> DeferredBlock<I> createHolder(ResourceKey<? extends Registry<Block>> registryKey, ResourceLocation key) {
    return DeferredBlock.createBlock(ResourceKey.create(registryKey, key));
  }


  /* Blocks with no items */

  /**
   * Registers a block with the block registry, giving it no item form.
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block registry object
   */
  public <B extends Block> DeferredHolder<Block,B> registerNoItem(String name, Function<ResourceLocation, ? extends B> block) {
    return super.register(name, block);
  }

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block registry object
   */
  public <B extends Block> DeferredHolder<Block,B> registerNoItem(String name, Supplier<? extends B> block) {
    return registerNoItem(name, id -> block.get());
  }

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param props  Block properties
   * @return  Block registry object
   */
  public DeferredHolder<Block,Block> registerNoItem(String name, BlockBehaviour.Properties props) {
    return registerNoItem(name, () -> new Block(props));
  }


  /* Standard blocks with items */

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name   Block ID
   * @param block  Block constructor
   * @param item   Function to create a BlockItem from a Block
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  @SuppressWarnings("unchecked")
  public <B extends Block> DeferredBlock<B> register(String name, Function<ResourceLocation, ? extends B> block, Function<? super B, ? extends BlockItem> item) {
    // need to leave super.register alone as the no-item one
    DeferredBlock<B> holder = (DeferredBlock<B>) registerNoItem(name, block);
    itemRegister.register(name, () -> item.apply(holder.get()));
    return holder;
  }

  /**
   * Registers a block with the block registry, using the default block item.
   * @param name   Block ID
   * @param block  Block constructor
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  @Override
  public <B extends Block> DeferredBlock<B> register(String name, Function<ResourceLocation, ? extends B> block) {
    return register(name, block, BLOCK_ITEM);
  }

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name   Block ID
   * @param block  Block supplier
   * @param item   Function to create a BlockItem from a Block
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> block, Function<? super B, ? extends BlockItem> item) {
    return register(name, id -> block.get(), item);
  }

  /**
   * Registers a block with the block registry, using the default block item.
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  @Override
  public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> block) {
    return register(name, block, BLOCK_ITEM);
  }

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name        Block ID
   * @param blockProps  Block properties
   * @param item        Function to create a BlockItem from a Block
   * @return  Block item registry object pair
   */
  public DeferredBlock<Block> register(String name, BlockBehaviour.Properties blockProps, Function<? super Block, ? extends BlockItem> item) {
    return register(name, () -> new Block(blockProps), item);
  }

  /**
   * Registers a block with the block registry, using the default block properties.
   * @param name        Block ID
   * @param blockProps  Block properties
   * @return  Block item registry object pair
   */
  public DeferredBlock<Block> register(String name, BlockBehaviour.Properties blockProps) {
    return register(name, blockProps, defaultBlockItem);
  }


  /* Building blocks */

  /**
   * Registers a building block with slabs and stairs, using a custom main block and block item.
   * @param name        Block name
   * @param properties  Properties for all blocks
   */
  public BuildingBlockBuilder registerBuilding(String name, BlockBehaviour.Properties properties) {
    return new BuildingBlockBuilder(name, properties);
  }

  /**
   * Registers a new wood object
   * @param name             Name of the wood object
   * @param behaviorCreator  Logic to create the behavior
   * @param flammable        If true, this wood type is flammable
   * @return Wood object
   */
  public WoodBlockObject registerWood(String name, Function<WoodVariant,BlockBehaviour.Properties> behaviorCreator, boolean flammable) {
    BlockSetType setType = new BlockSetType(resourceName(name));
    WoodType woodType = new WoodType(resourceName(name), setType);
    BlockSetType.register(setType);
    RegistrationHelper.registerWoodType(woodType);
    Item.Properties itemProps = new Item.Properties();

    // many of these are already burnable via tags, but simplier to set them all here
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableItem;
    Function<? super Block, ? extends BlockItem> burnableTallItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableSignItem;
    BiFunction<? super Block, ? super Block, ? extends BlockItem> burnableHangingSignItem;
    Item.Properties signProps = new Item.Properties().stacksTo(16);
    if (flammable) {
      burnableItem     = burnTime -> block -> new BurnableBlockItem(block, itemProps, burnTime);
      burnableTallItem = block -> new BurnableTallBlockItem(block, itemProps, 200);
      burnableSignItem = (standing, wall) -> new BurnableSignItem(signProps, standing, wall, 200);
      burnableHangingSignItem = (standing, wall) -> new BurnableHangingSignItem(signProps, standing, wall, 200);
    } else {
      Function<? super Block, ? extends BlockItem> defaultItemBlock = block -> new BlockItem(block, itemProps);
      burnableItem = burnTime -> defaultItemBlock;
      burnableTallItem = block -> new DoubleHighBlockItem(block, itemProps);
      burnableSignItem = (standing, wall) -> new SignItem(signProps, standing, wall);
      burnableHangingSignItem = (standing, wall) -> new HangingSignItem(standing, wall, signProps);
    }

    // planks
    Function<? super Block, ? extends BlockItem> burnable300 = burnableItem.apply(300);
    BlockBehaviour.Properties planksProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f);
    BuildingBlockObject planks = registerBuilding(name + "_planks", planksProps).item(block -> burnableItem.apply(block instanceof SlabBlock ? 150 : 300).apply(block)).build();
    BlockBehaviour.Properties fenceProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).forceSolidOn();
    DeferredBlock<FenceBlock> fence = register(name + "_fence", () -> new FenceBlock(fenceProps), burnable300);
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(2.0f));
    DeferredBlock<RotatedPillarBlock> strippedLog = register("stripped_" + name + "_log", stripped, burnable300);
    DeferredBlock<RotatedPillarBlock> strippedWood = register("stripped_" + name + "_wood", stripped, burnable300);
    DeferredBlock<RotatedPillarBlock> log = register(name + "_log", () -> new StrippableLogBlock(strippedLog, behaviorCreator.apply(WoodBlockObject.WoodVariant.LOG).instrument(NoteBlockInstrument.BASS).strength(2.0f)), burnable300);
    DeferredBlock<RotatedPillarBlock> wood = register(name + "_wood", () -> new StrippableLogBlock(strippedWood, behaviorCreator.apply(WoodBlockObject.WoodVariant.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0f)), burnable300);

    // doors
    DeferredBlock<DoorBlock> door = register(name + "_door", () -> new DoorBlock(setType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY)), burnableTallItem);
    DeferredBlock<TrapDoorBlock> trapdoor = register(name + "_trapdoor", () -> new TrapDoorBlock(setType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(net.minecraft.world.level.block.Blocks::never)), burnable300);
    DeferredBlock<FenceGateBlock> fenceGate = register(name + "_fence_gate", () -> new FenceGateBlock(woodType, fenceProps), burnable300);
    // redstone
    BlockBehaviour.Properties redstoneProps = behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().pushReaction(PushReaction.DESTROY).strength(0.5F);
    DeferredBlock<PressurePlateBlock> pressurePlate = register(name + "_pressure_plate", () -> new PressurePlateBlock(setType, redstoneProps), burnable300);
    DeferredBlock<ButtonBlock> button = register(name + "_button", () -> new ButtonBlock(setType, 30, redstoneProps), burnableItem.apply(100));
    // signs
    DeferredHolder<Block,StandingSignBlock> standingSign = registerNoItem(name + "_sign", () -> new StandingSignBlock(woodType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)));
    DeferredHolder<Block,WallSignBlock> wallSign = registerNoItem(name + "_wall_sign", () -> new WallSignBlock(woodType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F).lootFrom(standingSign)));
    DeferredHolder<Block,CeilingHangingSignBlock> hangingSign = registerNoItem(name + "_hanging_sign", () -> new CeilingHangingSignBlock(woodType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)));
    DeferredHolder<Block,WallHangingSignBlock> wallHangingSign = registerNoItem(name + "_wall_hanging_sign", () -> new WallHangingSignBlock(woodType, behaviorCreator.apply(WoodBlockObject.WoodVariant.PLANKS).instrument(NoteBlockInstrument.BASS).forceSolidOn().noCollission().strength(1.0F).lootFrom(hangingSign)));
    // tell mantle to inject these into the TE
    RegistrationHelper.registerSignBlock(standingSign);
    RegistrationHelper.registerSignBlock(wallSign);
    RegistrationHelper.registerHangingSignBlock(hangingSign);
    RegistrationHelper.registerHangingSignBlock(wallHangingSign);
    // sign is included automatically in asItem of the standing sign
    this.itemRegister.register(name + "_sign", () -> burnableSignItem.apply(standingSign.get(), wallSign.get()));
    this.itemRegister.register(name + "_hanging_sign", () -> burnableHangingSignItem.apply(hangingSign.get(), wallHangingSign.get()));
    // finally, return
    return new WoodBlockObject(resource(name), woodType,
                               planks, log, strippedLog, wood, strippedWood,
                               fence, fenceGate, door, trapdoor, pressurePlate, button,
                               standingSign, wallSign, hangingSign, wallHangingSign);
  }


  /* Metal */

  /** Starts a builder for registering a metal object */
  public MetalBuilder registerMetal(String name) {
    return new MetalBuilder(name);
  }


  /* Enum */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values      Enum values to use for this block
   * @param nameGetter  Function to get the block name.
   * @param mapper      Function to get a block for the given enum value
   * @param item        Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <E extends Enum<E>> EnumObject<E,Block> registerEnum(E[] values, Function<? super E,String> nameGetter, Function<E,? extends Block> mapper, Function<? super Block, ? extends BlockItem> item) {
    return EnumObject.generate(values, value -> register(nameGetter.apply(value), () -> mapper.apply(value), item));
  }

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values    Enum values to use for this block
   * @param name      Name of the block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <E extends Enum<E>> EnumObject<E,Block> registerEnum(E[] values, String name, Function<E,? extends Block> mapper, Function<? super Block, ? extends BlockItem> item) {
    return registerEnum(values, suffix(name), mapper, item);
  }

  /**
   * Registers a block with multiple variants, suffixing the name with the value name
   * @param name      Name of the block
   * @param values    Enum values to use for this block
   * @param mapper    Function to get a block for the given enum value
   * @param item      Function to get an item from the block
   * @return  EnumObject mapping between different block types
   */
  public <E extends Enum<E>> EnumObject<E,Block> registerEnum(String name, E[] values, Function<E,? extends Block> mapper, Function<? super Block, ? extends BlockItem> item) {
    return registerEnum(values, prefix(name), mapper, item);
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values      Enum value list.
   * @param nameGetter  Function to get the block name.
   * @param mapper      Function to map types to blocks.
   * @param <E>  Type of enum
   * @return  Enum object
   */
  public <E extends Enum<E>> EnumObject<E,Block> registerEnumNoItem(E[] values, Function<? super E,String> nameGetter, Function<E, ? extends Block> mapper) {
    return EnumObject.generate(values, value -> registerNoItem(nameGetter.apply(value), () -> mapper.apply(value)));
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values  Enum value list
   * @param name    Suffix after value name
   * @param mapper  Function to map types to blocks
   * @param <E>  Type of enum
   * @return  Enum object
   */
  public <E extends Enum<E>> EnumObject<E, Block> registerEnumNoItem(E[] values, String name, Function<E, ? extends Block> mapper) {
    return registerEnumNoItem(values, suffix(name), mapper);
  }

  /**
   * Registers a block with enum variants, but no item form
   * @param values  Enum value list
   * @param name    Suffix after value name
   * @param mapper  Function to map types to blocks
   * @param <E>  Type of enum
   * @return  Enum object
   */
  public <E extends Enum<E>> EnumObject<E, Block> registerEnumNoItem(String name, E[] values, Function<E, ? extends Block> mapper) {
    return registerEnumNoItem(values, prefix(name), mapper);
  }


  /* Flower pots */

  /**
   * Registers a potted form of the given block using the vanilla pot
   * @param name  Name of the flower
   * @param block Block to put in the block
   * @return  Potted block instance
   */
  public DeferredHolder<Block,FlowerPotBlock> registerPotted(String name, Supplier<? extends Block> block) {
    FlowerPotBlock flowerPot = (FlowerPotBlock) net.minecraft.world.level.block.Blocks.FLOWER_POT;
    DeferredHolder<Block,FlowerPotBlock> potted = registerNoItem("potted_" + name, () -> new FlowerPotBlock(() -> flowerPot, block, POTTED_PROPS));
    flowerPot.addPlant(resource(name), potted);
    return potted;
  }

  /** Registers a potted form of the given block using the vanilla pot */
  public DeferredHolder<Block,FlowerPotBlock> registerPotted(DeferredHolder<Block,? extends Block> block) {
    return registerPotted(block.getId().getPath(), block);
  }

  /** Registers a potted form of the given block using the vanilla pot with the given name getter. */
  public <E extends Enum<E> & StringRepresentable> EnumObject<E,Block> registerPottedEnum(E[] values, Function<? super E,String> nameGetter, EnumObject<E,Block> block) {
    return EnumObject.generate(values, value -> {
      Holder<Block> holder = block.getHolder(value);
      if (holder != null) {
        return registerPotted(nameGetter.apply(value), holder::value);
      }
      return null;
    });
  }

  /** Registers a potted form of the given block using the vanilla pot */
  public <E extends Enum<E> & StringRepresentable> EnumObject<E,Block> registerPottedEnum(E[] values, String name, EnumObject<E,Block> block) {
    return registerPottedEnum(values, suffix(name), block);
  }

  /** Registers a potted form of the given blocks using the vanilla pot, automatically choosing the values based on the passed object */
  public <T extends Enum<T> & StringRepresentable> EnumObject<T,Block> registerPottedEnum(String name, EnumObject<T,Block> block) {
    return registerPottedEnum(block.keys().iterator().next().getDeclaringClass().getEnumConstants(), name, block);
  }


  /* Builders */

  /**
   * Builder for creating a building block object.
   * Not used for wood, see {@link #registerWood(String, Function, boolean)}
   */
  @Accessors(fluent = true)
  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public class BuildingBlockBuilder {
    private final String name;
    /** Default block properties */
    private final BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties,Block> block = Block::new;
    private Function<BlockBehaviour.Properties,SlabBlock> slab = SlabBlock::new;
    private BiFunction<BlockState,BlockBehaviour.Properties,StairBlock> stairs = StairBlock::new;
    private Function<? super Block, ? extends BlockItem> item = defaultBlockItem;

    /** Registers the standard object */
    public BuildingBlockObject build() {
      DeferredBlock<Block> blockObj = register(name, () -> block.apply(properties), item);
      return new BuildingBlockObject(blockObj,
        register(name + "_slab", () -> slab.apply(properties), item),
        register(name + "_stairs", () -> stairs.apply(blockObj.get().defaultBlockState(), properties), item)
      );
    }

    /** Registers an object with a wall */
    public WallBuildingBlockObject wall(Function<BlockBehaviour.Properties,WallBlock> wall) {
      return new WallBuildingBlockObject(build(), register(name + "_wall", () -> wall.apply(properties), item));
    }

    /** Registers an object with a wall with the default constructor */
    public WallBuildingBlockObject wall() {
      return wall(WallBlock::new);
    }

    /** Registers an object with a fence */
    public FenceBuildingBlockObject fence(Function<BlockBehaviour.Properties,FenceBlock> fence) {
      return new FenceBuildingBlockObject(build(), register(name + "_fence", () -> fence.apply(properties), item));
    }

    /** Registers an object with a fence with the default constructor */
    public FenceBuildingBlockObject fence() {
      return fence(FenceBlock::new);
    }
  }

  /** Builder for creating a metal item object. */
  @Accessors(fluent = true)
  @CanIgnoreReturnValue
  @Setter
  public class MetalBuilder {
    /** Prefix for registry names */
    private final String name;
    /** Name of the common tag for this object. */
    private String tag;
    /** Constructor for the block item */
    private Function<Block,? extends BlockItem> blockItem = defaultBlockItem;
    /** Constructor for the ingot */
    private Supplier<Item> ingot;
    /** Constructor for the nugget */
    private Supplier<Item> nugget;

    protected MetalBuilder(String name) {
      this.name = name;
      this.tag = name;
      ingotNugget(new Item.Properties());
    }

    /** Sets the ingot supplier */
    public MetalBuilder ingot(Supplier<Item> ingot) {
      this.ingot = ingot;
      return this;
    }

    /** Sets the ingot properties */
    public MetalBuilder ingot(Item.Properties properties) {
      return ingot(() -> new Item(properties));
    }

    /** Sets the nugget supplier */
    public MetalBuilder nugget(Supplier<Item> nugget) {
      this.nugget = nugget;
      return this;
    }

    /** Sets the nugget properties */
    public MetalBuilder nugget(Item.Properties properties) {
      return nugget(() -> new Item(properties));
    }

    /** Sets the ingot and nugget */
    public MetalBuilder ingotNugget(Supplier<Item> supplier) {
      return ingot(supplier).nugget(supplier);
    }

    /** Sets the ingot and nugget */
    public MetalBuilder ingotNugget(Item.Properties properties) {
      return ingot(properties).nugget(properties);
    }

    /** Completes the builder with the block constructor */
    public MetalItemObject block(Supplier<Block> block) {
      return new MetalItemObject(tag,
        register(name + "_block", block, blockItem),
        itemRegister.register(name + "_ingot", ingot),
        itemRegister.register(name + "_nugget", nugget)
      );
    }

    /** Completes the builder with the block properties */
    public MetalItemObject block(BlockBehaviour.Properties properties) {
      return block(() -> new Block(properties));
    }
  }
}
