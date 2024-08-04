package net.craftoriya.memory_optimisations.mixin;

import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.craftoriya.memory_optimisations.interfaces.palette.PalettedContainerExtension;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Arrays;

@Mixin(Chunk.class)
public class ChunkMixin implements Freeable {

    /**
     * @author
     * @reason
     */
    @Override
    public void free() {
        Chunk self = (Chunk) (Object) this;
        self.heightmaps.values().forEach(Freeable::free);
        Arrays.stream(self.getSectionArray()).forEach(section -> {
            PalettedContainerExtension freeableExt = section.getBlockStateContainer();
            freeableExt = section.getBlockStateContainer();
            freeableExt.free();
            freeableExt = section.getBiomeContainer();
            freeableExt.free();
        });
        Freeable freeable = self.getChunkSkyLight();
        freeable.free();
    }


}
