package net.craftoriya.memory_optimisations.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class JNILoader {

    public static void load(File folder) {
        loadLib("/native/libMEMOPTJNI_LIB.so", folder);
    }

    private static void loadLib(String resourcePath, File folder) {
        try(InputStream is = JNILoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Library " + resourcePath + " is not found");
            File libFile = new File(folder.getAbsolutePath() + resourcePath);
            libFile.getParentFile().mkdirs();
            libFile.createNewFile();
            Files.copy(is, libFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(libFile.getAbsolutePath());
            System.out.println("Loaded " + resourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
