package slimeknights.mantle.loot.injection;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericRegistryDataProvider;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Data provider for adding new loot table injections */
@SuppressWarnings("unused") // API
public abstract class AbstractLootTableInjectionProvider extends GenericRegistryDataProvider {
  private final List<Builder> builders = new ArrayList<>();
  private final String domain;

  public AbstractLootTableInjectionProvider(PackOutput output, String domain, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, LootTableInjector.FOLDER, registries);
    this.domain = domain;
  }

  /** Method to add all relevant tables */
  protected abstract void addTables(HolderLookup.Provider lookup);

  @Override
  public final CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider lookup) {
    addTables(lookup);
    TypedMap context = ContextKey.registryContext(lookup);
    // add all builders to the output
    return allOf(builders.stream().map(builder -> {
      JsonObject json = LootTableInjection.LOADABLE.serialize(builder.build(), context).getAsJsonObject();
      if (builder.conditions.length > 0) {
        json.add("conditions", JsonHelper.serializeArray(ICondition.CODEC, builder.conditions));
      }
      return saveJson(output, ResourceLocation.fromNamespaceAndPath(domain, builder.path), json);
    }));
  }

  /** Creates a new injection */
  protected LootTableInjection.Builder inject(String path, ResourceLocation name, ICondition... conditions) {
    LootTableInjection.Builder builder = new LootTableInjection.Builder();
    builders.add(new Builder(path, name, builder, conditions));
    return builder;
  }

  /** Creates a new injection for the Minecraft domain */
  protected LootTableInjection.Builder inject(String path, String name, ICondition... conditions) {
    return inject(path, ResourceLocation.withDefaultNamespace(name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  protected LootTableInjection.Builder injectChest(String name, ICondition... conditions) {
    return inject(name, ResourceLocation.withDefaultNamespace("chests/" + name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  protected LootTableInjection.Builder injectGameplay(String name, ICondition... conditions) {
    return inject(name, ResourceLocation.withDefaultNamespace("gameplay/" + name), conditions);
  }

  /** Internal builder tuple */
  private record Builder(String path, ResourceLocation name, LootTableInjection.Builder builder, ICondition[] conditions) {
    public LootTableInjection build() {
      return builder.build(name);
    }
  }
}
