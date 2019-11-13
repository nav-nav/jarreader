import valtman.jar.reader.model.ClassDataModel;
import valtman.jar.reader.model.ClassModel;
import valtman.jar.reader.model.MethodModel;
import valtman.jar.reader.model.diff.JarDiffModel;
import valtman.jar.reader.service.JarReaderService;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DummyJarReaderService implements JarReaderService {
    public List<ClassModel> readJar(String path) {
        return Collections.singletonList(ClassModel.builder()
                .classDataModel(ClassDataModel.builder()
                        .annotations(Collections.singletonList("annotation"))
                        .methods(Arrays.asList(MethodModel.builder()
                                .argumentTypes(Arrays.asList("first", "second"))
                                .returnType("void")
                                .methodName("first")
                                .build(),
                                MethodModel.builder()
                                        .methodName("second")
                                        .exceptions(Collections.singletonList("newException"))
                                        .build()))
                        .build())
                .build());
    }

    @Override
    public JarDiffModel getDiff(File fromPath, File toPath) {
        return null;
    }
}
