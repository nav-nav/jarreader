package valtman.jar.reader.object.util;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import valtman.jar.reader.model.diff.ClassDiffModel;
import valtman.jar.reader.model.diff.JarDiffModel;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DiffUtilTest {

    public static final String UNCHANGED_METHOD = "public method(java.lang.Object,int,java.lang.Long):java.lang.String";
    public static final String DEFAULT_CONSTRUCTOR = "public <init>():void";
    public static final String UPDATED_METHOD = "private methodToUpdate():void";

    @Test
    public void shouldShowDeletedMethod() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithTwoMethods.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/deleted/ClassWithTwoMethods.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);
        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getMethods().getAdded()).isEmpty();
        assertThat(classDiffModel.getMethods().getUnchanged()).hasSize(2);
        assertThat(classDiffModel.getMethods().getUnchanged())
                .contains(DEFAULT_CONSTRUCTOR, UNCHANGED_METHOD);
        assertThat(classDiffModel.getMethods().getDeleted()).hasSize(1);
        assertThat(classDiffModel.getMethods().getDeleted().get(0)).isEqualTo(UPDATED_METHOD);
        assertThat(classDiffModel.getMethods().getUpdated()).isEmpty();
    }

    @Test
    public void shouldShowAddedMethod() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithTwoMethods.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/added/ClassWithTwoMethods.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);
        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getMethods().getAdded()).hasSize(1);
        assertThat(classDiffModel.getMethods().getAdded().get(0)).isEqualTo("methodToUpdate(java.lang.Object[]):void");
        assertThat(classDiffModel.getMethods().getUnchanged()).hasSize(3);
        assertThat(classDiffModel.getMethods().getUnchanged())
                .contains(DEFAULT_CONSTRUCTOR, UNCHANGED_METHOD, UPDATED_METHOD);
        assertThat(classDiffModel.getMethods().getDeleted()).isEmpty();
        assertThat(classDiffModel.getMethods().getUpdated()).isEmpty();
    }

    @Test
    public void shouldShowUpdatedMethod() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(classLoader.getResourceAsStream("classes/method/ClassWithTwoMethods.class"));
        ClassReader updatedClassReader = new ClassReader(classLoader.getResourceAsStream("classes/method/updated/ClassWithTwoMethods.class"));

        JarDiffModel jarDiffModel = DiffUtil.compareClass("test", classReader, updatedClassReader);
        checkContainsOnlyOneUpdatedClass(jarDiffModel);

        ClassDiffModel classDiffModel = jarDiffModel.getUpdated().get(0);
        assertThat(classDiffModel.getName()).isEqualTo("test");
        assertThat(classDiffModel.getMethods().getAdded()).isEmpty();
        assertThat(classDiffModel.getMethods().getUnchanged()).hasSize(2);
        assertThat(classDiffModel.getMethods().getUnchanged())
                .contains(DEFAULT_CONSTRUCTOR, UNCHANGED_METHOD);
        assertThat(classDiffModel.getMethods().getDeleted()).isEmpty();
        assertThat(classDiffModel.getMethods().getUpdated()).hasSize(1);

        var updatedMethodDiff = classDiffModel.getMethods().getUpdated().get(0);
        assertThat(updatedMethodDiff.getName()).isEqualTo("methodToUpdate");
        assertThat(updatedMethodDiff.getDeleted().getAccess()).isEqualTo("private");
        assertThat(updatedMethodDiff.getAdded().getAccess()).isEqualTo("protected");
        assertThat(updatedMethodDiff.getDeleted().getExceptions()).contains("java.lang.Exception");

    }

    @Test
    public void shouldPrintPrivateStaticMethodAccess() {
        assertThat(DiffUtil.getReadableAccess(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC)).isEqualTo("private static");
    }

    @Test
    public void shouldPrintPackageAbstractMethodAccess() {
        assertThat(DiffUtil.getReadableAccess(Opcodes.ACC_ABSTRACT)).isEqualTo("abstract");
    }

    @Test
    public void shouldPrintPublicFinalMethodAccess() {
        assertThat(DiffUtil.getReadableAccess(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL)).isEqualTo("public final");
    }

    @Test
    public void shouldPrintEmptyMethodAccess() {
        assertThat(DiffUtil.getReadableAccess(0)).isEmpty();
    }

    private void checkContainsOnlyOneUpdatedClass(JarDiffModel jarDiffModel) {
        assertThat(jarDiffModel).isNotNull();
        assertThat(jarDiffModel.getAdded()).isNull();
        assertThat(jarDiffModel.getUnchanged()).isNull();
        assertThat(jarDiffModel.getDeleted()).isNull();
        assertThat(jarDiffModel.getUpdated()).hasSize(1);
    }

}