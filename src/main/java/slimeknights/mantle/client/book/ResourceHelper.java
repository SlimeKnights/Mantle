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

@SideOnly(Side.CLIENT)
public class ResourceHelper {

  private static String bookRoot;

  private ResourceHelper() {
  }

  public static ResourceLocation getResourceLocation(String path) {
    return getResourceLocation(path, false);
  }

  public static ResourceLocation getResourceLocation(String path, boolean safe) {
    if (path == null)
      return safe ? new ResourceLocation("") : null;
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
      return safe ? new ResourceLocation("") : null;
    } else {
      ResourceLocation res = new ResourceLocation(path);
      if (resourceExists(res))
        return res;
      return safe ? new ResourceLocation("") : null;
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
        String s = ((String) iterator.next()).trim() + "\n";

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

      return builder.toString().trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  public static void setRoot(String location) {
    bookRoot = location;
  }
}
