package mantle.client;

import java.util.Collection;
import java.util.Iterator;

import mantle.blocks.util.BlockVariant;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelVariant
{
	private String modID;

	public ModelVariant(String modId)
	{
		this.modID = modId;
	}

	public void registerItemRenderer(Block block, Collection<BlockVariant> variants)
	{
		String[] names = new String[variants.size()];

		Iterator<BlockVariant> iterator = variants.iterator();
		for (int i = 0; iterator.hasNext(); i++)
		{
			BlockVariant variant = iterator.next();
			names[i] = (this.modID + ":") + variant.getName();

			this.registerItemRenderer(names[i], Item.getItemFromBlock(block), variant.getMeta());
		}

		ModelBakery.addVariantName(Item.getItemFromBlock(block), names);
	}

	public void registerItemRenderer(Block block, int meta)
	{
		this.registerItemRenderer(Item.getItemFromBlock(block), meta);
	}

	public void registerItemRenderer(Item item, int meta)
	{
		String name = (this.modID + ":") + item.getUnlocalizedName().substring(5);

		this.registerItemRenderer(name, item, meta);
	}

	public void registerItemRenderers(int meta, Item... items)
	{
		for (Item item : items)
		{
			this.registerItemRenderer(item, meta);
		}
	}

	public void registerItemRenderer(String name, Block block, int meta)
	{
		this.registerItemRenderer(name, Item.getItemFromBlock(block), meta);
	}

	public void registerItemRenderer(String name, Item item, int meta)
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, new ModelResourceLocation(name, "inventory"));
	}
}
