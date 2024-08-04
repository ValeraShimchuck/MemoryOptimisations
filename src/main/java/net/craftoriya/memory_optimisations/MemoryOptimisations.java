package net.craftoriya.memory_optimisations;

import net.craftoriya.memory_optimisations.interfaces.palette.Freeable;
import net.craftoriya.memory_optimisations.loader.JNILoader;
import net.craftoriya.memory_optimisations.log.AsyncLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerChunkManager;

import java.io.File;

public class MemoryOptimisations implements ModInitializer {
    @Override
    public void onInitialize() {
        File libs = FabricLoader.getInstance().getGameDir().resolve("mods/memory_optimisations/libs").toFile();
        JNILoader.load(libs);
        AsyncLogger logger = AsyncLogger.getInstance();
        //ServerChunkManager
        //try (AllocatedArray array = new AllocatedArray(8 * 200)) {
        //    Random random = new Random(100);
        //    long[] longs = random.longs(200).toArray();
        //    array.writeArray(IByteSerializer.LONG, Arrays.stream(longs).boxed().toArray(Long[]::new) , 0);
        //    Long[] copy = array.readArray(IByteSerializer.LONG, Long[]::new, 0, 200);
        //    for (int i = 0; i < longs.length; i++) {
        //        long l1 = longs[i];
        //        long l2 = copy[i];
        //        if (l1 != l2) System.out.println("WTF not same " + l1 + " " + l2);
        //    }
        //}
        //try (AllocatedArray array = new AllocatedArray(8 * 200)) {
        //    long[] longs = array.read(IByteSerializer.longArraySerializer(200), 0);
        //    for (long l : longs) {
        //        System.out.println("Check on dirty memory: " + l);
        //    }
        //}
        //System.out.println("Loaded libraries, testing...");
        //try(AllocatedArray allocatedArray = new AllocatedArray(10)) {
        //    allocatedArray.write(0, new byte[]{0,2,4,6,8,10,12,14,16,18});
        //    byte[] bytes = allocatedArray.read(0);
        //    System.out.println(Arrays.toString(bytes));
        //}
        //ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
        //    server.getWorlds().forEach(world -> {
        //
        //    });
        //});
        //CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
        //        .register(LiteralArgumentBuilder.<ServerCommandSource>literal("debugchunk")
        //                .then(RequiredArgumentBuilder.argument("loc", BlockPosArgumentType.blockPos()))
        //                .executes(ctx -> {
        //                    PosArgument pos = ctx.getArgument("loc", PosArgument.class);
        //                    Vec3d vec3d = pos.toAbsolutePos(ctx.getSource());
        //                    World world = ctx.getSource().getWorld();
        //                    return 1;
        //                })));

        //ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
        //    Freeable freeable = chunk;
        //    freeable.free();
        //});

        //ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
        //    chunk.setBlockState(new BlockPos(0, (chunk.getTopY() - 1) % 16, 0), Blocks.DIAMOND_BLOCK.getDefaultState(), false);
        //});


    }
}
