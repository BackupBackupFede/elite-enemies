package net.emeraude.eliteenemies.mixin;

import net.emeraude.eliteenemies.EliteTracker;
import net.emeraude.eliteenemies.Elites;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps a young elite from despawning, and picks up elites loaded from disk for the boss bar. */
@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void eliteenemies$stay(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (Elites.protectedFromDespawn(self)) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void eliteenemies$track(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (self.tickCount % 20 == 0 && self.level() instanceof ServerLevel && Elites.rarityOf(self) != null) {
            EliteTracker.track(self);
        }
    }
}
