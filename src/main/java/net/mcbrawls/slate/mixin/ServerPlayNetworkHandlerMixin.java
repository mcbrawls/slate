package net.mcbrawls.slate.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mcbrawls.slate.InventorySlate;
import net.mcbrawls.slate.Slate;
import net.mcbrawls.slate.SlateListeners;
import net.mcbrawls.slate.screen.SlateScreenHandler;
import net.mcbrawls.slate.screen.slot.ClickType;
import net.mcbrawls.slate.screen.slot.TileClickContext;
import net.mcbrawls.slate.tile.Tile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow public ServerPlayerEntity player;

    private ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @WrapMethod(method = "onCloseHandledScreen")
    private void handleScreenClose(CloseHandledScreenC2SPacket packet, Operation<Void> original) {
        ServerPlayNetworkHandler that = (ServerPlayNetworkHandler) (Object) this;

        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof SlateScreenHandler<?> handler) {
            Slate slate = handler.getSlate();
            if (!slate.getCanPlayerClose()) {
                NetworkThreadUtils.forceMainThread(packet, that, this.player.getServerWorld());

                if (slate.getCanBeClosed()) {
                    // reopen for client
                    ScreenHandlerType<?> type = screenHandler.getType();
                    OpenScreenS2CPacket openPacket = new OpenScreenS2CPacket(screenHandler.syncId, type, slate.getTitle());
                    this.sendPacket(openPacket);
                    screenHandler.syncState();
                }

                return;
            }
        }

        original.call(packet);
    }

    @Inject(method = "onRenameItem", at = @At("TAIL"))
    private void handleAnvilInput(RenameItemC2SPacket packet, CallbackInfo ci) {
        if (this.player.currentScreenHandler instanceof SlateScreenHandler<?> handler) {
            String input = packet.getName();
            handler.onAnvilInput(input);
            handler.syncState();
        }
    }

    @Inject(method = "onHandSwing", at = @At("TAIL"))
    private void handleHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = this.player;
        SlateListeners.INSTANCE.onSwing$slate(player, packet.getHand());
    }

    @Inject(method = "onPlayerInteractItem", at = @At("RETURN"))
    private void handleClientItemUse(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = this.player;
        SlateListeners.INSTANCE.onUse$slate(player, packet.getHand());
    }

    @WrapOperation(
            method = "onPlayerAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;dropSelectedItem(Z)Z"
            )
    )
    private boolean handleDrop(ServerPlayerEntity player, boolean entireStack, Operation<Boolean> original) {
        if (this.player.currentScreenHandler instanceof SlateScreenHandler<?> handler) {
            Slate slate = handler.getSlate();
            if (slate instanceof InventorySlate inventorySlate) {
                SlateListeners listeners = SlateListeners.INSTANCE;
                Tile tile = listeners.getSelectedSlotTile(inventorySlate, player);
                int button = 0;
                SlotActionType actionType = SlotActionType.THROW;
                TileClickContext context = new TileClickContext(tile, button, actionType, ClickType.Companion.parse(button, actionType), listeners.getClickModifiers(player), player, false);
                slate.onSlotClicked(context);
                return false;
            }
        }

        return original.call(player, entireStack);
    }
}
