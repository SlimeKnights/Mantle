package slimeknights.mantle.client.book.repository;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FileRepository extends BookRepository {

  public final ResourceLocation location;

  public FileRepository(ResourceLocation location) {
    this.location = location;
  }

  @Override
  public List<SectionData> getSections() {
    return new ArrayList<>(Arrays.asList(BookLoader.getGson().fromJson(this.resourceToString(this.getResource(this.getResourceLocation("index.json"))), SectionData[].class)));
  }

  @Override
  public ResourceLocation getResourceLocation(@Nullable String path, boolean safe) {
    if (path == null) {
      return safe ? new ResourceLocation("") : null;
    }

    if (!path.contains(":")) {
      String langPath = null;

      //noinspection ConstantConditions - this was proven to be null once
      if (Minecraft.getInstance().getLanguageManager() != null && Minecraft.getInstance().getLanguageManager().getSelected() != null) {
        langPath = Minecraft.getInstance().getLanguageManager().getSelected();
      }

      String defaultLangPath = "en_us";

      ResourceLocation res;

      // TODO: this can be optimized if we return the resource instead of the location, how feasible is that in practice?
      //noinspection ConstantConditions - see above
      if (langPath != null) {
        res = new ResourceLocation(this.location + "/" + langPath + "/" + path);
        if (this.resourceExists(res)) {
          return res;
        }
      }
      res = new ResourceLocation(this.location + "/" + defaultLangPath + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
      res = new ResourceLocation(this.location + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
    } else {
      ResourceLocation res = new ResourceLocation(path);
      if (this.resourceExists(res)) {
        return res;
      }
    }

    return safe ? new ResourceLocation("") : null;
  }

  @Override
  public Optional<Resource> getLocation(@Nullable ResourceLocation loc) {
    if (loc == null) {
      return Optional.empty();
    }
    return Minecraft.getInstance().getResourceManager().getResource(loc);
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipComments) {
    if (resource == null) {
      return "";
    }

    try {
      Iterator<String> iterator = IOUtils.readLines(resource.open(), StandardCharsets.UTF_8).iterator();
      StringBuilder builder = new StringBuilder();

      boolean isLongComment = false;

      while (iterator.hasNext()) {
        String s = iterator.next().trim() + "\n";

        // Comment skipper
        if (skipComments) {
          if (isLongComment) {
            if (s.endsWith("*/")) {
              isLongComment = false;
            }
            continue;
          } else {
            if (s.startsWith("/*")) {
              isLongComment = true;
              continue;
            }
          }
          if (s.startsWith("//")) {
            continue;
          }
        }

        builder.append(s);
      }

      return builder.toString().trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }
}
