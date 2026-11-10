package net.emeraude.eliteenemies.fabric;

import net.emeraude.eliteenemies.EliteEnemies;
import net.fabricmc.api.ModInitializer;

/** Fabric entry point (both sides). All logic is loader-agnostic in {@link EliteEnemies}. */
public final class EliteEnemiesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EliteEnemies.init();
    }
}
