package mantle.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mantle.blocks.iface.IBlockVariant;

@SideOnly(Side.CLIENT)
public class ModelVariant
{
	private String modID;

	public ModelVariant(String modId)
	{
		this.modID = modId;
	}

	public void registerItemRenderer(Block block, IBlockVariant[] variants)
	{
		String[] names = new String[variants.length];

		for (int i = 0; i < variants.length; ++i)
		{
			IBlockVariant subtype = variants[i];
			names[i] = (this.modID + ":") + subtype.getName();

			this.registerItemRenderer(names[i], Item.getItemFromBlock(block), subtype.getMetadata());
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

	public void registerItemRenderer(String name, Block block, int meta)
	{
		this.registerItemRenderer(name, Item.getItemFromBlock(block), meta);
	}

	public static void registerItemRenderer(String name, Item item, int meta)
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, new ModelResourceLocation(name, "inventory"));
	}
}
