package net.craftoriya.memory_optimisations.interfaces.palette;

import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;

public interface PalettedContainerExtension<T> {

    default void free() {

    }

    default PaletteStorage getStorage() {

        return null;
    }

    default Palette<T> getPalette() {
        return null;
    }

}
