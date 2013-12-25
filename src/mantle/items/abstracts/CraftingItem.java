package mantle.items.abstracts;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CraftingItem extends Item
{
    public String modTexPrefix;
    public String[] textureNames;
    public String[] unlocalizedNames;
    public String folder;
    public IIcon[] icons;

    public CraftingItem(String[] names, String[] tex, String folder, String modTexturePrefix, CreativeTabs tab)
    {
        super();
        this.modTexPrefix = modTexturePrefix;
        this.setCreativeTab(tab);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.textureNames = tex;
        this.unlocalizedNames = names;
        this.folder = folder;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage (int meta)
    {
        int arr = MathHelper.clamp_int(meta, 0, unlocalizedNames.length);
        return icons[arr];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons (IIconRegister iconRegister)
    {
        this.icons = new IIcon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            if (!(textureNames[i].equals("")))
                this.icons[i] = iconRegister.registerIcon(modTexPrefix + ":" + folder + textureNames[i]);
        }
    }

    public String getUnlocalizedName (ItemStack stack)
    {
        int arr = MathHelper.clamp_int(stack.getItemDamage(), 0, unlocalizedNames.length);
        return getUnlocalizedName() + "." + unlocalizedNames[arr];
    }

    public void getSubItems (Block b, CreativeTabs tab, List list)
    {
        for (int i = 0; i < unlocalizedNames.length; i++)
            if (!(textureNames[i].equals("")))
                list.add(new ItemStack(b, 1, i));
    }

}
