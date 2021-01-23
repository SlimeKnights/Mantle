package slimeknights.mantle.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for all Mantle specific config options
 */
public class Config {
	/** List of all cached client config values, for cache clearing */
	private static final List<CachedValue<?>> CLIENT_VALUES = new ArrayList<>();
	private static final List<CachedValue<?>> SERVER_VALUES = new ArrayList<>();

	/** If true, enables the heart renderer */
	public static final CachedBoolean EXTRA_HEART_RENDERER;

	/** List of preferences for tag outputs */
	public static final CachedValue<List<String>> TAG_PREFERENCES;

	private static final ForgeConfigSpec.Builder CLIENT_BUILDER, SERVER_BUILDER;
	public static final ForgeConfigSpec CLIENT_SPEC, SERVER_SPEC;

	static {
		CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		SERVER_BUILDER = new ForgeConfigSpec.Builder();

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

		CLIENT_SPEC = CLIENT_BUILDER.build();
		SERVER_SPEC = SERVER_BUILDER.build();
	}

	/**
	 * Creates a cached boolean value for the client and adds it to the list to be invalidated on reload
	 * @param function  Lambda to get config value
	 * @return  Cached config value
	 */
	private static CachedBoolean client(Function<Builder,BooleanValue> function) {
		CachedBoolean cached = new CachedBoolean(function.apply(CLIENT_BUILDER));
		CLIENT_VALUES.add(cached);
		return cached;
	}

	/**
	 * Creates a cached boolean value for the client and adds it to the list to be invalidated on reload
	 * @param function  Lambda to get config value
	 * @return  Cached config value
	 */
	private static <T> CachedValue<T> server(Function<Builder,ConfigValue<T>> function) {
		CachedValue<T> cached = new CachedValue<>(function.apply(SERVER_BUILDER));
		SERVER_VALUES.add(cached);
		return cached;
	}

	/**
	 * Event to call when the config changes
	 * @param configEvent  Config event
	 */
	public static void configChanged(final ModConfig.ModConfigEvent configEvent) {
		ModConfig config = configEvent.getConfig();
		if (config.getModId().equals(Mantle.modId)) {
			ForgeConfigSpec spec = config.getSpec();
			if (spec == Config.CLIENT_SPEC) {
				CLIENT_VALUES.forEach(CachedValue::invalidate);
			} else if (spec == Config.SERVER_SPEC) {
				SERVER_VALUES.forEach(CachedValue::invalidate);
			}
		}
	}
}
