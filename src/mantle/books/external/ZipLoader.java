package mantle.books.external;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import static mantle.lib.CoreRepo.logger;
import mantle.books.BookData;
import mantle.books.BookDataStore;
import mantle.books.ManualReader;
import mantle.books.client.BookImage;
import mantle.lib.client.MantleClientRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ZipLoader
{
    public static Map<String, ItemStack> loadedIS = Maps.newHashMap();

    public static BookData loadZip (File f)
    {
        BookLoad bl;
        String flName = new String();
        String flExt = new String();
        String lImg = new String();
        String rImg = new String();
        String bIcon = new String();
        String unlocName = new String();
        String toolTip = new String();
        Boolean translatable = false;
        BookData b = new BookData();
        boolean isClient = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
        if (f.exists())
        {
            if (FilenameUtils.getExtension(f.getPath()).equalsIgnoreCase("zip"))
            {
                try
                {
                    logger.info("Loading book zip " + f.getName());
                    ZipFile zipfile = new ZipFile(f);
                    Enumeration<? extends ZipEntry> e = zipfile.entries();
                    while (e.hasMoreElements())
                    {
                        ZipEntry entry = e.nextElement();
                        flName = entry.getName();
                        flExt = FilenameUtils.getExtension(flName);
                        if (!entry.isDirectory())
                        {
                            if (isClient && isImage(flExt))
                            {
                                String id = FilenameUtils.getBaseName(flName);
                                BufferedImage img = ImageIO.read(zipfile.getInputStream(entry));
                                DynamicTexture dynTexture = new DynamicTexture(img);
                                ResourceLocation rsLoc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(id, dynTexture);
                                BookImage bI = new BookImage(img.getWidth(), img.getHeight(), dynTexture, rsLoc);
                                MantleClientRegistry.imageCache.put(id, bI);
                            }
                            if (flExt.equalsIgnoreCase("xml"))
                            {
                                b.doc = ManualReader.readManual(zipfile.getInputStream(entry), flName);
                            }
                            if (flExt.equalsIgnoreCase("lang"))
                            {
                                //TODO 1.7 put this data into the vanilla lang stuffs
                            }
                            if (flExt.equalsIgnoreCase("json"))
                            {
                                Gson g = new Gson();
                                bl = g.fromJson(IOUtils.toString(zipfile.getInputStream(entry), Charsets.UTF_8), BookLoad.class);
                                for (BookIS bis : bl.registerItemStacks)
                                {
                                    try
                                    {
                                        ItemStack is = loadedIS.get(bis.cname);
                                        if (is == null)
                                        {
                                            Block blok = (Block) Block.blockRegistry.getObject(bis.cname);
                                            Item it = (Item) Item.itemRegistry.getObject(bis.cname);
                                            if (it != null)
                                            {
                                                is = new ItemStack(it, bis.getStackSize(), bis.metadata);
                                            }

                                            else if (bl != null)
                                            {
                                                is = new ItemStack(blok, bis.getStackSize(), bis.metadata);
                                            }
                                            if (is != null)
                                            {
                                                if (bis.tags != null && !bis.tags.isEmpty())
                                                {
                                                    try
                                                    {
                                                        is.stackTagCompound = (NBTTagCompound) JsonToNBT.func_150315_a(bis.tags);
                                                    }
                                                    catch (Exception e1)
                                                    {
                                                    }
                                                }
                                                loadedIS.put(bis.cname, is);
                                            }
                                        }
                                    }
                                    catch (Exception e1)
                                    {
                                    }
                                }
                                if (isClient)
                                {
                                    for (JsonObject jso : bl.smallRecipes)
                                    {
                                        if (jso != null)
                                        {
                                            ItemStack out = loadedIS.get(jso.get("out").getAsString());
                                            ItemStack in[] = getISArray(jso.getAsJsonArray("in"));
                                            String name = jso.get("name").getAsString();
                                            if (out != null && in != null && in.length <= 4 && name != null && !name.isEmpty())
                                            {
                                                MantleClientRegistry.registerManualSmallRecipe(name, out, in);
                                            }
                                        }
                                    }
                                    for (JsonObject jso : bl.largeRecipes)
                                    {
                                        if (jso != null)
                                        {
                                            ItemStack out = loadedIS.get(jso.get("out").getAsString());
                                            ItemStack in[] = getISArray(jso.getAsJsonArray("in"));
                                            String name = jso.get("name").getAsString();
                                            if (out != null && in != null && in.length <= 9 && name != null && !name.isEmpty())
                                            {
                                                MantleClientRegistry.registerManualLargeRecipe(name, out, in);
                                            }
                                        }
                                    }
                                    for (JsonObject jso : bl.furnaceRecipes)
                                    {
                                        if (jso != null)
                                        {
                                            ItemStack out = loadedIS.get(jso.get("out").getAsString());
                                            ItemStack in = loadedIS.get(jso.get("in").getAsString());
                                            String name = jso.get("name").getAsString();
                                            if (out != null && in != null && name != null && !name.isEmpty())
                                            {
                                                MantleClientRegistry.registerManualFurnaceRecipe(name, out, in);
                                            }
                                        }
                                    }
                                    for (JsonObject jso : bl.manualIcons)
                                    {
                                        if (jso != null)
                                        {
                                            ItemStack in = loadedIS.get(jso.get("stack").getAsString());
                                            String name = jso.get("name").getAsString();
                                            if (in != null && name != null && !name.isEmpty())
                                            {
                                                MantleClientRegistry.registerManualIcon(name, in);
                                            }
                                        }
                                    }
                                }
                                b.unlocalizedName = bl.unlocalizedName;
                                b.toolTip = bl.tooltip;
                                b.isTranslatable = bl.translatable;
                                b.leftImage = bl.LeftImage == null ? new ResourceLocation("mantle", "textures/gui/bookleft.png") : MantleClientRegistry.getBookImageFromCache(bl.LeftImage).resource;
                                b.rightImage = bl.rightImage == null ? new ResourceLocation("mantle", "textures/gui/bookright.png")
                                        : MantleClientRegistry.getBookImageFromCache(bl.rightImage).resource;
                                b.itemImage = bl.BookIcon == null ? new ResourceLocation("mantle", "textures/items/mantlebook_blue.png")
                                        : MantleClientRegistry.getBookImageFromCache(bl.BookIcon).resource;
                                b.isFromZip = true;
                                BookDataStore.addBook(b);
                            }
                        }

                    }
                    zipfile.close();
                    b.unlocalizedName = unlocName;
                    b.toolTip = toolTip;
                    b.isTranslatable = translatable;
                    b.leftImage = lImg == null ? new ResourceLocation("mantle", "textures/gui/bookleft.png") : MantleClientRegistry.getBookImageFromCache(lImg).resource;
                    b.rightImage = rImg == null ? new ResourceLocation("mantle", "textures/gui/bookright.png") : MantleClientRegistry.getBookImageFromCache(rImg).resource;
                    b.itemImage = bIcon == null ? new ResourceLocation("mantle", "textures/items/mantlebook_blue.png") : MantleClientRegistry.getBookImageFromCache(bIcon).resource;
                    b.isFromZip = true;
                    BookDataStore.addBook(b);
                }
                catch (Exception e)
                {
                    logger.info(" Error Loading book zip " + f.getName());
                }
                return null;

            }
            else
            {
                logger.error("Attempted to load non-zip file for mantle book. File " + f.getName() + " will be skipped");
            }
        }
        else
        {
            logger.error("Attempted to load non-existant file for mantle book. File will be skipped");

        }
        return null;
    }

    public static boolean isImage (String ext)
    {
        if (ext.equalsIgnoreCase("png"))
            return true;
        return false;
    }

    public static ItemStack[] getISArray (JsonArray a)
    {
        ItemStack[] is = new ItemStack[a.size()];
        ItemStack loadedStack;
        for (int i = 0; i < a.size(); i++)
        {
            if (a.get(i).getAsString() != null && !a.get(i).getAsString().isEmpty() && a.get(i).getAsString().equals("none"))
            {
                is[i] = null;
            }
            else
            {
                loadedStack = loadedIS.get(a.get(i).getAsString());
                if (loadedStack == null)
                {
                    is = null;
                    return is;
                }
                is[i] = loadedStack;
            }
        }
        return is;
    }
}
