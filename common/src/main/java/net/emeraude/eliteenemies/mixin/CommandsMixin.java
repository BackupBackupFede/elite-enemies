package net.emeraude.eliteenemies.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.emeraude.eliteenemies.EliteCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers /eliteenemies once the dispatcher is built, on both loaders, with no Fabric API. */
@Mixin(Commands.class)
public abstract class CommandsMixin {

    @Shadow
    public abstract CommandDispatcher<CommandSourceStack> getDispatcher();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void eliteenemies$register(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        EliteCommands.register(getDispatcher());
    }
}
