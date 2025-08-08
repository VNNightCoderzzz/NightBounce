package net.ccbluex.liquidbounce.injection.forge.mixins.tweaks;

import io.netty.buffer.ByteBuf;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

        @ModifyVariable(
            method = "addFaviconToStatusResponse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ServerStatusResponse;setFavicon(Ljava/lang/String;)V", shift = At.Shift.AFTER),
            ordinal = 1
    )
    private ByteBuf releaseFaviconByteBuf(ByteBuf byteBuf) {
        try {
            ClientUtils.INSTANCE.getLOGGER().info("Releasing favicon ByteBuf: {}", byteBuf);
        } catch (Exception e) {
            ClientUtils.INSTANCE.getLOGGER().error("Error occurred during favicon ByteBuf release", e);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
        return byteBuf;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            ClientUtils.INSTANCE.getLOGGER().info("{} mixin successfully loaded!", getClass().getSimpleName());
        } catch (Exception e) {
            ClientUtils.INSTANCE.getLOGGER().error("Failed to load {} mixin: {}", getClass().getSimpleName(), e.getMessage());
        }
    }
}