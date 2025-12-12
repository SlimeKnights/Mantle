package slimeknights.mantle.data.client;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.GenericDataProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Data generator to create png image files */
public abstract class GenericTextureGenerator extends GenericDataProvider {
  @Nullable
  protected final ExistingFileHelper existingFileHelper;
  @Nullable
  private final ExistingFileHelper.ResourceType resourceType;

  /** Constructor which marks files as existing */
  public GenericTextureGenerator(PackOutput packOutput, @Nullable ExistingFileHelper existingFileHelper, String folder) {
    super(packOutput, Target.RESOURCE_PACK, folder);
    this.existingFileHelper = existingFileHelper;
    if (existingFileHelper != null) {
      this.resourceType = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", folder);
    } else {
      this.resourceType = null;
    }
  }

  /** Saves the given image to the given location */
  protected CompletableFuture<?> saveImage(CachedOutput cache, ResourceLocation location, NativeImage image) {
    if (existingFileHelper != null && resourceType != null) {
      existingFileHelper.trackGenerated(location, resourceType);
    }
    return saveImage(cache, pathProvider, location, image);
  }

  /** Saves metadata for the given image */
  protected CompletableFuture<?> saveMetadata(CachedOutput cache, ResourceLocation location, JsonObject metadata) {
    return DataProvider.saveStable(cache, metadata, this.pathProvider.file(location, "png.mcmeta"));
  }


  /* Helpers */

  /** Reads an image from disk. Note the caller is responsible for closing the resource */
  public static NativeImage read(ExistingFileHelper existingFileHelper, String folder, ResourceLocation path) throws IOException {
    try {
      Resource resource = existingFileHelper.getResource(path, PackType.CLIENT_RESOURCES, ".png", folder);
      try (InputStream stream = resource.open()) {
        return NativeImage.read(stream);
      }
    } catch (IOException | NoSuchElementException e) {
      Mantle.logger.error("Failed to read image at {}", path, e);
      throw e;
    }
  }

  /** Saves the given image to the given location */
  public static CompletableFuture<?> saveImage(CachedOutput cache, PathProvider pathProvider, ResourceLocation location, NativeImage image) {
    return CompletableFuture.runAsync(() -> {
      try {
        Path path = pathProvider.file(location, "png");
        byte[] bytes = image.asByteArray();
        cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
      } catch (IOException e) {
        Mantle.logger.error("Couldn't write image for {}", location, e);
        throw new CompletionException(e);
      }
    }, Util.backgroundExecutor());
  }
}
