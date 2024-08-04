package net.craftoriya.memory_optimisations.jni;

public class AllocatedArrayJNI {

    public static native long allocate(int bytes);
    public static native void deallocate(long address);
    public static native void write(long address, int offset, byte[] data);
    public static native void read(long address, int offset, byte[] data);
    public static native void copy(long dst, int dstOffset, long src, int srcOffset, int length);
}
