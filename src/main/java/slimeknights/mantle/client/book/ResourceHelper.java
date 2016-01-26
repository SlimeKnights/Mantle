package slimeknights.mantle.client.book;

import java.io.IOException;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class ResourceHelper {

  private static String bookRoot;

  private ResourceHelper() {
  }

  public static ResourceLocation getResourceLocation(String path) {
    if (path == null)
      return null;
    if (!path.contains(":")) {
      String langPath = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
      String defaultLangPath = "en_US";

      ResourceLocation res = new ResourceLocation(bookRoot + "/" + langPath + "/" + path);
      if (resourceExists(res))
        return res;
      res = new ResourceLocation(bookRoot + "/" + defaultLangPath + "/" + path);
      if (resourceExists(res))
        return res;
      res = new ResourceLocation(bookRoot + "/" + path);
      if (resourceExists(res))
        return res;
      return null;
    } else {
      ResourceLocation res = new ResourceLocation(path);
      if (resourceExists(res))
        return res;
      return null;
    }
  }

  public static IResource getResource(ResourceLocation loc) {
    if (loc == null)
      return null;
    try {
      return Minecraft.getMinecraft().getResourceManager().getResource(loc);
    } catch (IOException e) {
      return null;
    }
  }

  public static boolean resourceExists(String location) {
    return resourceExists(new ResourceLocation(location));
  }

  public static boolean resourceExists(ResourceLocation location) {
    if (location == null)
      return false;
    try {
      Minecraft.getMinecraft().getResourceManager().getResource(location);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static String resourceToString(IResource resource) {
    return resourceToString(resource, true);
  }

  public static String resourceToString(IResource resource, boolean skipCommments) {
    if (resource == null)
      return "";
    try {
      Iterator iterator = IOUtils.readLines(resource.getInputStream(), Charsets.UTF_8).iterator();
      StringBuilder builder = new StringBuilder();

      boolean isLongComment = false;

      while (iterator.hasNext()) {
        String s = ((String) iterator.next()).trim();

        // Comment skipper
        if (skipCommments) {
          if (isLongComment) {
            if (s.endsWith("*/"))
              isLongComment = false;
            continue;
          } else {
            if (s.startsWith("/*")) {
              isLongComment = true;
              continue;
            }
          }
          if (s.startsWith("//"))
            continue;
        }

        builder.append(s);
      }

      String data = builder.toString().trim();

      return data;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  public static void setBookRoot(String location) {
    bookRoot = location;
  }
}
