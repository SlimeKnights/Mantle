package mantle.books.external;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class ZipLoader
{
    public BookData loadZip (File f)
    {
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
                    ZipFile zipfile = new ZipFile(f);
                    Enumeration e = zipfile.entries();
                    while (e.hasMoreElements())
                    {
                        ZipEntry entry = (ZipEntry) e.nextElement();
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
                            if (flExt.equalsIgnoreCase("bookconfig"))
                            {
                                Document config = ManualReader.readManual(zipfile.getInputStream(entry), flName);
                                NodeList nodes = config.getElementsByTagName("UnlocalizedName");
                                if (nodes != null)
                                    unlocName = nodes.item(0).getTextContent();
                                nodes = config.getElementsByTagName("Tooltip");
                                if (nodes != null)
                                    toolTip = nodes.item(0).getTextContent();
                                nodes = config.getElementsByTagName("Translatable");
                                if (nodes != null)
                                    translatable = StringUtils.isNullOrEmpty(nodes.item(0).getTextContent()) ? false : nodes.item(0).getTextContent().equalsIgnoreCase("true") ? true : false;
                                //if these 3 are null or empty or invalid then the book will use the mantle defaults
                                nodes = config.getElementsByTagName("BookIcon");
                                if (nodes != null)
                                    bIcon = nodes.item(0).getTextContent();
                                nodes = config.getElementsByTagName("LeftImage");
                                if (nodes != null)
                                    lImg = nodes.item(0).getTextContent();
                                nodes = config.getElementsByTagName("RightImage");
                                if (nodes != null)
                                    lImg = nodes.item(0).getTextContent();
                                if (isClient)
                                {
                                    nodes = config.getElementsByTagName("smallRecipe");
                                    if (nodes != null)
                                        registerSmallRecipes(nodes);
                                    nodes = config.getElementsByTagName("largeRecipe");
                                    if (nodes != null)
                                        registerLargeRecipes(nodes);
                                    nodes = config.getElementsByTagName("furnaceRecipe");
                                    if (nodes != null)
                                        registerFurnaceRecipes(nodes);
                                    nodes = config.getElementsByTagName("registerIcon");
                                    if (nodes != null)
                                        registerIcons(nodes);
                                }

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
                }
                return null;

            }
            else
            {
                logger.error("Attempted to load non-zip file for mantle book. File will be skipped");
            }
        }
        else
        {
            logger.error("Attempted to load non-existant file for mantle book. File will be skipped");

        }
        return null;
    }

    public boolean isImage (String ext)
    {
        if (ext.equalsIgnoreCase("png"))
            return true;
        return false;
    }

    //    public static void registerManualSmallRecipe (String name, ItemStack output, ItemStack... stacks)
    public void registerSmallRecipes (NodeList node)
    {
        String item[];
        String name = new String();
        ItemStack isOut;
        ItemStack[] isIn = new ItemStack[4];
        int meta = 0;
        int num = 0;
        for (int i = 0; i < node.getLength(); i++)
        {
            item = node.item(i).getTextContent().split("|");
            name = item[0];
            isOut = getISFromString(item[1]);
            for (int j = 2; j < 6; j++)
            {
                isIn[j - 2] = item[j].equalsIgnoreCase("null") ? null : getISFromString(item[j]);
            }
            MantleClientRegistry.registerManualSmallRecipe(name, isOut, isIn);
        }
    }

    //    public static void registerManualLargeRecipe (String name, ItemStack output, ItemStack... stacks)
    public void registerLargeRecipes (NodeList node)
    {
        String item[];
        String name = new String();
        ItemStack isOut;
        ItemStack[] isIn = new ItemStack[9];
        int meta = 0;
        int num = 0;
        for (int i = 0; i < node.getLength(); i++)
        {
            item = node.item(i).getTextContent().split("|");
            name = item[0];
            isOut = getISFromString(item[1]);
            for (int j = 2; j < 11; j++)
            {
                isIn[j - 2] = item[j].equalsIgnoreCase("null") ? null : getISFromString(item[j]);
            }
            MantleClientRegistry.registerManualLargeRecipe(name, isOut, isIn);
        }
    }

    //name, Out, in, 
    public void registerFurnaceRecipes (NodeList node)
    {
        String item[];
        String name = new String();
        ItemStack isOut;
        ItemStack isIn;
        int meta = 0;
        int num = 0;
        for (int i = 0; i < node.getLength(); i++)
        {
            item = node.item(i).getTextContent().split("|");
            name = item[0];
            isOut = getISFromString(item[1]);
            isIn = getISFromString(item[2]);
            MantleClientRegistry.registerManualFurnaceRecipe(name, isOut, isIn);
        }
    }

    //name, out
    public void registerIcons (NodeList node)
    {
        String item[];
        String name = new String();
        ItemStack is;
        int meta = 0;
        int num = 0;
        for (int i = 0; i < node.getLength(); i++)
        {
            item = node.item(i).getTextContent().split("|");
            name = item[0];
            is = getISFromString(item[1]);
            MantleClientRegistry.registerManualIcon(name, is);
        }
    }

    //TODO null protect this!!!
    public ItemStack getISFromString (String s)
    {
        try
        {
            String name = s.substring(0, s.indexOf("@") - 1);
            int meta = Integer.parseInt(s.substring(s.indexOf("@") + 1, s.contains("#") ? s.indexOf("#") - 1 : s.length() - 1));
            int stacksize = s.contains("#") ? Integer.parseInt(s.substring(s.indexOf("#") + 1)) : 1;
            String key = name + ":" + meta;
            Item i = (Item) Item.itemRegistry.getObject(key);
            Block b = (Block) Block.blockRegistry.getObject(key);
            if (i != null)
            {
                return new ItemStack(i, stacksize, meta);
            }
            if (b != null)
            {
                return new ItemStack(b, stacksize, meta);
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return null;
    }
}
