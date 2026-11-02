package slimeknights.mantle.loot.injection;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.injection.LootTableInjection.LootPoolInjection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Class handling injecting additional entries into loot tables */
public enum LootTableInjector implements BakeCallback<LootTableInjection> {
  INSTANCE;

  /** Registry key */
  public static final ResourceKey<Registry<LootTableInjection>> REGISTRY_KEY = ResourceKey.createRegistryKey(Mantle.getResource("loot_injectors"));
  /** Datapack folder for the injector */
  public static final String FOLDER = "mantle/loot_injectors";

  /** Initializes the loot table injector */
  public static void init(IEventBus modBus) {
    modBus.addListener(DataPackRegistryEvent.NewRegistry.class, INSTANCE::addRegistries);
    NeoForge.EVENT_BUS.addListener(LootTableLoadEvent.class, INSTANCE::lootTableLoad);
  }

  /** Map of injections to use on loot table load */
  private Map<ResourceLocation,LootTableInjection> injections = Collections.emptyMap();

  /**
   * Called to register the injector as a data registry
   * Needed to ensure it loads before loot tables which are a reloadable registry.
   */
  private void addRegistries(DataPackRegistryEvent.NewRegistry event) {
    event.dataPackRegistry(REGISTRY_KEY, LootTableInjection.CODEC, null, builder -> {
      builder.onBake(this);
    });
  }

  @Override
  public void onBake(Registry<LootTableInjection> registry) {
    long time = System.nanoTime();
    Map<ResourceLocation,LootTableInjection.Builder> builders = new HashMap<>();
    int loaded = 0;
    for (LootTableInjection injection : registry) {
      LootTableInjection.Builder builder = builders.computeIfAbsent(injection.name(), id -> new LootTableInjection.Builder());
      for (LootPoolInjection pool : injection.pools()) {
        builder.addToPool(pool);
      }
      loaded++;
    }
    // build final map
    injections = builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey()))
      .collect(Collectors.toUnmodifiableMap(LootTableInjection::name, Function.identity()));
    // log timer
    Mantle.logger.info("Loaded {} loot table injectors injecting into {} tables in {} ms", loaded, injections.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Called on loot table load to handle the actual injection */
  private void lootTableLoad(LootTableLoadEvent event) {
    LootTableInjection injection = injections.get(event.getName());
    if (injection != null) {
      Mantle.logger.debug("Injecting into {} pools in the table {}", injection.pools().size(), injection.name());
      LootTable table = event.getTable();
      for (LootPoolInjection pool : injection.pools()) {
        pool.inject(table);
      }
    }
  }
}
