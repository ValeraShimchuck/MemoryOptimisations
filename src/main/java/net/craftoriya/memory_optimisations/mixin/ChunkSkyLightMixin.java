package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.palette.AllocatedArrayPaletteStorage;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkSkyLight.class)
public class ChunkSkyLightMixin implements Freeable {

    @Mutable
    @Shadow public PaletteStorage palette;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/light/ChunkSkyLight;palette:Lnet/minecraft/util/collection/PaletteStorage;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void inject(ChunkSkyLight instance, PaletteStorage value, Operation<Void> original) {
        original.call(instance, new AllocatedArrayPaletteStorage(value.getElementBits(), value.getSize()));
    }

    @Override
    public void free() {
        PaletteStorageExtension ext = palette;
        ext.free();
    }
}
