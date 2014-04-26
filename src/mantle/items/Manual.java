package mantle.items;

import net.minecraftforge.common.MinecraftForge;

import mantle.event.ManualOpenEvent;

import java.util.List;

import mantle.Mantle;
import mantle.books.BookDataStore;
import mantle.items.abstracts.CraftingItem;
import mantle.lib.CoreRepo;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Manual extends CraftingItem
{

    public final String modID;
    static String[] name = new String[] { "beginner", "toolstation", "smeltery", "diary" };
    static String[] textureName = new String[] { "tinkerbook_diary", "tinkerbook_toolstation", "tinkerbook_smeltery", "tinkerbook_blue" };

    public Manual()
    {
        super(name, textureName, "", "mantle", null);
        modID = CoreRepo.modId;
    }

    public void updateManual ()
    {
        updateData(new String[] { "test" }, new String[] { "mantlebook_blue" }, "", "mantle");
    }

    public Manual(String[] name, String[] textureName, String folder, String modTexturePrefix, CreativeTabs tab, String modID)
    {
        super(name, textureName, "", "mantle", tab);
        this.modID = modID;
    }

    @Override
    public ItemStack onItemRightClick (ItemStack stack, World world, EntityPlayer player)
    {
    	ManualOpenEvent.Pre preOpenEvent = new ManualOpenEvent.Pre(stack, player);
    	MinecraftForge.EVENT_BUS.post(preOpenEvent);

        player.openGui(Mantle.instance, mantle.client.MProxyClient.manualGuiID, world, 0, 0, 0);
        ManualOpenEvent.Post postOpenEvent = new ManualOpenEvent.Post(stack, player);
        MinecraftForge.EVENT_BUS.post(postOpenEvent);

        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation (ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        list.add("\u00a7o" + StatCollector.translateToLocal(BookDataStore.getBookfromID(stack.getItemDamage()).toolTip));
    }

    @Override
    public String getUnlocalizedName ()
    {
        return modID + ":" + super.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName (ItemStack par1ItemStack)
    {
        return super.getUnlocalizedName(par1ItemStack);
    }
}
