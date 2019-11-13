package valtman.jar.reader.object;

import valtman.jar.reader.model.diff.JarDiffModel;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String... args) throws ExecutionException, InterruptedException {
        String path = "/Users/nataliya_valtman/IdeaProjects/jarreader/jar-object-reader/src/test/resources/lombok.jar";
        String path2 = "/Users/nataliya_valtman/IdeaProjects/jarreader/jar-object-reader/src/test/resources/lombok-0.4.jar";
        JarDiffModel diff = new JarObjectReaderService().getDiff(new File(path), new File(path2));
        System.out.println(diff);

    }


}
