package slimeknights.mantle.client.book;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.action.protocol.ProtocolGoToPage;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.ContentBlank;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.ContentImage;
import slimeknights.mantle.client.book.data.content.ContentImageText;
import slimeknights.mantle.client.book.data.content.ContentText;
import slimeknights.mantle.client.book.data.content.ContentTextImage;
import slimeknights.mantle.client.book.data.content.ContentTextLeftImage;
import slimeknights.mantle.client.book.data.content.ContentTextRightImage;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.network.NetworkWrapper;
import slimeknights.mantle.network.book.PacketUpdateSavedPage;
import static slimeknights.mantle.client.book.ResourceHelper.setBookRoot;

@SideOnly(Side.CLIENT)
public class BookLoader implements IResourceManagerReloadListener {

  /** GSON object to be used for book loading purposes */
  public static final Gson GSON = new Gson();

  /** Maps page content presets to names */
  private static final HashMap<String, Class<? extends PageContent>> typeToContentMap = new HashMap<>();

  /** Internal registry of all books for the purposes of the reloader, maps books to name */
  private static final HashMap<String, BookData> books = new HashMap<>();

  private static final NetworkWrapper wrapper = new NetworkWrapper("mantle:books");

  private static final Random random = new Random();
  private static char[] randAlphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

  public BookLoader() {
    ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);

    wrapper.registerPacketServer(PacketUpdateSavedPage.class);

    // Register page types
    registerPageType("blank", ContentBlank.class);
    registerPageType("text", ContentText.class);
    registerPageType("image", ContentImage.class);
    registerPageType("image with text below", ContentImageText.class);
    registerPageType("text with image below", ContentTextImage.class);
    registerPageType("text with left image etch", ContentTextLeftImage.class);
    registerPageType("text with right image etch", ContentTextRightImage.class);

    // Register action protocols
    StringActionProcessor.registerProtocol(new ProtocolGoToPage());
    StringActionProcessor.registerProtocol(new ProtocolGoToPage(true, "-rtn"));
  }

  /**
   * Registers a type of page prefabricate
   *
   * @param name  The name of the page type
   * @param clazz The PageContent class for this page type
   * @RecommendedInvoke init
   */
  public static void registerPageType(String name, Class<? extends PageContent> clazz) {
    if (typeToContentMap.containsKey(name))
      throw new IllegalArgumentException("Page type " + name + " already in use.");

    typeToContentMap.put(name, clazz);
  }

  /**
   * Gets a type of page prefabricate by name
   *
   * @param name The name of the page type
   * @return The class of the page type, ContentError.class if page type not registered
   */
  public static Class<? extends PageContent> getPageType(String name) {
    return typeToContentMap.getOrDefault(name, ContentError.class);
  }

  /**
   * Adds a book to the loader, and returns a reference object
   * Be warned that the returned BookData object is not immediately populated, and is instead populated when the resources are loaded/reloaded
   *
   * @param name     The name of the book, modid: will be automatically appended to the front of the name unless that is already added
   * @param location The location of the book folder, prefixed with the resource domain
   * @return The book object, not immediately populated
   */
  public static BookData registerBook(String name, String location) {
    BookData info = new BookData(location);

    books.put(name.contains(":") ? name : Loader.instance().activeModContainer().getModId() + ":" + name, info);

    info.addTransformer(new BookTransformer.IndexTranformer());

    return info;
  }

  /**
   * Returns a book by its name
   *
   * @param name The name of the book, prefixed with modid:
   * @return The instance of the book data, or null if the book is not registered
   */
  public static BookData getBook(String name) {
    return null;
  }

  public static void updateSavedPage(EntityPlayer player, ItemStack item, String page) {
    if (player.getHeldItem() == null)
      return;

    BookHelper.writeSavedPage(item, page);
    wrapper.network.sendToServer(new PacketUpdateSavedPage(page));
  }

  public static String randomName() {
    int length = random.nextInt(10);

    String s = "";

    for (int i = 0; i < length; i++) {
      s += randAlphabet[random.nextInt(randAlphabet.length)];
    }

    return s;
  }

  /**
   * Reloads all the books, called when the resource manager reloads, such as when the resource pack or the language is changed
   */
  @Override
  public void onResourceManagerReload(IResourceManager resourceManager) {
    for (BookData book : books.values()) {
      setBookRoot(book.bookLocation);

      book.pageCount = book.cascadeLoad();
      book.fullPageCount = (int) Math.ceil((book.pageCount - 1) / 2F) + 1;
    }
  }
}