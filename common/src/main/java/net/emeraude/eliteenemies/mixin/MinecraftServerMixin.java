package net.emeraude.eliteenemies.mixin;

import net.emeraude.eliteenemies.EliteTracker;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/** Updates the elites' boss bars, a few times a second. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void eliteenemies$tick(BooleanSupplier haveTime, CallbackInfo ci) {
        EliteTracker.tick((MinecraftServer) (Object) this);
    }
}
