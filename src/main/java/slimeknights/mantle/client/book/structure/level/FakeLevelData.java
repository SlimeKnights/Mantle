// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/FakeSpawnInfo.java
package slimeknights.mantle.client.book.structure.level;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;

/** Fake level data for {@link TemplateLevel} */
class FakeLevelData implements WritableLevelData {
  public static final FakeLevelData INSTANCE = new FakeLevelData();

  private FakeLevelData() {}

  @Getter
  private final GameRules gameRules = new GameRules();

  @Getter
  private BlockPos spawnPos = BlockPos.ZERO;
  @Getter
  private float spawnAngle;

  @Override
  public void setSpawn(BlockPos blockPos, float angle) {
    this.spawnPos = blockPos;
    this.spawnAngle = angle;
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
  public void setRaining(boolean isRaining) {}

  @Override
  public boolean isHardcore() {
    return false;
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
