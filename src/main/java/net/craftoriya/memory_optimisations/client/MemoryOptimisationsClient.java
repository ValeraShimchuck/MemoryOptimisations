package net.craftoriya.memory_optimisations.client;

import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.interfaces.palette.PalettedContainerExtension;
import net.craftoriya.memory_optimisations.log.AsyncLogger;
import net.craftoriya.memory_optimisations.mixin.accessors.PalettedContainerAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class MemoryOptimisationsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            int length = client.world.getChunkManager().chunks.chunks.length();
            for (int i = 0; i < length; i++) {
                Optional.ofNullable(client.world.getChunkManager().chunks.chunks.get(i)).ifPresent(Freeable::free);
            }
        });


        //AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
        //    if (!(player instanceof ClientPlayerEntity)) return ActionResult.SUCCESS;
        //    WorldChunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        //    ChunkSection section = chunk.getSection(pos.getY() >> 4);
        //    PalettedContainer<BlockState> container = section.getBlockStateContainer();
        //    MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        //    //Class<?> clazz = Class.forName(mappings.mapClassName("intermediary", "net.minecraft.class_2841$class_6561"));
        //    //Field storage = clazz.getDeclaredField(mappings.mapFieldName("intermediary", "net.minecraft.class_2841$class_6561", "comp_118", "L"));
        //    PalettedContainerExtension<BlockState> pse =  container;
        //    AsyncLogger.log("Size of palette: " + pse.getPalette().getSize());
        //    //AsyncLogger.log("Palette list: " + container.data.palette);
        //    AsyncLogger.log("Palette storage: " + Arrays.toString(pse.getStorage().getData()));
        //    AsyncLogger.log("Additional info: " + pse.getStorage());
        //    return ActionResult.SUCCESS;
        //});
    }


}
