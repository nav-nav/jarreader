package valtman.jar.reader.event;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;

public class DiffClassVisitor extends ClassVisitor {
//    ClassDataModel.ClassDataModelBuilder classDataModelBuilder;


    public DiffClassVisitor(int api) {
        super(api);
    }

    public DiffClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println(access + " " + name + " " + signature + " " + superName + " " + Arrays.toString(interfaces));
//        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println(access + " " + name + " " + signature + " " + desc + " " + Arrays.toString(exceptions));
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        System.out.println("----------------------------------------------------------------");
    }
}
