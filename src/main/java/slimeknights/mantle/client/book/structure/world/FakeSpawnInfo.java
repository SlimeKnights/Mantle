// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.16.5/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/FakeSpawnInfo.java
package slimeknights.mantle.client.book.structure.world;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.ISpawnWorldInfo;

public class FakeSpawnInfo implements ISpawnWorldInfo {

  private static final GameRules RULES = new GameRules();

  private int spawnX;
  private int spawnY;
  private int spawnZ;
  private float spawnAngle;

  @Override
  public void setSpawnX(int x) {
    this.spawnX = x;
  }

  @Override
  public void setSpawnY(int y) {
    this.spawnY = y;
  }

  @Override
  public void setSpawnZ(int z) {
    this.spawnZ = z;
  }

  @Override
  public void setSpawnAngle(float angle) {
    this.spawnAngle = angle;
  }

  @Override
  public int getSpawnX() {
    return this.spawnX;
  }

  @Override
  public int getSpawnY() {
    return this.spawnY;
  }

  @Override
  public int getSpawnZ() {
    return this.spawnZ;
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
  public GameRules getGameRulesInstance() {
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
