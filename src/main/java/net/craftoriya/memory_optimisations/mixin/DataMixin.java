package net.craftoriya.memory_optimisations.mixin;

import net.craftoriya.memory_optimisations.interfaces.palette.PaletteStorageExtension;
import net.craftoriya.memory_optimisations.palette.AllocatedArrayPaletteStorage;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PalettedContainer.Data.class)
public class DataMixin<T> {

    /**
     */
    @Overwrite
    public int getPacketSize() {
        PalettedContainer.Data<T> self = (PalettedContainer.Data<T>) (Object) this;
        //if (!(self.storage instanceof AllocatedArrayPaletteStorage)) {
        //    System.out.println("packet container got from " + self.storage);
        //}
        PaletteStorageExtension extension = self.storage;
        return 1 + self.palette.getPacketSize() + VarInts.getSizeInBytes(extension.getDataLength()) + extension.getDataLength() * 8;
    }

    /**
     */
    @Overwrite
    public void writePacket(PacketByteBuf buf) {
        PalettedContainer.Data<T> self = (PalettedContainer.Data<T>) (Object) this;
        PaletteStorageExtension extension = self.storage;




        buf.writeByte(self.storage.getElementBits());
        self.palette.writePacket(buf);
        buf.writeLongArray(extension.copyData());
    }
}
