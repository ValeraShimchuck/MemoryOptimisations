package net.craftoriya.memory_optimisations.mixin;

import net.minecraft.util.collection.PackedIntegerArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

@Mixin(PackedIntegerArray.class)
public class PackedIntegerArrayMixin {


    @Shadow @Final private long maxValue;

    @Shadow @Final private int elementsPerLong;

    @Shadow @Final private int indexScale;

    @Shadow @Final private int indexOffset;

    @Shadow @Final private int indexShift;

    private String debugInfo;

    @Inject(method = "<init>(II[J)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectConstructor(int elementBits, int size, long[] data, CallbackInfo ci, int i, int j) {
        //printDebug();
        //debugInfo ="----------------------------------------------------------\nData" + Arrays.hashCode(data) + "\n" +
        //        "PackedIntegerArray(" + elementBits + ", " + size + ", " + Arrays.toString(data) + "):\n" +
        //        "size: " + size + '\n' +
        //        "elementBits: " + elementBits + "\n" +
        //        "maxValue: " + maxValue + "\n" +
        //        "elementsPerLong: " + elementsPerLong + "\n" +
        //        "i: " + i + "\n" +
        //        "indexScale: " + indexScale + "\n" +
        //        "indexOffset: " + indexOffset + "\n" +
        //        "indexShift: " + indexShift + "\n" +
        //        "j: " + j;
        debugInfo = String.join("\n", (Iterable<? extends CharSequence>) StackWalker.getInstance().walk(stream -> stream
                .map(StackWalker.StackFrame::toStackTraceElement).map(StackTraceElement::toString).toList()));
    }


    //@Inject(method = "<init>(II[I)V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    //private void injectConstructor(int elementBits, int size, int[] data, CallbackInfo ci, int j, int i, int m, long n) {
    //    debugInfo ="----------------------------------------------------------\nData" + Arrays.hashCode(data) + "\n" +
    //            "PackedIntegerArray(" + elementBits + ", " + size + ", " + Arrays.toString(data) + "):\n" +
    //            "size: " + size + '\n' +
    //            "elementBits: " + elementBits + "\n" +
    //            "maxValue: " + maxValue + "\n" +
    //            "elementsPerLong: " + elementsPerLong + "\n" +
    //            "m: " + m + "\n" +
    //            "indexScale: " + indexScale + "\n" +
    //            "indexOffset: " + indexOffset + "\n" +
    //            "indexShift: " + indexShift + "\n" +
    //            "j: " + j;
    //}


    @Inject(method = "get", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void inject(int index, CallbackInfoReturnable<Integer> cir, int i, long l, int j) {
        //debugInfo = "get info " + index + "\n" +
        //        "i: " + i + "\n" +
        //        "l: " + l + "\n" +
        //        "j: " + j + "\n" +
        //        "retval: " + cir.getReturnValue();
    }

    @Override
    public String toString() {
        return debugInfo;
    }

    private void printDebug() {
        if (true) return;
        StringBuilder frames = new StringBuilder();
        StackWalker.getInstance().forEach(frame -> frames.append(frame.toStackTraceElement()).append("\n"));
        System.out.println(frames);
    }

}
