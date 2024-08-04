package net.craftoriya.memory_optimisations.interfaces.palette;

import net.craftoriya.memory_optimisations.memory.AllocatedArray;

public interface PaletteStorageExtension {

    default long[] copyData() {
        return new long[0];
    }

    default void writeData(long[] array) { }

    default void writePaletteIndices(AllocatedArray out) { }

    default int getDataLength() {
        return 0;
    }

    default void free() {

    }

    default void setDebugProperty(String key, String value) {

    }

    default String getDebugProperty(String key) {
        return null;
    }

}
