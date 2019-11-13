package valtman.jar.reader.event;

import valtman.jar.reader.model.ClassModel;
import valtman.jar.reader.model.diff.JarDiffModel;
import valtman.jar.reader.util.ReaderUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class JarEventReaderService implements valtman.jar.reader.service.JarReaderService {
    public List<ClassModel> readJar(File path) {
        ReaderUtils.load(path).forEach(
                (name, classReader) -> classReader.accept(new DiffClassVisitor(393216),0)
        );
        return Collections.emptyList();
    }

    @Override
    public JarDiffModel getDiff(File fromPath, File toPath) {
        return null;
    }
}
