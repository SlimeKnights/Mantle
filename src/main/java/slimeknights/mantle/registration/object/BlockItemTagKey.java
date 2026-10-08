package slimeknights.mantle.registration.object;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Object wrapping tag keys for a block that is {@link net.minecraft.data.tags.ItemTagsProvider#copy(TagKey, TagKey) copied} to an item tag. */
public record BlockItemTagKey(TagKey<Block> block, TagKey<Item> item) {
  /** Creates both tags at once */
  public static BlockItemTagKey create(ResourceLocation id) {
    return new BlockItemTagKey(BlockTags.create(id), ItemTags.create(id));
  }
}
