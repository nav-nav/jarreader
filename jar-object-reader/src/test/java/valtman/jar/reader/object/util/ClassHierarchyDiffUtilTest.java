package valtman.jar.reader.object.util;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import valtman.jar.reader.model.diff.ClassDiffModel;
import valtman.jar.reader.model.diff.JarDiffModel;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassHierarchyDiffUtilTest {

    public static final String UNCHANGED_INTERFACE = "java.util.function.Supplier";
    public static final String UPDATED_INTERFACE = "java.util.Comparator";
    public static final String SUPPER_CLASS_NAME = "valtman.jar.reader.object.util.ClassWithTwoMethods";

    @Test
    public void shouldShowDeletedInterface() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithHierarchy.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/deleted/ClassWithHierarchy.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);

        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getSupperClass().getUnchanged()).isEqualTo(SUPPER_CLASS_NAME);
        assertThat(classDiffModel.getInterfaces().getAdded()).isEmpty();
        assertThat(classDiffModel.getInterfaces().getUnchanged()).hasSize(1);
        assertThat(classDiffModel.getInterfaces().getUnchanged()).contains(UNCHANGED_INTERFACE);
        assertThat(classDiffModel.getInterfaces().getDeleted()).hasSize(1);
        assertThat(classDiffModel.getInterfaces().getDeleted().get(0)).isEqualTo(UPDATED_INTERFACE);
        assertThat(classDiffModel.getInterfaces().getUpdated()).isNull();
    }

    @Test
    public void shouldShowAddedInterface() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithHierarchy.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/added/ClassWithHierarchy.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);
        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getSupperClass().getUnchanged()).isEqualTo(SUPPER_CLASS_NAME);
        assertThat(classDiffModel.getInterfaces().getAdded()).hasSize(1);
        assertThat(classDiffModel.getInterfaces().getAdded().get(0)).isEqualTo("java.lang.Cloneable");
        assertThat(classDiffModel.getInterfaces().getUnchanged()).hasSize(2);
        assertThat(classDiffModel.getInterfaces().getUnchanged())
                .contains(UNCHANGED_INTERFACE, UPDATED_INTERFACE);
        assertThat(classDiffModel.getInterfaces().getDeleted()).isEmpty();
        assertThat(classDiffModel.getInterfaces().getUpdated()).isNull();
    }

    @Test
    public void shouldShowUpdatedSupperClass() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithHierarchy.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/updated/ClassWithHierarchy.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);
        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getSupperClass().getAdded()).isEqualTo("java.lang.Object");
        assertThat(classDiffModel.getSupperClass().getUnchanged()).isNull();
        assertThat(classDiffModel.getSupperClass().getDeleted()).isEqualTo(SUPPER_CLASS_NAME);
        assertThat(classDiffModel.getSupperClass().getUpdated()).isNull();
    }

    private void checkContainsOnlyOneUpdatedClass(JarDiffModel jarDiffModel) {
        assertThat(jarDiffModel).isNotNull();
        assertThat(jarDiffModel.getAdded()).isNull();
        assertThat(jarDiffModel.getUnchanged()).isNull();
        assertThat(jarDiffModel.getDeleted()).isNull();
        assertThat(jarDiffModel.getUpdated()).hasSize(1);
    }

}
