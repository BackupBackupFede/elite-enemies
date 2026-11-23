package net.emeraude.eliteenemies.mixin;

import net.emeraude.eliteenemies.EliteEnemiesSelfTest;
import net.emeraude.eliteenemies.Elites;
import net.emeraude.eliteenemies.SpawnReasonHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Every freshly spawned mob passes here once it is in the world, with its own gear already on.
 * Entities loaded from disk do not: an elite is made at spawn time only.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void eliteenemies$spawned(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EliteEnemiesSelfTest.saw(entity);
        if (entity instanceof Mob mob && entity instanceof SpawnReasonHolder holder) {
            String reason = holder.eliteenemies$takeReason();
            if (cir.getReturnValueZ() && reason != null) Elites.onSpawn(mob, reason, (ServerLevel) (Object) this);
        }
    }
}
