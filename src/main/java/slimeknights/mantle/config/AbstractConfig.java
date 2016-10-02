package slimeknights.mantle.config;

import com.google.common.reflect.TypeToken;

import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public abstract class AbstractConfig {

  List<AbstractConfigFile> configFileList = new ArrayList<>();
  private List<Function<?, ?>> configFiles = new ArrayList<>();

  public void save() {
    configFiles.forEach(function -> function.apply(null));
  }

  public <T extends AbstractConfigFile> T load(T configFile, Class<T> clazz) {
    try {
      CommentedConfigurationNode node = configFile.load();

      T val = node.getValue(TypeToken.of(clazz), configFile);
      val.loader = configFile.loader;
      val.file = configFile.file;
      val.insertDefaults();

      configFileList.add(val);
      // note: this is a workaround for generics because the generic info has to match the same class
      configFiles.add(configFile1 -> {
        this.save(val, clazz);
        return true;
      });

      return val;
    } catch(IOException | ObjectMappingException e) {
      e.printStackTrace();
    }
    return configFile;
  }

  public <T extends AbstractConfigFile> void save(T configFile, Class<T> clazz) {
    if(configFile.needsSaving()) {
      try {
        CommentedConfigurationNode node = configFile.load();

        node.setValue(TypeToken.of(clazz), configFile);
        configFile.save(node);
      } catch(IOException | ObjectMappingException e) {
        e.printStackTrace();
      }
    }
    configFile.clearNeedsSaving();
  }

  /**
   *   syncs the data to the current config
   *   returns true if data was changed
   */
  public static boolean syncConfig(AbstractConfig config, List<AbstractConfigFile> files) {
    boolean changed = false;

    if(config.configFileList.size() != files.size()) {
      return false;
    }

    Iterator<AbstractConfigFile> iterLocal = config.configFileList.iterator();
    Iterator<AbstractConfigFile> iterRemote = files.iterator();

    while(iterLocal.hasNext() && iterRemote.hasNext()) {
      changed |= iterLocal.next().sync(iterRemote.next());
    }

    if(changed) {
      config.save();
    }
    return changed;
  }
}
