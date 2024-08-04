package net.craftoriya.memory_optimisations.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.interfaces.palette.PalettedContainerExtension;
import net.craftoriya.memory_optimisations.palette.AllocatedArrayPaletteStorage;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.LongStream;

import static net.minecraft.world.chunk.PalettedContainer.applyEach;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerExtension {


    @Shadow public abstract void lock();

    @Shadow public volatile PalettedContainer.Data<T> data;

    @Shadow @Final private PaletteResizeListener<T> dummyListener;


    @Shadow public abstract void unlock();


    @Inject(method = "onResize", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/chunk/PalettedContainer;data:Lnet/minecraft/world/chunk/PalettedContainer$Data;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.BEFORE
        )
    )
    private void injectOnResize(int i, T object, CallbackInfoReturnable<Integer> cir) {
        PaletteStorageExtension ext = data.storage;
        ext.free();
    }

    /**
     */
    @Overwrite
    private static <T> DataResult<PalettedContainer<T>> read(
            IndexedIterable<T> idList,
            PalettedContainer.PaletteProvider paletteProvider,
            ReadableContainer.Serialized<T> serialized
    ) {
        PaletteStorage paletteStorage;
        List<T> list = serialized.paletteEntries();
        int i = paletteProvider.getContainerSize();
        int j = paletteProvider.getBits(idList, list.size());
        PalettedContainer.DataProvider<T> dataProvider = paletteProvider.createDataProvider(idList, j);
        if (j == 0) {
            paletteStorage = new EmptyPaletteStorage(i);
        } else {
            Optional<LongStream> optional = serialized.storage();
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }
            long[] ls = optional.get().toArray();
            try {
                if (dataProvider.factory() == PalettedContainer.PaletteProvider.ID_LIST) {
                    BiMapPalette<T> palette = new BiMapPalette<T>(idList, j, (id, value) -> 0, list);
                    PackedIntegerArray packedIntegerArray = new PackedIntegerArray(j, i, ls);
                    int[] is = new int[i];
                    packedIntegerArray.writePaletteIndices(is);
                    applyEach(is, id -> idList.getRawId(palette.get(id)));
                    paletteStorage = new AllocatedArrayPaletteStorage(dataProvider.bits(), i, is);
                } else {
                    paletteStorage = new AllocatedArrayPaletteStorage(dataProvider.bits(), i, ls);
                }
            } catch (PackedIntegerArray.InvalidLengthException invalidLengthException) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + invalidLengthException.getMessage());
            }
        }
        return DataResult.success(new PalettedContainer<T>(idList, paletteProvider, dataProvider, paletteStorage, list));
    }




    ///**
    // */
    //@Overwrite
    //private static <T, C extends ReadableContainer<T>> Codec<C> createCodec(
    //        IndexedIterable<T> idList,
    //        Codec<T> entryCodec,
    //        PalettedContainer.PaletteProvider provider,
    //        T defaultValue,
    //        ReadableContainer.Reader<T, C> reader
    //) {
    //    return RecordCodecBuilder.<ReadableContainer.Serialized<T>>create((instance) -> {
    //        return instance.group(entryCodec.mapResult(Codecs.orElsePartial(defaultValue))
    //                .listOf().fieldOf("palette")
    //                .forGetter(ReadableContainer.Serialized::paletteEntries),
    //                Codec.LONG_STREAM.lenientOptionalFieldOf("data")
    //                        .forGetter(ReadableContainer.Serialized::storage)
    //        ).apply(instance, (ReadableContainer.Serialized::new));
    //    }).comapFlatMap((serialized) -> reader.read(idList, provider, serialized), (container) -> {
    //        PalettedContainer<T> extension = (PalettedContainer<T>) container;
    //        ReadableContainer.Serialized<T> serialize = container.serialize(idList, provider);
    //        extension.data.storage.free();
    //        return serialize;
    //    });
    //}

    //@Inject(method = "method_38302", at = @At("RETURN"))
    //private static void inject(
    //        IndexedIterable indexedIterable,
    //        PalettedContainer.PaletteProvider paletteProvider,
    //        ReadableContainer container,
    //        CallbackInfoReturnable<ReadableContainer.Serialized> cir
    //) {
    //    PalettedContainer palettedContainer = (PalettedContainer) container;
    //    palettedContainer.free();
    //}


    /**
     */
    @Override
    public void free() {
        PalettedContainer<T> self = (PalettedContainer<T>) (Object) this;
        PaletteStorageExtension extension = self.data.storage;
        extension.free();
    }

    @Override
    public Palette<T> getPalette() {
        return data.palette;
    }

    @Override
    public PaletteStorage getStorage() {
        return data.storage;
    }

    /**
     */
    @Overwrite
    public void readPacket(PacketByteBuf buf) {
        PalettedContainer<T> self = (PalettedContainer<T>) (Object) this;
        self.lock();
        PaletteStorageExtension paletteStorageExtension = self.data.storage;
        paletteStorageExtension.free();
        try {
            int i = buf.readByte();
            PalettedContainer.Data<T> data = self.getCompatibleData(self.data, i);
            paletteStorageExtension = data.storage;
            data.palette.readPacket(buf);
            //buf.readLongArray(data.storage.getData());
            paletteStorageExtension.writeData(buf.readLongArray());
            self.data = data;
        } finally {
            self.unlock();
        }
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    public ReadableContainer.Serialized<T> serialize(IndexedIterable<T> idList, PalettedContainer.PaletteProvider paletteProvider) {
        lock();

        ReadableContainer.Serialized<T> var12;
        try {
            BiMapPalette<T> biMapPalette = new BiMapPalette<>(idList, data.storage.getElementBits(), dummyListener);
            int i = paletteProvider.getContainerSize();
            int[] is = new int[i];
            this.data.storage.writePaletteIndices(is);
            applyEach(is, (id) -> {
                return biMapPalette.index(this.data.palette.get(id));
            });
            int j = paletteProvider.getBits(idList, biMapPalette.getSize());
            Optional<LongStream> optional;
            if (j != 0) {
                PaletteStorageExtension packedIntegerArray = new AllocatedArrayPaletteStorage(j, i, is);
                optional = Optional.of(Arrays.stream(packedIntegerArray.copyData()));
                packedIntegerArray.free();
            } else {
                optional = Optional.empty();
            }

            var12 = new ReadableContainer.Serialized<T>(biMapPalette.getElements(), optional);
        } finally {
            unlock();
        }

        return var12;
    }

}
