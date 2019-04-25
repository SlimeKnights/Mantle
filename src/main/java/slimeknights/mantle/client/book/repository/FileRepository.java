package slimeknights.mantle.client.book.repository;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;

public class FileRepository extends BookRepository {

  public final String location;

  public FileRepository(String location) {
    this.location = location;
  }

  @Override
  public List<SectionData> getSections() {
    return new ArrayList<>(Arrays.asList(BookLoader.GSON
                                             .fromJson(resourceToString(getResource(getResourceLocation("index.json"))), SectionData[].class)));
  }

  @Override
  public ResourceLocation getResourceLocation(String path, boolean safe) {
    if(path == null) {
      return safe ? new ResourceLocation("") : null;
    }
    if(!path.contains(":")) {
      String langPath = null;

      if(Minecraft.getInstance().getLanguageManager() != null && Minecraft.getInstance().getLanguageManager().getCurrentLanguage() != null)
      {
        langPath = Minecraft.getInstance().getLanguageManager().getCurrentLanguage().getLanguageCode();
      }

      String defaultLangPath = "en_US";

      ResourceLocation res;

      if(langPath != null) {
        res = new ResourceLocation(location + "/" + langPath + "/" + path);
        if (resourceExists(res)) {
          return res;
        }
      }
      res = new ResourceLocation(location + "/" + defaultLangPath + "/" + path);
      if(resourceExists(res)) {
        return res;
      }
      res = new ResourceLocation(location + "/" + path);
      if(resourceExists(res)) {
        return res;
      }
      return safe ? new ResourceLocation("") : null;
    } else {
      ResourceLocation res = new ResourceLocation(path);
      if(resourceExists(res)) {
        return res;
      }
      return safe ? new ResourceLocation("") : null;
    }
  }

  @Override
  public IResource getResource(ResourceLocation loc) {
    if(loc == null) {
      return null;
    }
    try {
      return Minecraft.getInstance().getResourceManager().getResource(loc);
    } catch(IOException e) {
      return null;
    }
  }

  @Override
  public boolean resourceExists(ResourceLocation location) {
    if(location == null) {
      return false;
    }
    try {
      Minecraft.getInstance().getResourceManager().getResource(location);
      return true;
    } catch(IOException e) {
      return false;
    }
  }

  @Override
  public String resourceToString(IResource resource, boolean skipCommments) {
    if(resource == null) {
      return "";
    }
    try {
      Iterator iterator = IOUtils.readLines(resource.getInputStream(), Charsets.UTF_8).iterator();
      StringBuilder builder = new StringBuilder();

      boolean isLongComment = false;

      while(iterator.hasNext()) {
        String s = ((String) iterator.next()).trim() + "\n";

        // Comment skipper
        if(skipCommments) {
          if(isLongComment) {
            if(s.endsWith("*/")) {
              isLongComment = false;
            }
            continue;
          } else {
            if(s.startsWith("/*")) {
              isLongComment = true;
              continue;
            }
          }
          if(s.startsWith("//")) {
            continue;
          }
        }

        builder.append(s);
      }

      return builder.toString().trim();
    } catch(IOException e) {
      e.printStackTrace();
    }

    return "";
  }
}
