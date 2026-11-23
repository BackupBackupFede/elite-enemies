package net.emeraude.eliteenemies.neoforge;

import net.emeraude.eliteenemies.EliteEnemies;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge entry point (both sides). All logic is loader-agnostic in {@link EliteEnemies}. */
@Mod(EliteEnemies.MOD_ID)
public final class EliteEnemiesNeoForge {

    public EliteEnemiesNeoForge(IEventBus modBus, ModContainer container) {
        EliteEnemies.init(FMLPaths.CONFIGDIR.get());
    }
}
