package slimeknights.mantle.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Tag provider for all mantle tags, doubles as an item tag provider without need for a block tag provider
 */
public class MantleItemTagProvider extends TagsProvider<Item> {
  protected MantleItemTagProvider(DataGenerator dataGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(dataGenerator, Registry.ITEM, modId, existingFileHelper);
  }

  public MantleItemTagProvider(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper) {
    this(dataGenerator, Mantle.modId, existingFileHelper);
  }

  @Override
  public String getName() {
    return "Mantle Item Tags";
  }

  @Override
  protected void addTags() {
    this.tag(MantleTags.Items.OFFHAND_COOLDOWN);
  }

  @Override
  protected Path getPath(ResourceLocation id) {
    return this.generator.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/items/" + id.getPath() + ".json");
  }
}
