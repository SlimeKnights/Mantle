package mantle.internal;

import mantle.Mantle;
import net.minecraftforge.common.Configuration;

public class Config {

    private Config() {} // Singleton

    public static void loadConfig(Configuration conf) {
        conf.load();

        // TODO: Load/Save config vars here

        conf.save();
        Mantle.logger.info("Configuration load complete.");
    }

    // TODO: Config vars here

}
