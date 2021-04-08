package slimeknights.mantle.client.book.repository;

import org.apache.commons.io.IOUtils;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FileRepository extends BookRepository {

  public final String location;

  public FileRepository(String location) {
    this.location = location;
  }

  @Override
  public List<SectionData> getSections() {
    return new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(this.resourceToString(this.getResource(this.getResourceLocation("index.json"))), SectionData[].class)));
  }

  @Override
  public Identifier getResourceLocation(@Nullable String path, boolean safe) {
    if (path == null) {
      return safe ? new Identifier("") : null;
    }
    if (!path.contains(":")) {
      String langPath = null;

      //noinspection ConstantConditions - this was proven to be null once
      if (MinecraftClient.getInstance().getLanguageManager() != null && MinecraftClient.getInstance().getLanguageManager().getLanguage() != null) {
        langPath = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
      }

      String defaultLangPath = "en_us";

      Identifier res;

      //noinspection ConstantConditions - see above
      if (langPath != null) {
        res = new Identifier(this.location + "/" + langPath + "/" + path);
        if (this.resourceExists(res)) {
          return res;
        }
      }
      res = new Identifier(this.location + "/" + defaultLangPath + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
      res = new Identifier(this.location + "/" + path);
      if (this.resourceExists(res)) {
        return res;
      }
    }
    else {
      Identifier res = new Identifier(path);
      if (this.resourceExists(res)) {
        return res;
      }
    }

    return safe ? new Identifier("") : null;
  }

  @Override
  public Resource getResource(@Nullable Identifier loc) {
    if (loc == null) {
      return null;
    }
    try {
      return MinecraftClient.getInstance().getResourceManager().getResource(loc);
    }
    catch (IOException e) {
      return null;
    }
  }

  @Override
  public boolean resourceExists(@Nullable Identifier location) {
    if (location == null) {
      return false;
    }
    try {
      MinecraftClient.getInstance().getResourceManager().getResource(location);
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipCommments) {
    if (resource == null) {
      return "";
    }
    try {
      Iterator<String> iterator = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8).iterator();
      StringBuilder builder = new StringBuilder();

      boolean isLongComment = false;

      while (iterator.hasNext()) {
        String s = iterator.next().trim() + "\n";

        // Comment skipper
        if (skipCommments) {
          if (isLongComment) {
            if (s.endsWith("*/")) {
              isLongComment = false;
            }
            continue;
          }
          else {
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
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }
}
