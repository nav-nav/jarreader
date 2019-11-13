package valtman.jar.reader.util;

import org.objectweb.asm.ClassReader;
import valtman.jar.reader.exception.UnableToReadJarException;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReaderUtils {
    public static Map<String, ClassReader> load(File jarFile) {
        Map<String, ClassReader> classReaderMap = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry next = enumeration.nextElement();
                if (next.getName().endsWith(".class")) {
                    ClassReader reader = new ClassReader(jar.getInputStream(next));
                    classReaderMap.put(next.getRealName(), reader);
                }
            }
        } catch (IOException e) {
            throw new UnableToReadJarException("Can't read jar file " + jarFile.getName(), e);
        }
        return classReaderMap;
    }
}
