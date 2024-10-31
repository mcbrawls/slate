package net.mcbrawls.slate.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.mcbrawls.slate.Slate;
import net.mcbrawls.slate.SlatePlayer;
import net.mcbrawls.slate.screen.SlateScreenHandler;
import net.mcbrawls.slate.screen.SlateScreenHandlerFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements SlatePlayer {
    @Shadow public abstract void onHandledScreenClosed();

    @Shadow public abstract void closeHandledScreen();

    @Unique
    private boolean ignoreNextClosePacket = false;

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "openHandledScreen", at = @At("HEAD"))
    private void detectScreenOpen(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        if (factory instanceof SlateScreenHandlerFactory && this.currentScreenHandler != this.playerScreenHandler) {
            this.ignoreNextClosePacket = true;
        }
    }

    @WrapOperation(
            method = "closeHandledScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
            )
    )
    private void preventClientScreenClosure(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
        if (this.ignoreNextClosePacket) {
            this.ignoreNextClosePacket = false;
            return;
        }

        original.call(instance, packet);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void closeSlateOnDeath(DamageSource source, CallbackInfo ci) {
        if (this.currentScreenHandler instanceof SlateScreenHandler) {
            this.closeHandledScreen();
        }
    }

    @Override
    public boolean openSlate(Slate slate) {
        return slate.open((ServerPlayerEntity) (Object) this);
    }

    @Override
    public @Nullable Slate getSlate() {
        SlateScreenHandler<?> screenHandler = this.getSlateScreenHandler();
        return screenHandler == null ? null : screenHandler.getSlate();
    }

    @Override
    public @Nullable SlateScreenHandler<?> getSlateScreenHandler() {
        if (this.currentScreenHandler instanceof SlateScreenHandler<?> handler) {
            return handler;
        }

        return null;
    }

    @Override
    public boolean hasSlateOpen() {
        return this.getSlateScreenHandler() != null;
    }
}
