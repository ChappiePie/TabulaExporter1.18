package chappie.tabulamcexporter;

import me.ichun.mods.tabula.client.export.ExportList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("tabulamcexporter")
public class TabulaExporterMod {
    public static final ExportMinecraftJava EXPORTER = new ExportMinecraftJava();

    public TabulaExporterMod() {
        ExportList.EXPORTERS.put(EXPORTER.getId(), EXPORTER);
    }
}
