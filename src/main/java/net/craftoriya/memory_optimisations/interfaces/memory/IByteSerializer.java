package net.craftoriya.memory_optimisations.interfaces.memory;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IByteSerializer<T> {

    IByteSerializer<Byte> BYTE = create(1, (aByte, bytes) -> bytes[0] = aByte, bytes -> bytes[0]);
    IByteSerializer<Short> SHORT = create(2, ((aShort, bytes) -> {
                bytes[0] = aShort.byteValue();
                bytes[1] = (byte) (aShort >> 8);
            }),
            bytes -> (short) (((short) bytes[0] & 0xFF) |
                                (((short) bytes[1] & 0xFF) << 8))
    );
    IByteSerializer<Integer> INT = create(4, (integer, bytes) -> {
        bytes[0] = integer.byteValue();
        bytes[1] = (byte) (integer >> 8);
        bytes[2] = (byte) (integer >> 16);
        bytes[3] = (byte) (integer >> 24);
    }, bytes -> ((int) bytes[0] & 0xFF) |
            (((int) bytes[1] & 0xFF) << 8) |
            (((int) bytes[2] & 0xFF) << 16) |
            (((int) bytes[3] & 0xFF) << 24));

    IByteSerializer<Long> LONG = create(8, (aLong, bytes) -> {
        bytes[0] = aLong.byteValue();
        bytes[1] = (byte) (aLong >> 8);
        bytes[2] = (byte) (aLong >> 16);
        bytes[3] = (byte) (aLong >> 24);
        bytes[4] = (byte) (aLong >> 32);
        bytes[5] = (byte) (aLong >> 40);
        bytes[6] = (byte) (aLong >> 48);
        bytes[7] = (byte) (aLong >> 56);
    }, bytes -> ((long) bytes[0] & 0xFF) |
            (((long) bytes[1] & 0xFF) << 8) |
            (((long) bytes[2] & 0xFF) << 16) |
            (((long) bytes[3] & 0xFF) << 24) |
            (((long) bytes[4] & 0xFF) << 32) |
            (((long) bytes[5] & 0xFF) << 40) |
            (((long) bytes[6] & 0xFF) << 48) |
            (((long) bytes[7] & 0xFF) << 56));

    static IByteSerializer<long[]> longArraySerializer(int size) {
        return create(
                size * 8,
                (longs, bytes) -> {
                    for (int i = 0; i < longs.length; i++) {
                        long l = longs[i];
                        byte[] bytesSeg = new byte[8];
                        LONG.serialize(l, bytesSeg);
                        System.arraycopy(bytesSeg, 0, bytes, i * 8, 8);
                    }
                },
                (bytes) -> {
                    long[] longs = new long[bytes.length / 8];
                    for (int i = 0; i < longs.length; i++) {
                        byte[] bytesSeg = new byte[8];
                        System.arraycopy(bytes, i * 8, bytesSeg, 0, 8);
                        longs[i] = LONG.deserialize(bytesSeg);
                    }
                    return longs;
                }
        );
    }


    static <T> IByteSerializer<T> create(
            int elementSize,
            BiConsumer<T, byte[]> serializer,
            Function<byte[], T> deserializer
    ) {
        return new IByteSerializer<>() {
            @Override
            public void serialize(T obj, byte[] buffer) {
                serializer.accept(obj, buffer);
            }

            @Override
            public T deserialize(byte[] bytes) {
                return deserializer.apply(bytes);
            }

            @Override
            public int getElementSize() {
                return elementSize;
            }
        };
    }

    void serialize(T obj, byte[] buffer);

    T deserialize(byte[] bytes);

    int getElementSize();

}
