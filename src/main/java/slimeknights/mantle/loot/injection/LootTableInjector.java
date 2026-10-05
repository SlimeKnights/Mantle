package slimeknights.mantle.loot.injection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.IEarlyReloadListener;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.loot.injection.LootTableInjection.LootPoolInjection;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Class handling injecting additional entries into loot tables */
public enum LootTableInjector implements IEarlyReloadListener {
  INSTANCE;

  /** Datapack folder for the injector */
  public static final String FOLDER = "mantle/loot_injectors";

  /** Initializes the loot table injector */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, INSTANCE::addReloadListeners);
    NeoForge.EVENT_BUS.addListener(LootTableLoadEvent.class, INSTANCE::lootTableLoad);
  }

  /** Registry access for loot table stuff */
  private RegistryAccess registry = RegistryAccess.EMPTY;
  /** Condition context for preventing load */
  private IContext context = IContext.EMPTY;
  /** Map of injections to use on loot table load */
  private Map<ResourceLocation,LootTableInjection> injections = Collections.emptyMap();

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    long time = System.nanoTime();
    Map<ResourceLocation,LootTableInjection.Builder> builders = new HashMap<>();
    TypedMap context = TypedMapBuilder.builder().put(ContextKey.REGISTRY_LOOKUP, registry).put(ContextKey.CONDITION_CONTEXT, this.context).build();
    int loaded = 0;
    for (Entry<ResourceLocation,Resource> entry : manager.listResources(FOLDER, loc -> loc.getPath().endsWith(".json")).entrySet()) {
      try (Reader reader = entry.getValue().openAsReader()) {
        JsonObject json = GsonHelper.fromJson(JsonHelper.DEFAULT_GSON, reader, JsonObject.class);
        // skip if empty for easy removals
        if (!json.keySet().isEmpty() && JsonHelper.processConditions(json, "conditions", this.context)) {
          // the builder allows us to merge from multiple sources, for efficiency
          // ensures a given table name and pool name both show just once
          LootTableInjection injection = LootTableInjection.LOADABLE.deserialize(json, context);
          LootTableInjection.Builder builder = builders.computeIfAbsent(injection.name(), id -> new LootTableInjection.Builder());
          for (LootPoolInjection pool : injection.pools()) {
            builder.addToPool(pool);
          }
          loaded++;
        }
      } catch (IllegalArgumentException | IOException | JsonParseException ex) {
        Mantle.logger.error("Couldn't parse loot injection from {}", entry.getKey(), ex);
      }
    }
    // build final map
    injections = builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey()))
                         .collect(Collectors.toUnmodifiableMap(LootTableInjection::name, Function.identity()));
    // log timer
    Mantle.logger.info("Loaded {} loot table injectors injecting into {} tables in {} ms", loaded, injections.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Called on world load to register the reload listeners and fetch parsing contexts */
  private void addReloadListeners(AddReloadListenerEvent event) {
    event.addListener(this);
    registry = event.getRegistryAccess();
    context = event.getConditionContext();
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
