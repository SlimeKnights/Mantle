package slimeknights.mantle.command;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;

import java.nio.file.Path;
import java.nio.file.Paths;

/** A command for different book */
public class BookCommand {
  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.getEntity() instanceof AbstractClientPlayer)
      .then(Commands.literal("open")
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .executes(BookCommand::openBook)))
      .then(Commands.literal("export_images")
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .executes(BookCommand::exportImages)));
  }

  /**
   * Opens the specified book
   * @param context  Command context
   * @return  Integer return
   */
  private static int openBook(CommandContext<CommandSourceStack> context) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");

    BookData bookData = BookLoader.getBook(book);
    if(bookData != null) {
      bookData.openGui(Component.literal("Book"), "", null, null);
    } else {
      OpenNamedBookPacket.ClientOnly.errorStatus(book);
      return 1;
    }

    return 0;
  }

  /**
   * Renders all images in the book to files
   * @param context  Command context
   * @return  Integer return
   */
  private static int exportImages(CommandContext<CommandSourceStack> context) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");

    BookData bookData = BookLoader.getBook(book);
    if(bookData != null) {
      int width = BookScreen.PAGE_WIDTH_UNSCALED * 2;
      int height = BookScreen.PAGE_HEIGHT_UNSCALED;
      float zFar = 1000.0F + 10000.0F * 3;

      bookData.load();
      BookScreen screen = new BookScreen(Component.literal("Book"), bookData, "", null, null);
      screen.init(Minecraft.getInstance(), width, height);
      screen.drawArrows = false;

      Path gameDirectory = Minecraft.getInstance().gameDirectory.toPath();
      Path screenshotDir = Paths.get(gameDirectory.toString(), Screenshot.SCREENSHOT_DIR, "mantle_book", book.getNamespace(), book.getPath());
      if(!screenshotDir.toFile().mkdirs() && !screenshotDir.toFile().exists()) {
        throw new CommandRuntimeException(Component.literal("Failed to create directory for screenshots"));
      }

      Matrix4f matrix = (new Matrix4f()).setOrtho(0.0F, width, height, 0.0F, 1000.0F, zFar);
      RenderSystem.setProjectionMatrix(matrix, VertexSorting.ORTHOGRAPHIC_Z);

      PoseStack stack = RenderSystem.getModelViewStack();
      stack.pushPose();
      stack.setIdentity();
      stack.translate(0, 0, 1000F - zFar);
      RenderSystem.applyModelViewMatrix();
      Lighting.setupFor3DItems();

      RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);

      try {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(new BufferBuilder(2048));
        target.bindWrite(true);

        RenderSystem.clear(256, Minecraft.ON_OSX);

        GuiGraphics gui = new GuiGraphics(Minecraft.getInstance(), buffer);

        do {
          RenderSystem.clear(256, Minecraft.ON_OSX);

          screen.tick();
          screen.render(gui, 0, 0, 0);
          gui.flush();

          try (NativeImage image = takeScreenshot(target)) {
            int page = screen.getPage_();
            String pageFormat = page < 0 ? "cover" : "page_" + page;
            Path path = Paths.get(screenshotDir.toString(), pageFormat + ".png");

            if (page == -1) { // the cover is half the width
              try (NativeImage scaled = new NativeImage(image.format(), BookScreen.PAGE_WIDTH_UNSCALED, BookScreen.PAGE_HEIGHT_UNSCALED, false)) {
                image.copyRect(scaled, image.getWidth() / 2 - BookScreen.PAGE_WIDTH_UNSCALED / 2, 0, 0, 0, BookScreen.PAGE_WIDTH_UNSCALED, BookScreen.PAGE_HEIGHT_UNSCALED, false, false);
                scaled.writeToFile(path);
              } catch (Exception e) {
                Mantle.logger.error("Failed to save screenshot", e);
              }
            } else {
              image.writeToFile(path);
            }
          } catch (Exception e) {
            Mantle.logger.error("Failed to save screenshot", e);
            throw new CommandRuntimeException(Component.literal("Failed to save screenshot"));
          }
        } while (screen.nextPage());
      } finally {
        stack.popPose();
        RenderSystem.applyModelViewMatrix();
        target.unbindWrite();
        target.destroyBuffers();
      }
    } else {
      OpenNamedBookPacket.ClientOnly.errorStatus(book);
      return 1;
    }

    return 0;
  }

  /**
   * Duplicate of Screenshot#takeScreenshot, but with transparency
   */
  private static NativeImage takeScreenshot(RenderTarget pFramebuffer) {
    int i = pFramebuffer.width;
    int j = pFramebuffer.height;
    NativeImage nativeimage = new NativeImage(i, j, false);
    RenderSystem.bindTexture(pFramebuffer.getColorTextureId());
    nativeimage.downloadTexture(0, false);
    nativeimage.flipY();
    return nativeimage;
  }
}
