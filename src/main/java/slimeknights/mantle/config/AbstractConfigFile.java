package slimeknights.mantle.config;

import com.google.common.reflect.TypeToken;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

public abstract class AbstractConfigFile implements Serializable {

  private static boolean initialized = false;

  File file;
  ConfigurationLoader<CommentedConfigurationNode> loader;
  // default value is TRUE since most people will forget to set it to true when it needs saving anyway
  // if you want to use it, set it to false in your constructor or something ;o
  private boolean needsSaving = true;

  @Setting("_VERSION")
  private int configVersion;

  // required constructor for deserialization
  public AbstractConfigFile() {
    file = null;
    loader = null;
  }

  public AbstractConfigFile(File configFolder, String name) {
    this(new File(configFolder, name + ".cfg"));
  }

  public AbstractConfigFile(File configFile) {
    configFile.getParentFile().mkdirs();
    file = configFile;
    loader = HoconConfigurationLoader.builder().setFile(file).build();
  }

  public CommentedConfigurationNode load() throws IOException {
    return loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
  }

  public void save(ConfigurationNode node) throws IOException {
    loader.save(node);
  }

  public String getName() {
    return file.getName();
  }

  public abstract void insertDefaults();

  /**
   * Return a number for the config version.
   * If the version number differs from the one in the file the file will be updated.
   * So if you add new fields etc. update the number
   */
  protected abstract int getConfigVersion();

  void setConfigVersion() {
    if(configVersion != getConfigVersion()) {
      configVersion = getConfigVersion();
      setNeedsSaving();
    }
  }


  public AbstractConfigFile loadFromPacket(byte[] packetData) {
    ConfigurationLoader<CommentedConfigurationNode> packetDataLoader = HoconConfigurationLoader
        .builder()
        .setSource(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(packetData))))
        .setSink(AtomicFiles.createAtomicWriterFactory(file.toPath(), StandardCharsets.UTF_8))
        .build();

    try {
      CommentedConfigurationNode node = packetDataLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

      try {
        return node.getValue(TypeToken.of(this.getClass()));
      } catch(ObjectMappingException e) {
        e.printStackTrace();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
    if(type != AbstractConfigFile.class) {
      fields.addAll(Arrays.asList(type.getDeclaredFields()));

      if(type.getSuperclass() != null && AbstractConfigFile.class.isAssignableFrom(type.getSuperclass())) {
        fields = getAllFields(fields, type.getSuperclass());
      }
    }

    return fields;
  }

  public byte[] getPacketData() {
    try {
      return Files.readAllBytes(file.toPath());
    } catch(IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean sync(AbstractConfigFile other) {
    return sync(other, this);
  }

  public boolean sync(Object other, Object that) {
    if(other.getClass() != that.getClass()) {
      return false;
    }

    List<Field> fieldsToProcess = new ArrayList<>();
    getAllFields(fieldsToProcess, that.getClass());

    for(Field field : fieldsToProcess) {
      syncField(other, field);
    }

    return needsSaving();
  }

  private void syncField(Object other, Field field) {
    try {
      // don't sync transient fields
      if(Modifier.isTransient(field.getModifiers())) {
        return;
      }
      if(Modifier.isStatic(field.getModifiers())) {
        return;
      }
      if(!field.isAccessible()) {
        field.setAccessible(true);
      }

      Object original = field.get(this);
      Object remote = field.get(other);

      // is this a subclass that contains entries itself?
      if(field.getClass().isAnnotationPresent(ConfigSerializable.class)) {
        sync(remote, original);
      }
      else {
        if(!original.equals(remote)) {
          field.set(this, remote);
          setNeedsSaving();
        }
      }
    } catch(IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public void setNeedsSaving() {
    needsSaving = true;
  }

  public boolean needsSaving() {
    return needsSaving;
  }

  public void clearNeedsSaving() {
    needsSaving = false;
  }

  public static void init() {
    if(initialized) {
      return;
    }

    // item and block serializer/deserializer
    TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Block.class), new RegistrySerializer<Block>() {
      @Override
      IForgeRegistry<Block> getRegistry() {
        return GameData.getBlockRegistry();
      }
    });

    TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Item.class), new RegistrySerializer<Item>() {
      @Override
      IForgeRegistry<Item> getRegistry() {
        return GameData.getItemRegistry();
      }
    });

    TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(BlockMeta.class), BlockMeta.SERIALIZER);

    initialized = true;
  }

  private static abstract class RegistrySerializer<T extends IForgeRegistryEntry<T>> implements TypeSerializer<T> {

    // done at runtime so registry changes from joining servers take effect
    abstract IForgeRegistry<T> getRegistry();

    @Override
    public T deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
      return getRegistry().getValue(new ResourceLocation(configurationNode.getString()));
    }

    @Override
    public void serialize(TypeToken<?> typeToken, T t, ConfigurationNode configurationNode)
        throws ObjectMappingException {
      configurationNode.setValue(t.getRegistryName());
    }
  }
}
