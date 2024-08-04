package net.craftoriya.memory_optimisations.memory;

import net.craftoriya.memory_optimisations.interfaces.memory.IByteSerializer;
import net.craftoriya.memory_optimisations.jni.AllocatedArrayJNI;
import net.craftoriya.memory_optimisations.log.AsyncLogger;

import java.util.Arrays;
import java.util.function.IntFunction;

public class AllocatedArray implements AutoCloseable {

    private final long address;
    private final int size;
    private boolean isFreed = false;
    private String deallocationTrace;
    private final String allocationTrace;


    public AllocatedArray(int size) {
        this.address = AllocatedArrayJNI.allocate(size);
        allocationTrace = getTrace();
        this.size = size;
    }

    public byte[] read(int offset, int size) {
        ensureSpace(offset, size);
        ensureAllocated();
        byte[] bytes = new byte[size];
        AllocatedArrayJNI.read(address, offset, bytes);
        return bytes;
    }

    public byte[] read(int offset) {
        return read(offset, size - offset);
    }

    public void write(int offset, byte[] data) {
        ensureAllocated();
        ensureSpace(offset, data.length);
        AllocatedArrayJNI.write(address, offset, data);
    }

    public int getSize() {
        return size;
    }

    public <T> T read(IByteSerializer<T> serializer, int elementIndex) {
        int offset = elementIndex * serializer.getElementSize();
        byte[] data = read(offset, serializer.getElementSize());
        return serializer.deserialize(data);
    }

    public <T> T[] readArray(IByteSerializer<T> serializer, IntFunction<T[]> arrayGenerator, int offset, int size) {
        int elementSize = serializer.getElementSize();
        byte[] bytes = read(offset * elementSize, size * serializer.getElementSize());
        T[] objArray = arrayGenerator.apply(size);
        for (int i = 0; i < size; i++) {
            byte[] subBuffer = Arrays.copyOfRange(bytes, i * elementSize, i * elementSize + elementSize);
            objArray[i] = serializer.deserialize(subBuffer);
        }
        return objArray;
    }

    public <T> void writeArray(IByteSerializer<T> serializer, T[] array, int dstOffset) {
        int elementSize = serializer.getElementSize();
        byte[] bytes = new byte[elementSize * array.length];
        for (int i = 0; i < array.length; i++) {
            byte[] subBuffer = new byte[elementSize];
            serializer.serialize(array[i], subBuffer);
            System.arraycopy(subBuffer, 0, bytes, i * elementSize, elementSize);
        }
        write(dstOffset, bytes);
    }

    public <T> void write(IByteSerializer<T> serializer, T obj, int elementIndex) {
        int offset = elementIndex * serializer.getElementSize();
        byte[] data = new byte[serializer.getElementSize()];
        serializer.serialize(obj, data);
        write(offset, data);
    }

    public synchronized void deallocate() {
        if (isFreed) return;
        deallocationTrace = getTrace();
        isFreed = true;
        AllocatedArrayJNI.deallocate(address);
    }

    private String getTrace() {
        return String.join("\n", (Iterable<? extends CharSequence>) StackWalker.getInstance()
                .walk(stream -> stream
                        .map(StackWalker.StackFrame::toStackTraceElement)
                        .map(StackTraceElement::toString)
                        .toList()));
    }


    private void ensureSpace(int offset, int size) {
        if (offset + size > this.size) {
            throw new IllegalArgumentException(
                    "You are an idiot, you are trying to write to memory is not associated with this address"
            );
        }
    }

    private void ensureAllocated() {
        if (isFreed) {
            System.err.println("Deallocation stacktrace");
            System.err.println(deallocationTrace);
            throw new IllegalArgumentException("You are an idiot, you are trying to use deallocated memory");
        }
    }

    public AllocatedArray copy(int offset, int size) {
        ensureSpace(offset, size);
        ensureAllocated();
        AllocatedArray newArray = new AllocatedArray(size);
        AllocatedArrayJNI.copy(newArray.address, 0, address, offset, size);
        return newArray;
    }

    public void copy(AllocatedArray dst, int dstOffset, int srcOffset, int length) {
        ensureSpace(srcOffset, size);
        ensureAllocated();
        AllocatedArrayJNI.copy(dst.address, dstOffset,address, srcOffset, length);
    }

    public AllocatedArray copy() {
        return copy(0, size);
    }


    @Override
    public void close() {
        deallocate();
    }

    @Override
    protected void finalize() throws Throwable {
        if (!isFreed) AsyncLogger.log("Memory has not been freed. Was allocated: " + allocationTrace);
    }
}
