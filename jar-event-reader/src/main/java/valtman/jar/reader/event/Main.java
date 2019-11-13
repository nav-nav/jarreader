package valtman.jar.reader.event;

import valtman.jar.reader.util.ReaderUtils;

import java.io.File;

public class Main {
    public static void main(String... args) {
        String path = "/Users/nataliya_valtman/IdeaProjects/jarreader/src/test/resources/lombok.jar";
        ReaderUtils.load(new File(path)).forEach(
                (name, classReader) -> classReader.accept(new DiffClassVisitor(393216),0)
        );


    }
}
