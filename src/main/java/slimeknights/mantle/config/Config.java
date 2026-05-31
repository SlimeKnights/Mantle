package slimeknights.mantle.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
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

  /** If true, enables the fluid fog fix. If false, disables it for better shader compatability. */
  public static final BooleanValue ENABLE_FLUID_FOG_FIX;

	/** List of preferences for tag outputs */
	private static final List<String> DEFAULT_TAG_PREFERENCES = Arrays.asList("minecraft", "tconstruct", "tmechworks", "metalborn", "embers", "create", "immersiveengineering", "mekanism", "thermal");
	public static final ConfigValue<List<? extends String>> TAG_PREFERENCES;

	public static final ModConfigSpec CLIENT_SPEC, SERVER_SPEC;

	static {
    ModConfigSpec.Builder client = new ModConfigSpec.Builder();
    ModConfigSpec.Builder server = new ModConfigSpec.Builder();

		// client options
    HEART_RENDERER = client
      .comment(
        "If not DISABLE, enables the Mantle heart renderer, which stacks hearts by changing the color instead of vertically stacking them.",
        "WITH_MAX will show the max health in colored containers behind the health bar. NO_MAX will show just the health bar",
        "If DISABLE, uses the Forge heart renderer.",
        "Mod authors: this config is not meant for compatibility with your heart renderer, cancel the RenderGameOverlayEvent.Pre event and our logic won't run")
      .translation("config.mantle.extraHeartRenderer")
      .defineEnum("heartRenderer", HeartRenderer.WITH_MAX);

    ENABLE_FLUID_FOG_FIX = client
      .comment(
        "If true, fluids properly have their lighting adjusted under vanilla fog effects such as blindness. If false, they render as nearly fullbright ignoring fog.",
        "This config option is provided as the fix breaks shaders, and slightly broken is better than fully broken.",
        "Best fix is to fix your shaders though, so you can have no broken visuals.")
      .translation("config.mantle.enableFluidFogFix")
      .define("enableFluidFogFix", true);

		// server options
		TAG_PREFERENCES = server.comment("Preferences for outputs from tags used in automatic compat in recipes")
                            .translation("config.mantle.tagPreferences")
                            .defineList("tagPreferences", DEFAULT_TAG_PREFERENCES, str -> true);

		CLIENT_SPEC = client.build();
		SERVER_SPEC = server.build();
	}
}
