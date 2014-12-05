package mantle.items.abstracts;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CraftingItem extends Item
{
    public String modTexPrefix;
    public String[] textureNames;
    public String[] unlocalizedNames;
    public String folder;

    public CraftingItem(String[] names, String[] tex, String folder, String modTexturePrefix, CreativeTabs tab)
    {
        super();
        this.modTexPrefix = modTexturePrefix;
        if (tab != null)
        {
            this.setCreativeTab(tab);
        }
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.textureNames = tex;
        this.unlocalizedNames = names;
        this.folder = folder;
    }

    public void updateData (String[] names, String[] tex, String folder, String modTexturePrefix)
    {
        this.modTexPrefix = modTexturePrefix;
        this.textureNames = tex;
        this.unlocalizedNames = names;
        this.folder = folder;
    }

    public String getUnlocalizedName (ItemStack stack)
    {
        int arr = MathHelper.clamp_int(stack.getItemDamage(), 0, unlocalizedNames.length);
        return getUnlocalizedName() + "." + unlocalizedNames[arr];
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public void getSubItems (Item b, CreativeTabs tab, List list)
    {
        for (int i = 0; i < unlocalizedNames.length; i++)
            if (!(textureNames[i].equals("")))
                list.add(new ItemStack(b, 1, i));
    }

}
