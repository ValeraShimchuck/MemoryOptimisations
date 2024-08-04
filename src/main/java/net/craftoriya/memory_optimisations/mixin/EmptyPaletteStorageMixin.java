package net.craftoriya.memory_optimisations.mixin;

import net.craftoriya.memory_optimisations.interfaces.memory.IByteSerializer;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.memory.AllocatedArray;
import net.minecraft.util.collection.EmptyPaletteStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmptyPaletteStorage.class)
public class EmptyPaletteStorageMixin implements PaletteStorageExtension {


    @Shadow @Final private int size;

    @Shadow @Final public static long[] EMPTY_DATA;

    @Override
    public int getDataLength() {
        return size;
    }

    @Override
    public long[] copyData() {
        return EMPTY_DATA;
    }

    @Override
    public void free() {
    }

    @Override
    public void writePaletteIndices(AllocatedArray out) {
        for (int i = 0; i < size; i++) {
            out.write(IByteSerializer.LONG, 0L, i);
        }
    }
}
