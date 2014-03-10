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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
                                //TODO 1.7.2 add in ability to register stuff w/ client registry via this file
                            }

                        }

                    }
                    zipfile.close();
                    b.unlocalizedName = unlocName;
                    b.toolTip = toolTip;
                    b.isTranslatable = translatable;
                    b.leftImage = new ResourceLocation("mantle", "textures/gui/bookleft.png");
                    b.rightImage = new ResourceLocation("mantle", "textures/gui/bookright.png");
                    b.itemImage = new ResourceLocation("mantle", "textures/items/mantlebook_blue.png");
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
}
