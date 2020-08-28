package ru.ijo42.uplink;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import ru.ijo42.uplink.api.ForgeAPI;
import ru.ijo42.uplink.api.UplinkAPI;
import ru.ijo42.uplink.api.util.NativeUtil;

import java.nio.file.Path;

@Mod(
        modid = Uplink.MOD_ID,
        name = Uplink.MOD_NAME,
        version = Uplink.VERSION
)
public class Uplink {

    public static final String MOD_ID = "uplink";
    public static final String MOD_NAME = "Uplink";
    public static final String VERSION = "@VERSION@";
    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static Uplink INSTANCE;

    static {
        NativeUtil.loadNativeLibrary();
    }

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PresenceListener listener = new PresenceListener();
        UplinkAPI.init(new ForgeAPI() {
            @Override
            public int getModsCount() {
                return Loader.instance().getModList().size();
            }

            @Override
            public int getPlayerCount() {
                return Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().size();
            }

            @Override
            public int getMaxPlayers() {
                return Minecraft.getMinecraft().getNetHandler().currentServerMaxPlayers;
            }

            @Override
            public String getIGN() {
                return Minecraft.getMinecraft().getSession().getUsername();
            }

            @Override
            public Path getConfigDir() {
                return event.getModConfigurationDirectory().toPath();
            }

            @Override
            public boolean isMP() {
                return Minecraft.getMinecraft().getCurrentServerData() != null;
            }

            @Override
            public String getServerIP() {
                return Minecraft.getMinecraft().getCurrentServerData().serverIP;
            }

            @Override
            public String getWorldName() {
                return Minecraft.getMinecraft().getIntegratedServer().getWorldName();
            }
        }, event.getModLog(), listener);
        MinecraftForge.EVENT_BUS.register(listener);
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        LogManager.getLogger("Uplink").error("Invalid fingerprint detected! The file " + event.source.getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }

}