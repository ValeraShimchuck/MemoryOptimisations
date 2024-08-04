package net.craftoriya.memory_optimisations.palette;

import net.craftoriya.memory_optimisations.interfaces.memory.IByteSerializer;
import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.log.AsyncLogger;
import net.craftoriya.memory_optimisations.memory.AllocatedArray;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;

public class AllocatedArrayPaletteStorage implements PaletteStorage, PaletteStorageExtension {

    private static final int[] INDEX_PARAMETERS = new int[]{-1, -1, 0, Integer.MIN_VALUE, 0, 0, 0x55555555, 0x55555555, 0, Integer.MIN_VALUE, 0, 1, 0x33333333, 0x33333333, 0, 0x2AAAAAAA, 0x2AAAAAAA, 0, 0x24924924, 0x24924924, 0, Integer.MIN_VALUE, 0, 2, 0x1C71C71C, 0x1C71C71C, 0, 0x19999999, 0x19999999, 0, 390451572, 390451572, 0, 0x15555555, 0x15555555, 0, 0x13B13B13, 0x13B13B13, 0, 306783378, 306783378, 0, 0x11111111, 0x11111111, 0, Integer.MIN_VALUE, 0, 3, 0xF0F0F0F, 0xF0F0F0F, 0, 0xE38E38E, 0xE38E38E, 0, 226050910, 226050910, 0, 0xCCCCCCC, 0xCCCCCCC, 0, 0xC30C30C, 0xC30C30C, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 0xAAAAAAA, 0xAAAAAAA, 0, 171798691, 171798691, 0, 0x9D89D89, 0x9D89D89, 0, 159072862, 159072862, 0, 0x9249249, 0x9249249, 0, 148102320, 148102320, 0, 0x8888888, 0x8888888, 0, 138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 0x7878787, 0x7878787, 0, 0x7507507, 0x7507507, 0, 0x71C71C7, 0x71C71C7, 0, 116080197, 116080197, 0, 113025455, 113025455, 0, 0x6906906, 0x6906906, 0, 0x6666666, 0x6666666, 0, 104755299, 104755299, 0, 0x6186186, 0x6186186, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 0x5B05B05, 0x5B05B05, 0, 93368854, 93368854, 0, 91382282, 91382282, 0, 0x5555555, 0x5555555, 0, 87652393, 87652393, 0, 85899345, 85899345, 0, 0x5050505, 0x5050505, 0, 0x4EC4EC4, 0x4EC4EC4, 0, 81037118, 81037118, 0, 79536431, 79536431, 0, 78090314, 78090314, 0, 0x4924924, 0x4924924, 0, 75350303, 75350303, 0, 74051160, 74051160, 0, 72796055, 72796055, 0, 0x4444444, 0x4444444, 0, 70409299, 70409299, 0, 69273666, 69273666, 0, 0x4104104, 0x4104104, 0, Integer.MIN_VALUE, 0, 5};
    private final AllocatedArray data;
    //private final PaletteStorage original;
    private final int elementBits;
    private final long maxValue;
    private final int size;
    private final int elementsPerLong;
    private final int indexScale;
    private final int indexOffset;
    private final int indexShift;
    //private String lastWrite = "none";
    //private final Map<String, String> debugProperties = new ConcurrentHashMap<>();

    public AllocatedArrayPaletteStorage(int elementBits, int size, int[] data) {
        this(elementBits, size);
        int j;
        int i = 0;
        for (j = 0; j <= size - this.elementsPerLong; j += this.elementsPerLong) {
            long l = 0L;
            for (int k = this.elementsPerLong - 1; k >= 0; --k) {
                l <<= elementBits;
                l |= (long) data[j + k] & this.maxValue;
            }
            int number = i++;
            this.data.write(IByteSerializer.LONG, l, number);
            //this.original.getData()[number] = l;
        }
        int m = size - j;
        if (m > 0) {
            long n = 0L;
            for (int o = m - 1; o >= 0; --o) {
                n <<= elementBits;
                n |= (long) data[j + o] & this.maxValue;
            }
            this.data.write(IByteSerializer.LONG, n, i);
            //this.original.getData()[i] = n;
            //this.data.read(IByteSerializer.LONG, i); // long n = this.data[i]
        }
        //for (int d = 0; d < getDataLength(); d++) {
        //    long l1 = this.data.read(IByteSerializer.LONG, d);
        //    long l2 = this.original.getData()[d];
        //    if (l1 != l2) AsyncLogger.log("WTF init inequality " + l1 + " " + l2);
        //}
    }

    public AllocatedArrayPaletteStorage(int elementBits, int size) {
        this(elementBits, size, (long[]) null);
    }

    public AllocatedArrayPaletteStorage(int elementBits, int size, long[] data) {
        Validate.inclusiveBetween(1L, 32L, elementBits);
        //original = new PackedIntegerArray(elementBits, size, data);
        this.size = size;
        this.elementBits = elementBits;
        this.maxValue = (1L << elementBits) - 1L;
        this.elementsPerLong = (char)(64 / elementBits);
        int i = 3 * (this.elementsPerLong - 1);
        this.indexScale = INDEX_PARAMETERS[i];      /* PREVIOUS this.indexScale = INDEX_PARAMETERS[i + 0];*/
        this.indexOffset = INDEX_PARAMETERS[i + 1];
        this.indexShift = INDEX_PARAMETERS[i + 2];
        int j = (size + this.elementsPerLong - 1) / this.elementsPerLong;
        if (data != null) {
            if (data.length != j) {
                throw new IllegalStateException("Invalid length given for storage, got: " + data.length + " but expected: " + j);
            }
            this.data = new AllocatedArray(j * 8);
            this.data.write(IByteSerializer.longArraySerializer(data.length), data, 0);
        } else {
            this.data = new AllocatedArray(j * 8);
        }


        boolean isShitThere = false;
        //for (int d = 0; d < getDataLength(); d++) {
        //    long l1 = this.data.read(IByteSerializer.LONG, d);
        //    //long l2 = this.original.getData()[d];
        //    if (l1 != l2) {
        //        AsyncLogger.log("WTF init inequality2 " + l1 + " " + l2);
        //        isShitThere = true;
        //    }
        //}
        //if (isShitThere) {
        //    AsyncLogger.log(original.toString());
        //    AsyncLogger.log( "----------------------------------------------------------\nData" + Arrays.hashCode(data) + "\n" +
        //            "AllocatedArrayPaletteStorage(" + elementBits + ", " + size + ", " + Arrays.toString(data) + "):\n" +
        //            "size: " + size + '\n' +
        //            "elementBits: " + elementBits + "\n" +
        //            "maxValue: " + maxValue + "\n" +
        //            "elementsPerLong: " + elementsPerLong + "\n" +
        //            "i: " + i + "\n" +
        //            "indexScale: " + indexScale + "\n" +
        //            "indexOffset: " + indexOffset + "\n" +
        //            "indexShift: " + indexShift + "\n" +
        //            "j: " + j
        //    );
        //}
        //updateLastWrite();
    }

    public AllocatedArrayPaletteStorage(int elementBits, int size, AllocatedArray allocatedArray) {
        Validate.inclusiveBetween(1L, 32L, elementBits);
        this.size = size;
        this.elementBits = elementBits;
        this.maxValue = (1L << elementBits) - 1L;
        this.elementsPerLong = (char)(64 / elementBits);
        int i = 3 * (this.elementsPerLong - 1);
        this.indexScale = INDEX_PARAMETERS[i]; /* PREVIOUS this.indexScale = INDEX_PARAMETERS[i + 0];*/
        this.indexOffset = INDEX_PARAMETERS[i + 1];
        this.indexShift = INDEX_PARAMETERS[i + 2];
        int j = (size + this.elementsPerLong - 1) / this.elementsPerLong;
        this.data = allocatedArray;
        //this.original = new PackedIntegerArray(
        //        elementBits,
        //        size,
        //        data.read(IByteSerializer.longArraySerializer(allocatedArray.getSize() / 8), 0)
        //);
        updateLastWrite();

    }

    private void updateLastWrite() {
        //lastWrite = String.join("\n", (Iterable<? extends CharSequence>) StackWalker.getInstance().walk(stream -> stream.map(StackWalker.StackFrame::toStackTraceElement)
        //        .map(StackTraceElement::toString).toList()));
    }

    private int getStorageIndex(int index) {
        long l = Integer.toUnsignedLong(this.indexScale);
        long m = Integer.toUnsignedLong(this.indexOffset);
        return (int)((long)index * l + m >> 32 >> this.indexShift);
    }

    @Override
    public int swap(int index, int value) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        Validate.inclusiveBetween(0L, this.maxValue, value);
        int i = this.getStorageIndex(index);
//        long l = this.data[i];
        long l = this.data.read(IByteSerializer.LONG, i);
        int j = (index - i * this.elementsPerLong) * this.elementBits;
        int k = (int)(l >> j & this.maxValue);
//        this.data[i] = l & (this.maxValue << j ^ 0xFFFFFFFFFFFFFFFFL) | ((long)value & this.maxValue) << j;
        this.data.write(IByteSerializer.LONG, l & ~(this.maxValue << j) | ((long)value & this.maxValue) << j, i);
        updateLastWrite();
        //int toCompare = this.original.swap(index, value);
        //int toCheck = get(index);
        //if (k != toCompare) AsyncLogger.log("WTF swap data " + index + " " + value + " " + k + " " + toCompare);
        //if (toCheck != value) AsyncLogger.log("WTF invalid swap " + toCheck + " " + value + " " + index);
        return k;
    }

    @Override
    public void set(int index, int value) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        Validate.inclusiveBetween(0L, this.maxValue, value);
        int i = this.getStorageIndex(index);
//        long l = this.data[i];
        long l = this.data.read(IByteSerializer.LONG, i);
        int j = (index - i * this.elementsPerLong) * this.elementBits;
//        this.data[i] = l & ~(this.maxValue << j) | ((long)value & this.maxValue) << j;
        this.data.write(IByteSerializer.LONG, l & ~(this.maxValue << j) | ((long)value & this.maxValue) << j, i);
        updateLastWrite();
        //this.original.set(index, value);
        int toCheck = get(index);
        if (toCheck != value) AsyncLogger.log("WTF on set validation failed " + value + " ");
    }

    @Override
    public int get(int index) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        int i = this.getStorageIndex(index);
//        long l = this.data[i];
        long l = this.data.read(IByteSerializer.LONG, i);
        int j = (index - i * this.elementsPerLong) * this.elementBits;
        int result = (int)(l >> j & this.maxValue);
        //int toCompare = this.original.get(index);
        //if (result != toCompare) {
        //    String sb = "WTF get data " + index + " " + result + " " + toCompare + "\n" +
        //            original + "\n" +
        //            " aaps get " + index + "\n" + "i: " + i + "\n" + "l: " + l + "\n" + "j: " + j;
        //    AsyncLogger.log(sb);
        //}
        return (int)(l >> j & this.maxValue);
    }

    @Override
    public long[] getData() {
        return copyData();
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getElementBits() {
        return this.elementBits;
    }

    @Override
    public void forEach(IntConsumer action) {
        int i = 0;
        for (int k = 0; k < getDataLength(); k++) {
            long l  = data.read(IByteSerializer.LONG, k);
            for (int j = 0; j < this.elementsPerLong; ++j) {
                action.accept((int)(l & this.maxValue));
                l >>= this.elementBits;
                if (++i < this.size) continue;
                return;
            }
        }
    }

    @Override
    public void writePaletteIndices(int[] out) {
        int m;
        long l;
        int k;
        int i = getDataLength();
        int j = 0;
        for (k = 0; k < i - 1; ++k) {
//            l = this.data[k];
            l = this.data.read(IByteSerializer.LONG, k);
            for (m = 0; m < this.elementsPerLong; ++m) {
                out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
            j += this.elementsPerLong;
        }
        k = this.size - j;
        if (k > 0) {
//            l = this.data[i - 1];
            l = this.data.read(IByteSerializer.LONG, i - 1);
            for (m = 0; m < k; ++m) {
                out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
        }
    }

    @Override
    public PaletteStorage copy() {
        return new AllocatedArrayPaletteStorage(this.elementBits, this.size, data.copy());
    }


    // ______________________________________________________________ custom methods
    public int getDataLength() {
        return data.getSize() / 8;
    }

    @Override
    public long[] copyData() {
        long[] copyData = data.read(IByteSerializer.longArraySerializer(getDataLength()), 0);
        //boolean areEqual = Arrays.equals(copyData, original.getData());
        //if (!areEqual) {
        //    AsyncLogger.log("Got problem\narr1: " + Arrays.toString(copyData) + "\narr2: " + Arrays.toString(original.getData()));
        //}
        return copyData;
        //return Arrays.stream(data.readArray(IByteSerializer.LONG, Long[]::new, 0, size))
        //        .mapToLong(l -> l).toArray();
    }

    @Override
    public void writeData(long[] array) {
       data.writeArray(IByteSerializer.LONG, Arrays.stream(array).boxed().toArray(Long[]::new), 0);
       updateLastWrite();
       //System.arraycopy(array, 0, original.getData(), 0, array.length);
    }

    @Override
    public void writePaletteIndices(AllocatedArray out) {
        int m;
        long l;
        int k;
        int i = getDataLength();
        int j = 0;
        for (k = 0; k < i - 1; ++k) {
//            l = this.data[k];
            l = this.data.read(IByteSerializer.LONG, k);
            for (m = 0; m < this.elementsPerLong; ++m) {
                out.write(IByteSerializer.INT, (int)(l & this.maxValue), j + m);
                //out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
            j += this.elementsPerLong;
        }
        k = this.size - j;
        if (k > 0) {
//            l = this.data[i - 1];
            l = this.data.read(IByteSerializer.LONG, i - 1);
            for (m = 0; m < k; ++m) {
                out.write(IByteSerializer.INT, (int)(l & this.maxValue), j + m);
                //out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
        }
        updateLastWrite();
    }

    @Override
    public void free() {
        data.close();
    }


    //@Override
    //public void setDebugProperty(String key, String value) {
    //    debugProperties.put(key, value);
    //}
//
    //@Override
    //public String getDebugProperty(String key) {
    //    return debugProperties.get(key);
    //}

    //@Override
    //public String toString() {
    //    return lastWrite;
    //}
}
