package slimeknights.mantle.command.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import slimeknights.mantle.compat.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.text.WordUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.command.GeneratePackHelper;
import slimeknights.mantle.command.MantleCommand;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** A command for different book operations, currently open and export_images */
public class BookCommand {
  private static final String BOOK_NOT_FOUND = "command.mantle.book_test.not_found";

  private static final String EXPORT_SUCCESS = "command.mantle.book.export.success";
  private static final String EXPORT_SUCCESS_HTML = "command.mantle.book.export.html.success";
  private static final String EXPORT_FAIL = "command.mantle.book.export.error_generic";
  private static final String EXPORT_FAIL_IO = "command.mantle.book.export.error_io";

  private static final String DEFAULT_BOOK_VERSION = "20";
  private static final String VERSION_FULL = "1.20";
  private static final int DEFAULT_SCALE = 2;

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
        // mantle book export_images <domain> [version]
        .then(Commands.argument("domain", StringArgumentType.word()).suggests(MantleClientCommand.REGISTERED_BOOK_DOMAINS)
          .then(Commands.argument("scale", IntegerArgumentType.integer(1, 16))
            .executes(context -> exportDomainImages(context, IntegerArgumentType.getInteger(context, "scale"))))
          .executes(context -> exportDomainImages(context, DEFAULT_SCALE)))
        // mantle book export_images <domain> [version]
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .then(Commands.argument("scale", IntegerArgumentType.integer(1, 16))
            .executes(context -> exportImages(context, IntegerArgumentType.getInteger(context, "scale"))))
          .executes(context -> exportImages(context, DEFAULT_SCALE))))

      .then(Commands.literal("export_html")
        // mantle book export_html <domain> [version]
        .then(Commands.argument("domain", StringArgumentType.word()).suggests(MantleClientCommand.REGISTERED_BOOK_DOMAINS)
          .then(Commands.argument("version", StringArgumentType.word())
            .executes(context -> exportDomainHtml(context, StringArgumentType.getString(context, "version"))))
          .executes(context -> exportDomainHtml(context, DEFAULT_BOOK_VERSION)))
        // mantle book export_html <id> [version]
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
          .then(Commands.argument("version", StringArgumentType.word())
            .executes(context -> exportHTML(context, StringArgumentType.getString(context, "version"))))
          .executes(context -> exportHTML(context, DEFAULT_BOOK_VERSION))));
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
   * Renders all images in the book to files
   * @param context  Command context
   * @return  Integer return
   */
  private static int exportImages(CommandContext<CommandSourceStack> context, int scale) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");
    return doExport(book, scale, false, DEFAULT_BOOK_VERSION);
  }

  /**
   * Renders all images in the books in the given domain to files
   * @param context  Command context
   * @return  Integer return
   */
  private static int exportDomainImages(CommandContext<CommandSourceStack> context, int scale) {
    String domain = StringArgumentType.getString(context, "domain");
    for (ResourceLocation book : BookLoader.getAllBooks()) {
      if (domain.equals(book.getNamespace())) {
        int code = doExport(book, scale, false, DEFAULT_BOOK_VERSION);
        if (code != 0) return code;
      }
    }
    return 0;
  }

  /**
   * Exports all pages in the book to HTML and png
   * @param context Command context
   * @return Integer return
   */
  private static int exportHTML(CommandContext<CommandSourceStack> context, String version) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");
    return doExport(book, 2, true, version);
  }

  /**
   * Exports all pages in all books to HTML and png
   * @param context Command context
   * @return Integer return
   */
  private static int exportDomainHtml(CommandContext<CommandSourceStack> context, String version) {
    String domain = StringArgumentType.getString(context, "domain");
    for (ResourceLocation book : BookLoader.getAllBooks()) {
      if (domain.equals(book.getNamespace())) {
        int code = doExport(book, 2, true, version);
        if (code != 0) return code;
      }
    }
    return 0;
  }

  /**
   * Renders all images in the book to files
   * @param book  Book to export
   * @param scale  Scale to export at
   * @param html  Include HTML
   * @param version  version in each files header
   * @return  Integer return
   */
  private static int doExport(ResourceLocation book, int scale, boolean html, String version) {
    BookData bookData = BookLoader.getBook(book);

    Path gameDirectory = Minecraft.getInstance().gameDirectory.toPath();
    // images go in screenshots
    Path screenshotDir = Paths.get(gameDirectory.toString(), Screenshot.SCREENSHOT_DIR, "mantle_book", book.getNamespace(), book.getPath());
    // html goes in root folder
    Path htmlDir = html ? Paths.get(gameDirectory.toString(), "mantle_book", book.getNamespace(), book.getPath().replace('_', '-')) : null;
    if (bookData != null) {
      // ensure outputs exist
      if (!screenshotDir.toFile().mkdirs() && !screenshotDir.toFile().exists()) {
        throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL_IO, screenshotDir));
      }
      if (htmlDir != null && !htmlDir.toFile().mkdirs() && !htmlDir.toFile().exists()) {
        throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL_IO, htmlDir));
      }

      int width = BookScreen.PAGE_WIDTH_UNSCALED * 2 * scale;
      int height = BookScreen.PAGE_HEIGHT_UNSCALED * scale;
      float zFar = 1000.0F + 10000.0F * 3;

      bookData.load();
      BookScreen screen = new BookScreen(Component.literal("Book"), bookData, "", null, null);
      screen.init(Minecraft.getInstance(), width / scale, height / scale);
      screen.drawArrows = false;
      screen.mouseInput = false;
      screen.drawText = !html;
      screen.enableAnimations = false;

      Matrix4f matrix = (new Matrix4f()).setOrtho(0.0F, width, height, 0.0F, 1000.0F, zFar);
      RenderSystem.setProjectionMatrix(matrix, VertexSorting.ORTHOGRAPHIC_Z);

      Matrix4fStack stack = RenderSystem.getModelViewStack();
      stack.pushMatrix();
      stack.identity();
      stack.translate(0, 0, 1000F - zFar);
      stack.scale(scale, scale, 1);
      RenderSystem.applyModelViewMatrix();
      Lighting.setupFor3DItems();

      RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
      target.enableStencil();

      try {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        target.bindWrite(true);

        GuiGraphics gui = new GuiGraphics(Minecraft.getInstance(), buffer);

        String bookKey = book.getPath() + "_" + version;
        // title goes export title -> regular title -> path
        String exportTitle = bookData.appearance.exportTitle;
        if (exportTitle.isEmpty()) {
          exportTitle = bookData.appearance.title;
          if (exportTitle.isEmpty()) {
            exportTitle = WordUtils.capitalize(book.getPath().replace('_', ' '));
          }
        }

        // fetch mod display name if possible
        ModContainer mod = ModList.get().getModContainerById(book.getNamespace()).orElse(null);
        String modName = mod == null ? book.getNamespace() : mod.getModInfo().getDisplayName();

        do {
          RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

          screen.tick();

          RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

          int page = screen.getPage_();
          // draw text on the cover; we just want it as an image
          if (html) {
            screen.drawText = page < 0;
          }

          gui.pose().pushPose();
          screen.render(gui, 0, 0, 0);
          gui.flush();
          gui.pose().popPose();

          try (NativeImage image = takeScreenshot(target)) {
            String pageFormat = page < 0 ? "cover" :  (html ? "clean_" : "page_") + page;
            Path path = Paths.get(screenshotDir.toString(), pageFormat + ".png");

            if (page == -1) { // the cover is half the width
              try (NativeImage scaled = new NativeImage(image.format(), width / 2, height, false)) {
                image.copyRect(scaled, image.getWidth() / 2 - width / 4, 0, 0, 0,
                  width / 2, height, false, false);
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

          if (html) {
            File file = Paths.get(htmlDir.toString(), page < 0 ? "index.html" : "page-" + page + ".html").toFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
              writer.write(page < 0 ? screen.coverToHtml(bookKey, exportTitle, VERSION_FULL, modName) : screen.pageToHtml(bookKey, exportTitle, VERSION_FULL, modName));
            } catch (IOException e) {
              Mantle.logger.error("Failed to export HTML", e);
              throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL));
            }
          }
        } while (screen.nextPage());

        // add gallery page
        if (html) {
          File file = Paths.get(htmlDir.toString(), "gallery.html").toFile();
          try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(galleryHtml(bookKey, exportTitle, modName));
          } catch (IOException e) {
            Mantle.logger.error("Failed to export HTML", e);
            throw new CommandRuntimeException(Component.translatable(EXPORT_FAIL));
          }
        }
      } finally {
        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.defaultBlendFunc();
        target.unbindWrite();
        target.destroyBuffers();
      }
    } else {
      bookNotFound(book);
      return 1;
    }

    sendFileMessage(screenshotDir, htmlDir);
    return 0;
  }


  /** Creates the gallery HTML page */
  private static String galleryHtml(String bookName, String title, String mod) {
    return "---\n" +
      "layout: book-gallery\n" +
      "title: " + title + " (" + VERSION_FULL + ") - Gallery" + '\n' +
      "breadcrumb: Gallery\n" +
      "description: Gallery of all pages for " + title + " from " + mod + " in Minecraft " + VERSION_FULL + ".\n" +
      "book: " + bookName + '\n' +
      "link_prefix: ../\n" +
      "link_suffix: /gallery\n" +
      "---\n\n";
  }

  /** Send a message to the player linking the directory */
  private static void sendFileMessage(Path screenshotDir, @Nullable Path htmlDir) {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      Component fileComponent = GeneratePackHelper.getOutputComponent(screenshotDir);
      if (htmlDir != null) {
        player.displayClientMessage(Component.translatable(EXPORT_SUCCESS_HTML, fileComponent, GeneratePackHelper.getOutputComponent(htmlDir)), false);
      } else {
        player.displayClientMessage(Component.translatable(EXPORT_SUCCESS, fileComponent), false);
      }
    }
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

  public static void bookNotFound(ResourceLocation book) {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      player.displayClientMessage(Component.translatable(BOOK_NOT_FOUND, book).withStyle(ChatFormatting.RED), false);
    }
  }
}
