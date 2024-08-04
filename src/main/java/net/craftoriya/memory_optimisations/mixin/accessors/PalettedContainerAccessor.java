package net.craftoriya.memory_optimisations.mixin.accessors;

import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {

    @Accessor("data")
    PalettedContainer.Data<T> getData();

    //default PaletteStorage getStorage() {
    //    return getData().storage;
    //}
//
    //default Palette<T> getPalette() {
    //    return getData().palette;
    //}


}
