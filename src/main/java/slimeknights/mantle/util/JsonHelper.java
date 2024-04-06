package slimeknights.mantle.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.common.BlockStateLoadable;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.network.NetworkWrapper;
import slimeknights.mantle.network.packet.ISimplePacket;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utilities to help in parsing JSON
 */
@SuppressWarnings("unused")
public class JsonHelper {
  private JsonHelper() {}

  /** Default GSON instance, use instead of creating a new instance unless you need additional type adapaters */
  public static final Gson DEFAULT_GSON = (new GsonBuilder())
    .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();

  /**
   * Gets an element from JSON, throwing an exception if missing
   * @param json        Object parent
   * @param memberName  Name to get
   * @return  JsonElement found
   * @throws JsonSyntaxException if element is missing
   */
  public static JsonElement getElement(JsonObject json, String memberName) {
    if (json.has(memberName)) {
      return json.get(memberName);
    } else {
      throw new JsonSyntaxException("Missing " + memberName + "");
    }
  }

  /**
   * Parses a list from an JsonArray
   * @param array   Json array
   * @param name    Json key of the array
   * @param mapper  Mapper from the element object and name to new object
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <T> List<T> parseList(JsonArray array, String name, BiFunction<JsonElement,String,T> mapper) {
    if (array.size() == 0) {
      throw new JsonSyntaxException(name + " must have at least 1 element");
    }
    // build the list
    ImmutableList.Builder<T> builder = ImmutableList.builder();
    for (int i = 0; i < array.size(); i++) {
      builder.add(mapper.apply(array.get(i), name + "[" + i + "]"));
    }
    return builder.build();
  }

  /**
   * Parses a list from an JsonArray
   * @param array   Json array
   * @param name    Json key of the array
   * @param mapper  Mapper from the json object to new object
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <T> List<T> parseList(JsonArray array, String name, Function<JsonObject,T> mapper) {
    return parseList(array, name, (element, s) -> mapper.apply(GsonHelper.convertToJsonObject(element, s)));
  }

  /**
   * Parses a list from an JsonArray
   * @param parent  Parent JSON object
   * @param name    Json key of the array
   * @param mapper  Mapper from raw type to new object
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <T> List<T> parseList(JsonObject parent, String name, BiFunction<JsonElement,String,T> mapper) {
    return parseList(GsonHelper.getAsJsonArray(parent, name), name, mapper);
  }

  /**
   * Parses a list from an JsonArray
   * @param parent  Parent JSON object
   * @param name    Json key of the array
   * @param mapper  Mapper from json object to new object
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <T> List<T> parseList(JsonObject parent, String name, Function<JsonObject,T> mapper) {
    return parseList(GsonHelper.getAsJsonArray(parent, name), name, mapper);
  }

  /**
   * Gets a resource location from JSON, throwing a nice exception if invalid
   * @param json  JSON object
   * @param key   Key to fetch
   * @return  Resource location parsed
   */
  public static ResourceLocation getResourceLocation(JsonObject json, String key) {
    String text = GsonHelper.getAsString(json, key);
    ResourceLocation location = ResourceLocation.tryParse(text);
    if (location == null) {
      throw new JsonSyntaxException("Expected " + key + " to be a Resource location, was '" + text + "'");
    }
    return location;
  }

  /**
   * Gets a resource location from JSON, throwing a nice exception if invalid
   * @param json  JSON object
   * @param key   Key to fetch
   * @param fallback  Fallback if key is not present
   * @return  Resource location parsed
   */
  public static ResourceLocation getResourceLocation(JsonObject json, String key, ResourceLocation fallback) {
    if (json.has(key)) {
      return getResourceLocation(json, key);
    }
    return fallback;
  }

  /**
   * Gets a resource location from JSON, throwing a nice exception if invalid
   * @param json  JSON object
   * @param key   Key to fetch
   * @return  Resource location parsed
   */
  public static ResourceLocation convertToResourceLocation(JsonElement json, String key) {
    String text = GsonHelper.convertToString(json, key);
    ResourceLocation location = ResourceLocation.tryParse(text);
    if (location == null) {
      throw new JsonSyntaxException("Expected " + key + " to be a resource location, was '" + text + "'");
    }
    return location;
  }

  /**
   * Parses a registry entry from JSON
   * @param registry  Registry
   * @param element   Element to deserialize
   * @param key       Json key
   * @param <T>  Object type
   * @return  Registry value
   * @throws JsonSyntaxException  If something failed to parse
   */
  public static <T> T convertToEntry(IForgeRegistry<T> registry, JsonElement element, String key) {
    ResourceLocation name = JsonHelper.convertToResourceLocation(element, key);
    if (registry.containsKey(name)) {
      T value = registry.getValue(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unknown " + registry.getRegistryName() + " " + name);
  }

  /**
   * Parses a registry entry from JSON
   * @param registry  Registry
   * @param parent    Parent JSON object
   * @param key       Json key
   * @param <T>  Object type
   * @return  Registry value
   * @throws JsonSyntaxException  If something failed to parse
   */
  public static <T> T getAsEntry(IForgeRegistry<T> registry, JsonObject parent, String key) {
    return convertToEntry(registry, JsonHelper.getElement(parent, key), key);
  }

  /** Parses an enum from its name */
  private static <T extends Enum<T>> T enumByName(String name, Class<T> enumClass) {
    for (T value : enumClass.getEnumConstants()) {
      if (value.name().toLowerCase(Locale.ROOT).equals(name)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Invalid " + enumClass.getSimpleName() + " " + name);
  }

  /** Gets an enum value from its string name */
  public static <T extends Enum<T>> T convertToEnum(JsonElement element, String key, Class<T> enumClass) {
    String name = GsonHelper.convertToString(element, key);
    return enumByName(name, enumClass);
  }

  /** Gets an enum value from its string name */
  public static <T extends Enum<T>> T getAsEnum(JsonObject json, String key, Class<T> enumClass) {
    String name = GsonHelper.getAsString(json, key);
    return enumByName(name, enumClass);
  }

  /**
   * Parses a color as a string
   * @param color  Color to parse
   * @return  Parsed string
   * @deprecated use {@link ColorLoadable#parseString(String, String)}
   */
  @Deprecated(forRemoval = true)
  public static int parseColor(@Nullable String color) {
    if (color == null || color.isEmpty()) {
      return -1;
    }
    return ColorLoadable.ALPHA.parseString(color, "[unknown]");
  }


  /* Resource loaders */

  /**
   * Converts the resource into a JSON file
   * @param resource  Resource to read. Closed when done
   * @return  JSON object, or null if failed to parse
   */
  @Nullable
  public static JsonObject getJson(Resource resource, ResourceLocation location) {
    try (Reader reader = resource.openAsReader()) {
      return GsonHelper.parse(reader);
    } catch (JsonParseException | IOException e) {
      Mantle.logger.error("Failed to load JSON from resource {} from pack '{}'", location, resource.sourcePackId(), e);
      return null;
    }
  }

  /** Gets a list of JSON objects for a single path in all domains and packs, for a language file like loader */
  public static List<JsonObject> getFileInAllDomainsAndPacks(ResourceManager manager, String path, @Nullable String preferredPath) {
    return manager
      .getNamespaces().stream()
      .filter(ResourceLocation::isValidNamespace)
      .flatMap(namespace -> {
        ResourceLocation location = new ResourceLocation(namespace, path);
        return manager.getResourceStack(location).stream()
          .map(preferredPath != null ? resource -> {
            Mantle.logger.warn("Using deprecated path {} in pack {} - use {}:{} instead", location, resource.sourcePackId(), location.getNamespace(), preferredPath);
            return getJson(resource, location);
          } : resource -> JsonHelper.getJson(resource, location));
      }).filter(Objects::nonNull).toList();
  }

  /** Sends the packet to the given player */
  private static void sendPackets(NetworkWrapper network, ServerPlayer player, ISimplePacket[] packets) {
    // on an integrated server, the modifier registries have a single instance on both the client and the server thread
    // this means syncing is unneeded, and has the side-effect of recreating all the modifier instances (which can lead to unexpected behavior)
    // as a result, integrated servers just mark fullyLoaded as true without syncing anything, side-effect is listeners may run twice on single player

    // on a dedicated server, the client is running a separate game instance, this is where we send packets, plus fully loaded should already be true
    // this event is not fired when connecting to a server
    if (!player.connection.connection.isMemoryConnection()) {
      PacketTarget target = PacketDistributor.PLAYER.with(() -> player);
      for (ISimplePacket packet : packets) {
        network.send(target, packet);
      }
    }
  }

  /** Called when the player logs in to send packets */
  public static void syncPackets(OnDatapackSyncEvent event, NetworkWrapper network, ISimplePacket... packets) {
    // send to single player
    ServerPlayer targetedPlayer = event.getPlayer();
    if (targetedPlayer != null) {
      sendPackets(network, targetedPlayer, packets);
    } else {
      // send to all players
      for (ServerPlayer player : event.getPlayerList().getPlayers()) {
        sendPackets(network, player, packets);
      }
    }
  }

  /**
   * Localizes the given resource location to one within the folder
   * @param path        Path to localize
   * @param folder      Folder to trim (without trailing /), it is not validated so make sure you call correctly
   * @param extension   Extension to trim
   * @return  Localized location
   */
  public static String localize(String path, String folder, String extension) {
    return path.substring(folder.length() + 1, path.length() - extension.length());
  }

  /**
   * Localizes the given resource location to one within the folder
   * @param location    Location to localize
   * @param folder      Folder to trim (without trailing /), it is not validated so make sure you call correctly
   * @param extension   Extension to trim
   * @return  Localized location
   */
  public static ResourceLocation localize(ResourceLocation location, String folder, String extension) {
    return new ResourceLocation(location.getNamespace(), localize(location.getPath(), folder, extension));
  }


  /* Block States */

  /**
   * Converts the given JSON element into a block state
   * @param element  Element to convert
   * @param key      Element key
   * @return  Block state
   * @throws JsonSyntaxException  if a property does not parse or the element is the wrong type
   */
  public static BlockState convertToBlockState(JsonElement element, String key) {
    return BlockStateLoadable.DIFFERENCE.convert(element, key);
  }

  /**
   * Converts the given JSON element into a block state
   * @param parent   Parent containing the block state
   * @param key      Element key
   * @return  Block state
   * @throws JsonSyntaxException  if a property does not parse or the element is missing or the wrong type
   */
  public static BlockState getAsBlockState(JsonObject parent, String key) {
    return BlockStateLoadable.DIFFERENCE.getIfPresent(parent, key);
  }

  /**
   * Converts the given JSON object into a block state
   * @param json  Json object containing "block" and "properties"
   * @return  Block state
   * @throws JsonSyntaxException  if any property name or property value is invalid
   */
  public static BlockState convertToBlockState(JsonObject json) {
    return BlockStateLoadable.DIFFERENCE.deserialize(json);
  }

  /**
   * Serializes the given block state to JSON, essentially writes all values that differ from the state.
   * @param state  State
   * @return  JsonPrimitive of the block name if it matches the default state, JsonObject otherwise
   */
  public static JsonElement serializeBlockState(BlockState state) {
    return BlockStateLoadable.DIFFERENCE.serialize(state);
  }

  /**
   * Serializes the given block state to JSON, essentially writes all values that differ from the state
   * @param state  State
   * @return  JsonObject containing properties that differ from the default state
   */
  public static JsonObject serializeBlockState(BlockState state, JsonObject json) {
    BlockStateLoadable.DIFFERENCE.serialize(state, json);
    return json;
  }
}
