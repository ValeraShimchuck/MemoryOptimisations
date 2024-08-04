package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.palette.AllocatedArrayPaletteStorage;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(Heightmap.class)
public class HeightmapMixin implements Freeable {


    /**

     */

    @Shadow
    @Mutable
    public PaletteStorage storage;
    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();


    /**
     * @author
     * @reason
     */
    @Override
    public void free() {
        PaletteStorageExtension paletteStorageExtension = storage;
        paletteStorageExtension.free();
    }


    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/Heightmap;storage:Lnet/minecraft/util/collection/PaletteStorage;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void inject(Heightmap instance, PaletteStorage value, Operation<Void> original) {
        //instance.storage = new AllocatedArrayPaletteStorage(value.getElementBits(), 256);
        original.call(instance, new AllocatedArrayPaletteStorage(value.getElementBits(), value.getSize()));
        //return new AllocatedArrayPaletteStorage(value.getElementBits(), value.getSize());
    }

    //@Inject(method = "<init>", at =@At("RETURN"))
    //private void inject(Chunk chunk, Heightmap.Type type, CallbackInfo ci) {
    //        storage = new AllocatedArrayPaletteStorage(storage.getElementBits(), storage.getSize());
    //}


    /**
     */
    @Overwrite()
    public void setTo(Chunk chunk, Heightmap.Type type, long[] values) {
//        long[] ls = this.storage.getData();
        PaletteStorageExtension extension = storage;
        if (extension.getDataLength() == values.length) {
//            System.arraycopy(values, 0, ls, 0, values.length);
            extension.writeData(values);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk " + String.valueOf(chunk.getPos()) + ", size does not match; expected: " + this.storage.getDataLength() + ", got: " + values.length);
        Heightmap.populateHeightmaps(chunk, EnumSet.of(type));
    }

    /**
     */
    @Overwrite
    public long[] asLongArray() {
        PaletteStorageExtension extension = storage;
        return extension.copyData();
    }

}
