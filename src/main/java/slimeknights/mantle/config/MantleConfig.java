package slimeknights.mantle.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import slimeknights.mantle.Mantle;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for all Mantle specific config options
 */
@Config(name = Mantle.modId)
public class MantleConfig implements ConfigData {

	/** If true, enables the heart renderer */
	@Comment("If true, enables the Mantle heart renderer, which stacks hearts by changing the color instead of vertically stacking them.\n" +
					"Mod authors: this config is not meant for compatibility with your heart renderer, cancel the RenderGameOverlayEvent.Pre event and our logic won't run")
	public boolean renderExtraHeartsColored;

	/** List of preferences for tag outputs */
	@Comment("Preferences for outputs from tags used in automatic compat in recipes")
	public List<String> tagPreferences;
}
