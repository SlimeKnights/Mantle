package slimeknights.mantle.loot;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Data provider for adding new loot table injections */
public abstract class AbstractLootTableInjectionProvider extends GenericDataProvider {
  private final List<Builder> builders = new ArrayList<>();
  private final String domain;

  public AbstractLootTableInjectionProvider(PackOutput output, String domain) {
    super(output, Target.DATA_PACK, LootTableInjector.FOLDER);
    this.domain = domain;
  }

  /** Method to add all relevant tables */
  protected abstract void addTables();

  @Override
  public final CompletableFuture<?> run(CachedOutput output) {
    addTables();
    // add all builders to the output
    return allOf(builders.stream().map(builder -> {
      JsonObject json = LootTableInjection.LOADABLE.serialize(builder.build()).getAsJsonObject();
      if (builder.conditions.length > 0) {
        json.add("conditions", CraftingHelper.serialize(builder.conditions));
      }
      return saveJson(output, new ResourceLocation(domain, builder.path), json);
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
    return inject(path, new ResourceLocation(name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  protected LootTableInjection.Builder injectChest(String name, ICondition... conditions) {
    return inject(name, new ResourceLocation("chests/" + name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  protected LootTableInjection.Builder injectGameplay(String name, ICondition... conditions) {
    return inject(name, new ResourceLocation("gameplay/" + name), conditions);
  }

  /** Internal builder tuple */
  private record Builder(String path, ResourceLocation name, LootTableInjection.Builder builder, ICondition[] conditions) {
    public LootTableInjection build() {
      return builder.build(name);
    }
  }
}
