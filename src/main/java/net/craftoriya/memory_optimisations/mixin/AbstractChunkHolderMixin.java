package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(AbstractChunkHolder.class)
public class AbstractChunkHolderMixin {

    @WrapOperation(
            method = "replaceWith",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/atomic/AtomicReferenceArray;compareAndSet(ILjava/lang/Object;Ljava/lang/Object;)Z"

            )
    )
    private <E> boolean onReplace(
            AtomicReferenceArray<CompletableFuture<OptionalChunk<Chunk>>> instance,
            int i,
            E expectedValue,
            E newValue,
            Operation<Boolean> original
    ) {
        boolean bool = original.call(instance, i, expectedValue, newValue);
        if (bool) {
            CompletableFuture<OptionalChunk<Chunk>> castedChunk = (CompletableFuture<OptionalChunk<Chunk>>) expectedValue;
            castedChunk.join().ifPresent(chunk -> {
                Freeable freeable = chunk;
                freeable.free();
            });
        }
        return bool;
    }

    //@Inject(method = "unload(ILjava/util/concurrent/CompletableFuture;)V", at = @At("TAIL"))
    //private void onUnload(int statusIndex, CompletableFuture<OptionalChunk<Chunk>> previousFuture, CallbackInfo ci) {
    //    previousFuture.thenAccept(c -> {
    //        c.ifPresent(chunk -> {
    //            Freeable freeable = chunk;
    //            freeable.free();
    //        });
    //    });
    //}

}
