package net.emeraude.eliteenemies.mixin;

import net.emeraude.eliteenemies.SpawnReasonHolder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Remembers why a mob is being spawned (natural, spawner, egg...). The mob only becomes an elite
 * once it is in the world, in {@link ServerLevelMixin}: by then every subclass has given it its
 * own gear, which would otherwise overwrite the elite's. 26.x: the reason is an EntitySpawnReason.
 */
@Mixin(Mob.class)
public abstract class MobSpawnReasonMixin implements SpawnReasonHolder {

    @Unique
    private String eliteenemies$reason;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void eliteenemies$remember(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                       SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        eliteenemies$reason = reason.name();
    }

    @Override
    public String eliteenemies$takeReason() {
        String reason = eliteenemies$reason;
        eliteenemies$reason = null;
        return reason;
    }
}
