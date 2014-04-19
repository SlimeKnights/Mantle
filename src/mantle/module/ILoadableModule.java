package mantle.module;

/**
 * Interface for modules for dealing with other mods.
 *
 * Note that all ILoadableModule's should have a static String field "modId" which contains the mod ID of the mod it supports.
 * This is accessed by reflection because Java is dumb and has no statics in interfaces.
 *
 * @author Arkan <arkan@emberwalker.cc>
 */
public interface ILoadableModule {

    // Example modId field
    //public static String modId = "ExampleMod";

    // Called during PreInit
    public abstract void preInit();

    // Called during Init
    public abstract void init();

    // Called during PostInit
    public abstract void postInit();

}