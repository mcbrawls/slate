package net.mcbrawls.slate.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mcbrawls.slate.Slate;
import net.mcbrawls.slate.screen.SlateScreenHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
        if (screenHandler instanceof SlateScreenHandler handler) {
            Slate slate = handler.getSlate();
            if (!slate.getCanBeClosed()) {
                NetworkThreadUtils.forceMainThread(packet, that, this.player.getServerWorld());

                // reopen for client
                ScreenHandlerType<?> type = screenHandler.getType();
                OpenScreenS2CPacket openPacket = new OpenScreenS2CPacket(screenHandler.syncId, type, slate.getTitle());
                this.sendPacket(openPacket);
                screenHandler.syncState();

                return;
            }
        }

        original.call(packet);
    }
}
