package io.github.guy7cc.savestructurenbt;

import com.mojang.logging.LogUtils;
import io.github.guy7cc.savestructurenbt.command.SaveStructureCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SaveStructureNBT.MODID)
public class SaveStructureNBT
{
    public static final String MODID = "savestructurenbt";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SaveStructureNBT()
    {
        MinecraftForge.EVENT_BUS.addListener(SaveStructureCommand::register);
    }
}
