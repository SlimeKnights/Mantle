package slimeknights.mantle.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for all Mantle specific config options
 */
@Internal
public class Config {
  public enum HeartRenderer {
    DISABLE, NO_MAX, WITH_MAX
  }

	/** Heart renderer mode */
  public static final EnumValue<HeartRenderer> HEART_RENDERER;

	/** List of preferences for tag outputs */
	private static final List<String> DEFAULT_TAG_PREFERENCES = Arrays.asList("minecraft", "tconstruct", "tmechworks", "metalborn", "embers", "create", "immersiveengineering", "mekanism", "thermal");
	public static final ConfigValue<List<? extends String>> TAG_PREFERENCES;

	public static final ForgeConfigSpec CLIENT_SPEC, SERVER_SPEC;

	static {
    ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
    ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();

		// client options
    HEART_RENDERER = client
      .comment(
        "If not DISABLE, enables the Mantle heart renderer, which stacks hearts by changing the color instead of vertically stacking them.",
        "WITH_MAX will show the max health in colored containers behind the health bar. NO_MAX will show just the health bar",
        "If DISABLE, uses the Forge heart renderer.",
        "Mod authors: this config is not meant for compatibility with your heart renderer, cancel the RenderGameOverlayEvent.Pre event and our logic won't run")
      .translation("config.mantle.extraHeartRenderer")
      .defineEnum("heartRenderer", HeartRenderer.WITH_MAX);

		// server options
		TAG_PREFERENCES = server.comment("Preferences for outputs from tags used in automatic compat in recipes")
                            .translation("config.mantle.tagPreferences")
                            .defineList("tagPreferences", DEFAULT_TAG_PREFERENCES, str -> true);

		CLIENT_SPEC = client.build();
		SERVER_SPEC = server.build();
	}
}
