package slimeknights.mantle.command.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.command.MantleCommand;

import java.nio.file.Path;
import java.nio.file.Paths;

/** A command for different book */
public class BookCommand {
  private static final String BOOK_NOT_FOUND = "command.mantle.book_test.not_found";

  private static final String EXPORT_SUCCESS = "command.mantle.book.export.success";
  private static final String EXPORT_FAIL = "command.mantle.book.export.error_generic";
  private static final String EXPORT_FAIL_IO = "command.mantle.book.export.error_io";

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS) && source.getEntity() instanceof AbstractClientPlayer)
      .then(Commands.literal("open")
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .executes(BookCommand::openBook)))
      .then(Commands.literal("export_images")
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .then(Commands.argument("scale", IntegerArgumentType.integer(1, 16))
            .executes(BookCommand::exportImagesWithScale))
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
      // Delay execution to ensure chat window is closed
      Minecraft.getInstance().tell(() ->
        bookData.openGui(Component.literal("Book"), "", null, null)
      );
    } else {
      bookNotFound(book);
      return 1;
    }

    return 0;
  }

  /**
   * Renders all images in the book to files at specified scale
   * @param context  Command context
   * @return  Integer return
   */
  private static int exportImagesWithScale(CommandContext<CommandSourceStack> context) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");
    int scale = context.getArgument("scale", Integer.class);

    return doExportImages(book, scale);
  }

  /**
   * Renders all images in the book to files
   * @param context  Command context
   * @return  Integer return
   */
  private static int exportImages(CommandContext<CommandSourceStack> context) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");

    return doExportImages(book, 1);
  }

  /**
   * Renders all images in the book to files
   * @param book  Book to export
   * @param scale  Scale to export at
   * @return  Integer return
   */
  private static int doExportImages(ResourceLocation book, int scale) {
    BookData bookData = BookLoader.getBook(book);

    Path gameDirectory = Minecraft.getInstance().gameDirectory.toPath();
    Path screenshotDir = Paths.get(gameDirectory.toString(), "screenshots", "mantle_book", book.getNamespace(), book.getPath());

    if(bookData != null) {
      if(!screenshotDir.toFile().mkdirs() && !screenshotDir.toFile().exists()) {
        throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL_IO));
      }

      int width = BookScreen.PAGE_WIDTH_UNSCALED * 2 * scale;
      int height = BookScreen.PAGE_HEIGHT_UNSCALED * scale;
      float zFar = 1000.0F + 10000.0F * 3;

      bookData.load();
      BookScreen screen = new BookScreen(Component.literal("Book"), bookData, "", null, null);
      screen.init(Minecraft.getInstance(), width / scale, height / scale);
      screen.drawArrows = false;
      screen.mouseInput = false;

      Matrix4f matrix = Matrix4f.orthographic(0, width, 0, height, 1000.0F, zFar);
      RenderSystem.setProjectionMatrix(matrix);

      PoseStack stack = RenderSystem.getModelViewStack();
      stack.pushPose();
      stack.setIdentity();
      stack.translate(0, 0, 1000F - zFar);
      stack.scale(scale, scale, 1);
      RenderSystem.applyModelViewMatrix();
      Lighting.setupFor3DItems();

      RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
      target.enableStencil();

      try {
        target.bindWrite(true);

        PoseStack guiPose = new PoseStack();

        do {
          RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

          screen.tick();

          RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

          guiPose.pushPose();
          screen.render(guiPose, 0, 0, 0);
          guiPose.popPose();

          try (NativeImage image = takeScreenshot(target)) {
            int page = screen.getPage_();
            String pageFormat = page < 0 ? "cover" : "page_" + page;
            Path path = Paths.get(screenshotDir.toString(), pageFormat + ".png");

            if (page == -1) { // the cover is half the width
              try (NativeImage scaled = new NativeImage(image.format(), width / 2, height, false)) {
                copyRect(image, scaled, image.getWidth() / 2 - width / 4, 0, 0, 0,
                  width / 2, height);
                scaled.writeToFile(path);
              } catch (Exception e) {
                Mantle.logger.error("Failed to save screenshot", e);
                throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL));
              }
            } else {
              image.writeToFile(path);
            }
          } catch (Exception e) {
            Mantle.logger.error("Failed to save screenshot", e);
            throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL));
          }
        } while (screen.nextPage());
      } finally {
        stack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.defaultBlendFunc();
        target.unbindWrite();
        target.destroyBuffers();
      }
    } else {
      bookNotFound(book);
      return 1;
    }

    Player player = Minecraft.getInstance().player;
    if (player != null) {
      Component fileComponent = Component.literal(screenshotDir.toString()).withStyle(ChatFormatting.UNDERLINE)
        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotDir.toAbsolutePath().toString())));
      player.displayClientMessage(Component.translatable(EXPORT_SUCCESS, fileComponent), false);
    }
    return 0;
  }

  /**
   * Duplicate of {@link net.minecraft.client.Screenshot#takeScreenshot}, but with transparency
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

  /**
   * Minimalistic backport of NativeImage#copyRect
   */
  public static void copyRect(NativeImage src, NativeImage dst, int srcX, int srcY, int dstX, int dstY, int width, int height) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int color = src.getPixelRGBA(srcX + x, srcY + y);
        dst.setPixelRGBA(dstX + x, dstY + y, color);
      }
    }
  }

  public static void bookNotFound(ResourceLocation book) {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      player.displayClientMessage(Component.translatable(BOOK_NOT_FOUND, book).withStyle(ChatFormatting.RED), false);
    }
  }
}
