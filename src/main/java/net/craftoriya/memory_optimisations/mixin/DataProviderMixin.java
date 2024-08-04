package net.craftoriya.memory_optimisations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.craftoriya.memory_optimisations.palette.AllocatedArrayPaletteStorage;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PalettedContainer.DataProvider.class)
public class DataProviderMixin<T> {

    //@WrapOperation(method = "createData",
    //        at = @At(
    //                value = "STORE",
    //                target = "",
    //                opcode = Opcodes.IF_ICMPEQ
    //        ))
    //private void inject() {
//
    //}

    @Shadow @Final private int bits;

    @Shadow @Final private Palette.Factory factory;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public PalettedContainer.Data<T> createData(IndexedIterable<T> idList, PaletteResizeListener<T> listener, int size) {
        PaletteStorage paletteStorage = bits == 0 ? new EmptyPaletteStorage(size) : new AllocatedArrayPaletteStorage(this.bits, size);
        Palette<T> palette = factory.create(this.bits, idList, listener, List.of());
        return new PalettedContainer.Data<>((PalettedContainer.DataProvider<T>) (Object) this, paletteStorage, palette);
    }


}
