package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.render.chunk.RenderedChunk")
public class RenderedChunkMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/PalettedContainer;copy()Lnet/minecraft/world/chunk/PalettedContainer;"))
    private PalettedContainer<BlockState> injectConstructor(PalettedContainer<BlockState> instance, Operation<PalettedContainer<BlockState>> original) {
        return instance;
    }


}
