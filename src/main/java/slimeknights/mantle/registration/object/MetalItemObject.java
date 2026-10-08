package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

import static slimeknights.mantle.Mantle.commonResource;

/** Object wrapper containing ingots, nuggets, and blocks */
public class MetalItemObject extends ItemObject<Block,Block> implements MultiObject<ItemLike> {
  private final Holder<Item> ingotHolder;
  private final Holder<Item> nuggetHolder;
  @Getter
  private final BlockItemTagKey blockTag;
  @Getter
  private final TagKey<Item> ingotTag;
  @Getter
  private final TagKey<Item> nuggetTag;

  public MetalItemObject(String tagName, DeferredHolder<Block,? extends Block> block, Holder<Item> ingot, Holder<Item> nugget) {
    super(block);
    this.ingotHolder = ingot;
    this.nuggetHolder = nugget;
    this.blockTag = BlockItemTagKey.create(commonResource("storage_blocks/" + tagName));
    this.ingotTag = getTag("ingots/" + tagName);
    this.nuggetTag = getTag("nuggets/" + tagName);
  }

  /** Gets the ingot for this object */
  public Item getIngot() {
    return ingotHolder.value();
  }

  /** Gets the ingot for this object */
  public Item getNugget() {
    return nuggetHolder.value();
  }

  /**
   * Creates a tag for a resource
   * @param name  Tag name
   * @return  Tag
   */
  private static TagKey<Item> getTag(String name) {
    return ItemTags.create(commonResource(name));
  }

  @Override
  public List<ItemLike> values() {
    return List.of(get(), getIngot(), getIngot());
  }
}
