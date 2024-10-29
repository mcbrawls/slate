package net.mcbrawls.slate.mixin;

import net.mcbrawls.slate.Slate;
import net.mcbrawls.slate.SlatePlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements SlatePlayer {
    @Override
    public boolean openSlate(Slate slate) {
        return Slate.Companion.openSlate(slate, (ServerPlayerEntity) (Object) this);
    }
}
