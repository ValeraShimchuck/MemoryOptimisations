package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkManager.ClientChunkMap.class)
public class ClientChunkMapMixin {

    @Shadow private int loadedChunkCount;

    @Shadow @Final public AtomicReferenceArray<WorldChunk> chunks;

    @WrapOperation(
            method = "set", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;unloadBlockEntities(Lnet/minecraft/world/chunk/WorldChunk;)V",
    opcode = Opcodes.ASTORE)
    )
    private void setInject(ClientWorld instance, WorldChunk chunk, Operation<Void> original) {
        original.call(instance, chunk);
        Chunk chunk1 = chunk;
        chunk1.free();

        //worldChunk.free();
    }

    ///**
    // * @author
    // * @reason
    // */
    //@Overwrite
    //protected void set(int index, @Nullable WorldChunk chunk) {
    //    WorldChunk worldChunk = (WorldChunk)chunks.getAndSet(index, chunk);
    //    if (worldChunk != null) {
    //        --loadedChunkCount;
    //        ClientChunkManager.ClientChunkMap self = (ClientChunkManager.ClientChunkMap) (Object) this;
    //
    //        ClientChunkManager.this.world.unloadBlockEntities(worldChunk);
    //    }
//
    //    if (chunk != null) {
    //        ++this.loadedChunkCount;
    //    }
    //}

    @Inject(method = "compareAndSet", at = @At("TAIL"))
    private void compareAndSetInject(int index, WorldChunk expect, WorldChunk update, CallbackInfoReturnable<WorldChunk> cir) {
        expect.free();
    }

}
