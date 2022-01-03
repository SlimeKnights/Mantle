package slimeknights.mantle.client.book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.action.protocol.ProtocolGoToPage;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.ContentBlank;
import slimeknights.mantle.client.book.data.content.ContentBlockInteraction;
import slimeknights.mantle.client.book.data.content.ContentCrafting;
import slimeknights.mantle.client.book.data.content.ContentImage;
import slimeknights.mantle.client.book.data.content.ContentImageText;
import slimeknights.mantle.client.book.data.content.ContentIndex;
import slimeknights.mantle.client.book.data.content.ContentPadding;
import slimeknights.mantle.client.book.data.content.ContentShowcase;
import slimeknights.mantle.client.book.data.content.ContentSmelting;
import slimeknights.mantle.client.book.data.content.ContentSmithing;
import slimeknights.mantle.client.book.data.content.ContentStructure;
import slimeknights.mantle.client.book.data.content.ContentText;
import slimeknights.mantle.client.book.data.content.ContentTextImage;
import slimeknights.mantle.client.book.data.content.ContentTextLeftImage;
import slimeknights.mantle.client.book.data.content.ContentTextRightImage;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.deserializer.ConditionDeserializer;
import slimeknights.mantle.client.book.data.deserializer.HexStringDeserializer;
import slimeknights.mantle.client.book.data.element.IngredientData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.book.transformer.BookTransformer;
import slimeknights.mantle.client.book.transformer.IndexTransformer;
import slimeknights.mantle.data.ResourceLocationSerializer;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;

public class BookLoader implements ResourceManagerReloadListener {

  /**
   * GSON object to be used for book loading purposes
   */
  private static Gson gson;
  private static boolean gsonDirty = true;
  private static final HashMap<Type, Object> gsonTypeAdapters = new HashMap<>();

  /**
   * Maps page content presets to names
   */
  private static final HashMap<ResourceLocation, Class<? extends PageContent>> typeToContentMap = new HashMap<>();

  /**
   * Internal registry of all books for the purposes of the reloader, maps books to name
   */
  private static final HashMap<ResourceLocation, BookData> books = new HashMap<>();

  public BookLoader() {
    // Register page types
    registerPageType(ContentBlank.ID, ContentBlank.class);
    registerPageType(ContentText.ID, ContentText.class);
    registerPageType(ContentPadding.LEFT_ID, ContentPadding.ContentLeftPadding.class);
    registerPageType(ContentPadding.RIGHT_ID, ContentPadding.ContentRightPadding.class);
    registerPageType(ContentImage.ID, ContentImage.class);
    registerPageType(ContentImageText.ID, ContentImageText.class);
    registerPageType(ContentTextImage.ID, ContentTextImage.class);
    registerPageType(ContentTextLeftImage.ID, ContentTextLeftImage.class);
    registerPageType(ContentTextRightImage.ID, ContentTextRightImage.class);
    registerPageType(ContentCrafting.ID, ContentCrafting.class);
    registerPageType(ContentSmelting.ID, ContentSmelting.class);
    registerPageType(ContentSmithing.ID, ContentSmithing.class);
    registerPageType(ContentBlockInteraction.ID, ContentBlockInteraction.class);
    registerPageType(ContentStructure.ID, ContentStructure.class);
    registerPageType(ContentIndex.ID, ContentIndex.class);
    registerPageType(ContentShowcase.ID, ContentShowcase.class);

    // Register action protocols
    StringActionProcessor.registerProtocol(Mantle.getResource("go-to-page"), new ProtocolGoToPage(false));
    StringActionProcessor.registerProtocol(Mantle.getResource("go-to-page-rtn"), new ProtocolGoToPage(true));

    // Register GSON type adapters
    registerGsonTypeAdapter(ResourceLocation.class, ResourceLocationSerializer.resourceLocation("mantle"));
    registerGsonTypeAdapter(int.class, new HexStringDeserializer());
    registerGsonTypeAdapter(ICondition.class, new ConditionDeserializer());
    registerGsonTypeAdapter(IngredientData.class, new IngredientData.Deserializer());

    // Register page types that are implicitly hidden from indexes
    IndexTransformer.addHiddenPageType(ContentBlank.ID);
    IndexTransformer.addHiddenPageType(ContentPadding.LEFT_ID);
    IndexTransformer.addHiddenPageType(ContentPadding.RIGHT_ID);
    IndexTransformer.addHiddenPageType(ContentIndex.ID);
  }

  /**
   * Registers a type of page prefabricate
   *
   * @param id    The name of the page type
   * @param clazz The PageContent class for this page type
   * @RecommendedInvoke init
   */
  public static void registerPageType(ResourceLocation id, Class<? extends PageContent> clazz) {
    if (typeToContentMap.containsKey(id)) {
      throw new IllegalArgumentException("Page type " + id + " already in use.");
    }

    typeToContentMap.put(id, clazz);
  }

  /**
   * Gets a type of page prefabricate by name
   *
   * @param name The name of the page type
   * @return The class of the page type, ContentError.class if page type not registered
   */
  @Nullable
  public static Class<? extends PageContent> getPageType(ResourceLocation name) {
    return typeToContentMap.get(name);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param id           The ID of the book
   * @param repositories All the repositories the book will load the sections from
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(ResourceLocation id, BookRepository... repositories) {
    return registerBook(id, true, true, repositories);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param id                 The ID of the book
   * @param appendIndex        Whether an index should be added to the front of the book using a BookTransformer
   * @param appendContentTable Whether a table of contents should be added to the front of each section using a BookTransformer
   * @param repositories       All the repositories the book will load the sections from
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(ResourceLocation id, boolean appendIndex, boolean appendContentTable, BookRepository... repositories) {
    BookData info = new BookData(repositories);

    if (appendIndex) {
      info.addTransformer(BookTransformer.indexTranformer());
    }
    if (appendContentTable) {
      info.addTransformer(BookTransformer.contentTableTransformer());
    }

    books.put(id, info);
    return info;
  }

  /**
   * Gets the instance of the given book
   * @param id The ID of the book to retrieve
   * @return The book object, or null if it doesn't exist
   */
  @Nullable
  public static BookData getBook(ResourceLocation id) {
    return books.getOrDefault(id, null);
  }

  /**
   * Updates the saved page of a held book
   * @param player  Player instance
   * @param hand    Hand
   * @param page    New page
   */
  public static void updateSavedPage(@Nullable Player player, InteractionHand hand, String page) {
    if (player != null) {
      ItemStack item = player.getItemInHand(hand);
      if (!item.isEmpty()) {
        BookHelper.writeSavedPageToBook(item, page);
        MantleNetwork.INSTANCE.network.sendToServer(new UpdateHeldPagePacket(hand, page));
      }
    }
  }

  /**
   * Updates the saved page of a held book
   * @param pos     Position being changed
   * @param page    New page
   */
  public static void updateSavedPage(BlockPos pos, String page) {
    MantleNetwork.INSTANCE.network.sendToServer(new UpdateLecternPagePacket(pos, page));
  }

  public static Gson getGson() {
    if(gson == null || gsonDirty) {
      GsonBuilder builder = new GsonBuilder();

      for(Type type : gsonTypeAdapters.keySet()) {
        builder.registerTypeAdapter(type, gsonTypeAdapters.get(type));
      }

      gson = builder.create();
      gsonDirty = false;
    }

    return gson;
  }

  public static void registerGsonTypeAdapter(Type type, Object adapter) {
    gsonTypeAdapters.put(type, adapter);
    gsonDirty = true;
  }

  /**
   * Reloads all the books, called when the resource manager reloads, such as when the resource pack or the language is changed
   */
  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    books.forEach((s, bookData) -> bookData.reset());
  }
}
