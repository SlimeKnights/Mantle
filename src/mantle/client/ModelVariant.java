package mantle.client;

import java.util.ArrayList;

import mantle.items.util.IItemWithVariants;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    public String getResource(String resource)
    {
        return (MOD_ID + ":") + resource;
    }

    public void addVariantNames(Block block, String... names)
    {
        for (int i = 0; i < names.length; i++)
        {
            names[i] = getResource(names[i]);
        }

        ModelBakery.addVariantName(Item.getItemFromBlock(block), names);
    }

    public void registerBlockModel(Block block)
    {
        ResourceLocation resourceLocation = (ResourceLocation) Block.blockRegistry.getNameForObject(block);

        registerBlockModel(block, 0, resourceLocation.toString());
    }

    public void registerItemModel(Item item)
    {
        ResourceLocation resourceLocation = (ResourceLocation) Item.itemRegistry.getNameForObject(item);

        registerItemModel(item, 0, resourceLocation.toString());
    }

    public void registerBlockModel(Block block, int meta, String modelName)
    {
        registerItemModel(Item.getItemFromBlock(block), meta, modelName);
    }
    
    public void registerItemModelVariants(Item item)
    {
        for (int i = 0; i < ((IItemWithVariants) item).getVariantNames().length; i++)
        {
            String NAME = item.getUnlocalizedName().substring(5) + "_" + ((IItemWithVariants) item).getVariantNames()[i];
            ModelBakery.addVariantName(item, getResource(NAME));
            this.registerItemModel(item, i, getResource(NAME));
        }
    }
    
    public void registerItemSubTypesModel(Item item, CreativeTabs tab)
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        item.getSubItems(item, tab, list);
        for (ItemStack i : list)
        {
            this.registerItemModel(item, i.getItemDamage(), item.getUnlocalizedName().substring(5));
        }
    }

    public void registerItemModel(Item item, int meta, String resourcePath)
    {
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(resourcePath, "inventory");

        mc.getRenderItem().getItemModelMesher().register(item, meta, modelResourceLocation);
    }
}
