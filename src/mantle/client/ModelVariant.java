package mantle.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelVariant
{
    private String MOD_ID;

    private Minecraft mc;

    public ModelVariant(String modId, Minecraft mc)
    {
        this.MOD_ID = modId;
        this.mc = mc;
    }

    private void registerBlockModel(Block block)
    {
        ResourceLocation resourceLocation = (ResourceLocation) Block.blockRegistry.getNameForObject(block);

        registerBlockModel(block, 0, resourceLocation.getResourcePath());
    }

    private void registerItemModel(Item item)
    {
        ResourceLocation resourceLocation = (ResourceLocation) Item.itemRegistry.getNameForObject(item);

        registerItemModel(item, 0, resourceLocation.getResourcePath());
    }

    private void registerBlockModel(Block block, int meta, String modelName)
    {
        registerItemModel(Item.getItemFromBlock(block), meta, modelName);
    }

    private void registerItemModel(Item item, int meta, String resourcePath)
    {
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation((this.MOD_ID + ":") + resourcePath, "inventory");

        this.mc.getRenderItem().getItemModelMesher().register(item, meta, modelResourceLocation);
    }

    private void registerBlockModelVariant(Block block, int meta, String resourcePath)
    {
        Item item = Item.getItemFromBlock(block);

        registerItemModel(item, meta, resourcePath);

        ModelBakery.addVariantName(item, (this.MOD_ID + ":") + resourcePath);
    }
}
