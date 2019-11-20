package valtman.jar.reader.object;

import org.junit.jupiter.api.Test;
import valtman.jar.reader.exception.UnableToReadJarException;
import valtman.jar.reader.model.MethodModel;
import valtman.jar.reader.model.diff.ClassDiffModel;
import valtman.jar.reader.model.diff.DiffModel;
import valtman.jar.reader.model.diff.JarDiffModel;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JarObjectReaderServiceTest {

    private final JarObjectReaderService jarService = new JarObjectReaderService();

    @Test
    public void shouldCompareJarFiles() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        JarDiffModel diff = jarService.getDiff(
                new File(classLoader.getResource("jar/test-1.jar").toURI()),
                new File(classLoader.getResource("jar/test-2.jar").toURI()));
        assertThat(diff).isNotNull();

        assertThat(diff.getAdded()).hasSize(1);
        assertThat(diff.getAdded()).contains("valtman/test/jar/added/ClassToAdd.class");

        assertThat(diff.getDeleted()).hasSize(1);
        assertThat(diff.getDeleted()).contains("valtman/test/jar/deleted/ClassToDelete.class");

        assertThat(diff.getUnchanged()).hasSize(4);
        assertThat(diff.getUnchanged()).containsExactlyInAnyOrder(
                "valtman/test/jar/exceptions/SomeException.class", "valtman/test/jar/interfaces/SomeInterface.class",
                "valtman/test/jar/unchanged/FirstClass.class", "valtman/test/jar/unchanged/SecondClass.class");

        assertThat(diff.getUpdated()).hasSize(1);

        ClassDiffModel updatedClassDiff = diff.getUpdated().get(0);

        assertThat(updatedClassDiff.getName()).isEqualTo("valtman/test/jar/updated/ClassToUpdate.class");

        assertThat(updatedClassDiff.getSupperClass().getAdded()).isEqualTo("valtman.test.jar.unchanged.FirstClass");
        assertThat(updatedClassDiff.getSupperClass().getDeleted()).isEqualTo("valtman.test.jar.unchanged.SecondClass");

        assertThat(updatedClassDiff.getMethods().getAdded()).hasSize(1);
        assertThat(updatedClassDiff.getMethods().getAdded()).contains("methodToAdd(java.lang.String):java.lang.Object");
        assertThat(updatedClassDiff.getMethods().getDeleted()).hasSize(1);
        assertThat(updatedClassDiff.getMethods().getDeleted()).contains("private methodToDelete():java.lang.Object");

        assertThat(updatedClassDiff.getMethods().getUpdated()).hasSize(1);
        DiffModel<MethodModel, MethodModel> updatedMethodDiff = updatedClassDiff.getMethods().getUpdated().get(0);
        assertThat(updatedMethodDiff.getName()).isEqualTo("methodToUpdate");
        assertThat(updatedMethodDiff.getDeleted().getExceptions()).hasSize(1);
        assertThat(updatedMethodDiff.getDeleted().getExceptions()).contains("java.lang.Exception");

    }

    @Test
    public void shouldThrowExceptionForUnreadableJar() {
        ClassLoader classLoader = getClass().getClassLoader();

        CompletionException executionException = assertThrows(CompletionException.class, () -> jarService.getDiff(
                new File(classLoader.getResource("jar/test-1.jar").toURI()),
                new File(classLoader.getResource("jar/invalid.jar").toURI())));

        assertThat(executionException.getCause()).isInstanceOf(UnableToReadJarException.class);
        assertThat(executionException.getCause().getMessage()).isEqualTo("Can't read jar file invalid.jar");
    }
}