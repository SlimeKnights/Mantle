package slimeknights.mantle.client.model.connected;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Place to register data related to connected block models
 */
@SuppressWarnings("WeakerAccess")
public class ConnectedModelRegistry {
  /* Predicates */

  /** Default state predicate, compares the blocks for equality */
  private static final BiPredicate<BlockState,BlockState> BLOCK_CONNECTION_PREDICATE = (s1, s2) -> s1.getBlock() == s2.getBlock();
  /** Map of name to connection type. Allows registering custom logic to connect two blocks together, used for glass panes for instance */
  private static final Map<String,BiPredicate<BlockState,BlockState>> CONNECTION_PREDICATES = new HashMap<>();

  /**
   * Registers a new connection type
   * @param name       Name for the type
   * @param predicate  Connection logic to compare two blockstates for equality
   */
  public static void registerPredicate(String name, BiPredicate<BlockState,BlockState> predicate) {
    CONNECTION_PREDICATES.putIfAbsent(name, predicate);
  }

  /**
   * Gets the connection predicate for the given JSON
   * @param json  JsonObject parent
   * @param key   Predicate key
   * @return  Connection predicate
   */
  public static BiPredicate<BlockState,BlockState> deserializePredicate(JsonObject json, String key) {
    String name = GsonHelper.getAsString(json, key, "block");
    if (!CONNECTION_PREDICATES.containsKey(name)) {
      throw new JsonSyntaxException("Unknown connection predicate " + name);
    }
    return CONNECTION_PREDICATES.get(name);
  }

  /**
   * Gets a single predicate by name
   * @param name  Predicate name
   * @return  Predicate
   */
  @SuppressWarnings("unused")
  public static BiPredicate<BlockState,BlockState> getPredicate(String name) {
    return CONNECTION_PREDICATES.getOrDefault(name, BLOCK_CONNECTION_PREDICATE);
  }

  /**
   * Gets a property from the state if it exists
   * @param state  State with property
   * @param prop   Property to get
   * @return  True if the property exists and is true, false if false or missing
   */
  private static boolean safeGet(BlockState state, BooleanProperty prop) {
    return state.hasProperty(prop) && state.getValue(prop);
  }

  static {
    // connect to block
    registerPredicate("block", BLOCK_CONNECTION_PREDICATE);
    // smarter connections for panes
    registerPredicate("pane", (state, neighbor) -> {
      // must be the same block, and either both blocks must be center only, or neither are center only
      return state.getBlock() == neighbor.getBlock()
             && (safeGet(state, PipeBlock.NORTH) || safeGet(state, PipeBlock.EAST) || safeGet(state, PipeBlock.SOUTH) || safeGet(state, PipeBlock.WEST))
                == (safeGet(neighbor, PipeBlock.NORTH) || safeGet(neighbor, PipeBlock.EAST) || safeGet(neighbor, PipeBlock.SOUTH) || safeGet(neighbor, PipeBlock.WEST));

    });
  }


  /* Texture mapping */

  /** Map of name to connection types */
  private static final Map<String,String[]> CONNECTION_TYPES = new HashMap<>();

  /**
   * Registers a connection type
   * @param name    Type name
   * @param mapper  Function of predicate to texture name. Predicate will match NSWE, signifying the texture connects UDLR
   *                Only called during registration, results are cached into the 16 variants
   */
  public static void registerType(String name, Function<Predicate<Direction>,String> mapper) {
    if (!CONNECTION_TYPES.containsKey(name)) {
      String[] suffixes = new String[16];
      for (int i = 0; i < 16; i++) {
        final int index = i;
        suffixes[i] = mapper.apply((dir) -> {
          int flag = 1 << dir.get2DDataValue();
          return (index & flag) == flag;
        });
      }
      CONNECTION_TYPES.put(name, suffixes);
    }
  }

  /**
   * Gets the connection type from the given JSON
   * @param json  JsonObject parent
   * @param key   Predicate key
   * @return  Connection predicate
   */
  public static String[] deserializeType(JsonElement json, String key) {
    String name = GsonHelper.convertToString(json, key);
    if (!CONNECTION_TYPES.containsKey(name)) {
      throw new JsonSyntaxException("Unknown connection type " + name);
    }
    return CONNECTION_TYPES.get(name);
  }

  static {
    // connects on all four sides
    registerType("cornerless_full", predicate -> {
      String name = "";
      if (predicate.test(Direction.NORTH)) name += "u";
      if (predicate.test(Direction.SOUTH)) name += "d";
      if (predicate.test(Direction.WEST)) name += "l";
      if (predicate.test(Direction.EAST)) name += "r";
      return name;
    });
    // connects horizontally
    registerType("horizontal", predicate -> {
      boolean right = predicate.test(Direction.EAST);
      if (predicate.test(Direction.WEST)) {
        return right ? "middle" : "right";
      }
      return right ? "left" : "";
    });
    // connects vertically
    registerType("vertical", predicate -> {
      boolean bottom = predicate.test(Direction.SOUTH);
      if (predicate.test(Direction.NORTH)) {
        return bottom ? "middle" : "bottom";
      }
      return bottom ? "top" : "";
    });
    // connects to just the top block
    registerType("top", predicate -> predicate.test(Direction.NORTH) ? "bottom" : "");
  }
}
