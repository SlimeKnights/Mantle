// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/FakeSpawnInfo.java
package slimeknights.mantle.client.book.structure.level;

import net.minecraft.world.Difficulty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeLevelData implements WritableLevelData {

  private static final GameRules RULES = new GameRules();

  private BlockPos spawn = BlockPos.ZERO;
  private float spawnAngle;

  @Override
  public void setSpawn(BlockPos spawnPoint, float spawnAngle) {
    this.spawn = spawnPoint;
    this.spawnAngle = spawnAngle;
  }

  @Override
  public BlockPos getSpawnPos() {
    return this.spawn;
  }

  @Override
  public float getSpawnAngle() {
    return this.spawnAngle;
  }

  @Override
  public long getGameTime() {
    return 0;
  }

  @Override
  public long getDayTime() {
    return 0;
  }

  @Override
  public boolean isThundering() {
    return false;
  }

  @Override
  public boolean isRaining() {
    return false;
  }

  @Override
  public void setRaining(boolean isRaining) {

  }

  @Override
  public boolean isHardcore() {
    return false;
  }

  @Override
  public GameRules getGameRules() {
    return RULES;
  }

  @Override
  public Difficulty getDifficulty() {
    return Difficulty.PEACEFUL;
  }

  @Override
  public boolean isDifficultyLocked() {
    return false;
  }
}
