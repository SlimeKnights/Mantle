package slimeknights.mantle.config;

import me.shedaniel.autoconfig.annotation.Config;
import slimeknights.mantle.Mantle;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for all Mantle specific config options
 */
@Config(name = Mantle.modId)
public class MantleConfig {

	/** If true, enables the heart renderer */
	public static final boolean EXTRA_HEART_RENDERER;

	/** List of preferences for tag outputs */
	public static final List<String> TAG_PREFERENCES;

	static {
		// client options
		EXTRA_HEART_RENDERER = client(builder ->
			builder.comment(
					"If true, enables the Mantle heart renderer, which stacks hearts by changing the color instead of vertically stacking them.",
					"Mod authors: this config is not meant for compatibility with your heart renderer, cancel the RenderGameOverlayEvent.Pre event and our logic won't run")
						 .translation("config.mantle.extraHeartRenderer")
						 .define("extraHeartRenderer", true));

		// server options
		TAG_PREFERENCES = server(builder ->
				builder.comment("Preferences for outputs from tags used in automatic compat in recipes")
				.translation("config.mantle.tagPreferences")
				.define("tagPreferences", Arrays.asList("minecraft", "tconstruct", "tmechworks", "immersiveengineering", "mekanism", "thermalfoundation", "thermalexpansion")));

	}
}
