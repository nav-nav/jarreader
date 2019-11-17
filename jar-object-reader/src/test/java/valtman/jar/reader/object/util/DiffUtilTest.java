package valtman.jar.reader.object.util;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.assertj.core.api.Assertions.assertThat;

class DiffUtilTest {

    @Test
    public void shouldPrintPrivateStaticMethodAccess() {
        assertThat(MethodDiffUtil.getReadableAccess(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC)).isEqualTo("private static");
    }

    @Test
    public void shouldPrintPackageAbstractMethodAccess() {
        assertThat(MethodDiffUtil.getReadableAccess(Opcodes.ACC_ABSTRACT)).isEqualTo("abstract");
    }

    @Test
    public void shouldPrintPublicFinalMethodAccess() {
        assertThat(MethodDiffUtil.getReadableAccess(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL)).isEqualTo("public final");
    }

    @Test
    public void shouldPrintEmptyMethodAccess() {
        assertThat(MethodDiffUtil.getReadableAccess(0)).isEmpty();
    }

}