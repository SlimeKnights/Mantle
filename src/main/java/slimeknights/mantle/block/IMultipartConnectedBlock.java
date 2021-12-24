package slimeknights.mantle.block;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import slimeknights.mantle.client.model.connected.ConnectedModel;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used in {@link ConnectedModel} to workaround Forge #6841. Allows a multipart block like panes to have connected textures
 * @deprecated Only use if the block model uses multipart or weighted random. Ideally Forge will fix the bug and this will no longer be needed
 */
@Deprecated
@SuppressWarnings({"unused", "JavadocReference"})
public interface IMultipartConnectedBlock {
  /** Map of direction to boolean property for that direction */
  Map<Direction,BooleanProperty> CONNECTED_DIRECTIONS = Arrays.stream(Direction.values())
       .map(dir -> Pair.of(dir,BooleanProperty.create("connected_" + dir.getSerializedName())))
      .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (u,v) -> u, () -> new EnumMap<>(Direction.class)));

  /**
   * Applies false to all directions in the state, for use in {@link net.minecraft.block.Block#setDefaultState(BlockState)}
   * @param state  Original state
   * @return  State with all connections false
   */
  static BlockState defaultConnections(BlockState state) {
    for (BooleanProperty prop : CONNECTED_DIRECTIONS.values()) {
      state = state.setValue(prop, false);
    }
    return state;
  }

  /**
   * Fills a state container, for use in {@link Block#fillStateContainer(Builder)}
   * @param builder  State container builder
   */
  static void fillStateContainer(Builder<Block, BlockState> builder) {
    CONNECTED_DIRECTIONS.values().forEach(builder::add);
  }

  /**
   * Checks if the block connects to the given neighbor
   * @param state     State to check
   * @param neighbor  Neighbor to check
   * @return  True if the block connects
   */
  default boolean connects(BlockState state, BlockState neighbor) {
    return state.getBlock() == neighbor.getBlock();
  }

  /**
   * Gets the new connected state based on the given block update
   * @param state     Current state
   * @param facing    Side that updated
   * @param neighbor  Block on the side
   * @return  Updated block state
   */
  default BlockState getConnectionUpdate(BlockState state, Direction facing, BlockState neighbor) {
    return state.setValue(CONNECTED_DIRECTIONS.get(facing), connects(state, neighbor));
  }
}
