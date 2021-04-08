package slimeknights.mantle.client.book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Hand;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.action.protocol.ProtocolGoToPage;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.ContentBlank;
import slimeknights.mantle.client.book.data.content.ContentBlockInteraction;
import slimeknights.mantle.client.book.data.content.ContentCrafting;
import slimeknights.mantle.client.book.data.content.ContentImage;
import slimeknights.mantle.client.book.data.content.ContentImageText;
import slimeknights.mantle.client.book.data.content.ContentSmelting;
import slimeknights.mantle.client.book.data.content.ContentSmithing;
import slimeknights.mantle.client.book.data.content.ContentStructure;
import slimeknights.mantle.client.book.data.content.ContentText;
import slimeknights.mantle.client.book.data.content.ContentTextImage;
import slimeknights.mantle.client.book.data.content.ContentTextLeftImage;
import slimeknights.mantle.client.book.data.content.ContentTextRightImage;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.deserializer.HexStringDeserializer;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.UpdateSavedPagePacket;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class BookLoader implements ResourceReloadListener {

  /** GSON object to be used for book loading purposes */
  public static final Gson GSON = new GsonBuilder().registerTypeAdapter(int.class, new HexStringDeserializer())
          .create();

  /** Maps page content presets to names */
  private static final HashMap<String, Class<? extends PageContent>> typeToContentMap = new HashMap<>();

  /** Internal registry of all books for the purposes of the reloader, maps books to name */
  private static final HashMap<String, BookData> books = new HashMap<>();

  public BookLoader() {
    // Register page types
    registerPageType("blank", ContentBlank.class);
    registerPageType("text", ContentText.class);
    registerPageType("image", ContentImage.class);
    registerPageType("image with text below", ContentImageText.class);
    registerPageType("text with image below", ContentTextImage.class);
    registerPageType("text with left image etch", ContentTextLeftImage.class);
    registerPageType("text with right image etch", ContentTextRightImage.class);
    registerPageType("crafting", ContentCrafting.class);
    registerPageType("smelting", ContentSmelting.class);
    registerPageType("smithing", ContentSmithing.class);
    registerPageType("block interaction", ContentBlockInteraction.class);
    registerPageType("structure", ContentStructure.class);

    // Register action protocols
    StringActionProcessor.registerProtocol(new ProtocolGoToPage());
    StringActionProcessor.registerProtocol(new ProtocolGoToPage(true, ProtocolGoToPage.GO_TO_RTN));
  }

  /**
   * Registers a type of page prefabricate
   *
   * @param name  The name of the page type
   * @param clazz The PageContent class for this page type
   * @RecommendedInvoke init
   */
  public static void registerPageType(String name, Class<? extends PageContent> clazz) {
    if (typeToContentMap.containsKey(name)) {
      throw new IllegalArgumentException("Page type " + name + " already in use.");
    }

    typeToContentMap.put(name, clazz);
  }

  /**
   * Gets a type of page prefabricate by name
   *
   * @param name The name of the page type
   * @return The class of the page type, ContentError.class if page type not registered
   */
  @Nullable
  public static Class<? extends PageContent> getPageType(String name) {
    return typeToContentMap.get(name);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param name         The name of the book, modid: will be automatically appended to the front of the name unless that is already added
   * @param repositories All the repositories the book will load the sections from
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(String name, BookRepository... repositories) {
    return registerBook(name, true, true, repositories);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param name               The name of the book, modid: will be automatically appended to the front of the name unless that is already added
   * @param appendIndex        Whether an index should be added to the front of the book using a BookTransformer
   * @param appendContentTable Whether a table of contents should be added to the front of each section using a BookTransformer
   * @param repositories       All the repositories the book will load the sections from
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(String name, boolean appendIndex, boolean appendContentTable, BookRepository... repositories) {
    BookData info = new BookData(repositories);

    books.put(name.contains(":") ? name : Mantle.modId + ":" + name, info);

    if (appendIndex) {
      info.addTransformer(BookTransformer.indexTranformer());
    }
    if (appendContentTable) {
      info.addTransformer(BookTransformer.contentTableTransformer());
    }

    return info;
  }

  public static void updateSavedPage(@Nullable PlayerEntity player, ItemStack item, String page) {
    if (player == null) {
      return;
    }
    if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
      return;
    }

    BookHelper.writeSavedPage(item, page);
    MantleNetwork.INSTANCE.sendToServer(new UpdateSavedPagePacket(page));
  }

  /**
   * Reloads all the books, called when the resource manager reloads, such as when the resource pack or the language is changed
   */
  @Override
  public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
    books.forEach((s, bookData) -> bookData.reset());
    return null;
  }
}