package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {


    @Inject(method = "method_60440", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;save(Lnet/minecraft/world/chunk/Chunk;)Z",
            shift = At.Shift.AFTER))
    private void onChunkUnload(ChunkHolder chunkHolder, long l, CallbackInfo ci, @Local Chunk chunk) {
        //ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, chunk);
        Freeable freeable = chunk;
        freeable.free();
    }

}
