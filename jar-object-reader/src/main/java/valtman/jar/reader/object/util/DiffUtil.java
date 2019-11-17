package valtman.jar.reader.object.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import valtman.jar.reader.model.diff.ClassDiffModel;
import valtman.jar.reader.model.diff.DiffModel;
import valtman.jar.reader.model.diff.JarDiffModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiffUtil {

    public static JarDiffModel compareClass(String name, ClassReader firstClassReader, ClassReader secondClassReader) {

        if (Objects.isNull(firstClassReader)) {
            return JarDiffModel.jarDiffBuilder()
                    .added(Collections.singletonList(name))
                    .build();
        }

        if (Objects.isNull(secondClassReader)) {
            return JarDiffModel.jarDiffBuilder()
                    .deleted(Collections.singletonList(name))
                    .build();
        }

        //TODO for new version it is deprecated
        if (Arrays.equals(firstClassReader.b, secondClassReader.b)) {
            return JarDiffModel.jarDiffBuilder()
                    .unchanged(Collections.singletonList(name))
                    .build();
        }

        ClassDiffModel classDiff = processUpdatedClass(name, firstClassReader, secondClassReader);
        if (isChanged(classDiff)) {
            return JarDiffModel.jarDiffBuilder()
                    .updated(Collections.singletonList(classDiff))
                    .build();
        }
        return JarDiffModel.jarDiffBuilder()
                .unchanged(Collections.singletonList(name))
                .build();
    }

    public static String getReadableClassName(String exceptionClass) {
        return exceptionClass.replace("/", ".");
    }

    private static boolean isChanged(ClassDiffModel classDiff) {
        return isCollectionChanged(classDiff.getMethods())
                || isCollectionChanged(classDiff.getAnnotations())
                || isCollectionChanged(classDiff.getInterfaces())
                || isObjectChanged(classDiff.getSupperClass());
    }

    private static <U> boolean isObjectChanged(DiffModel<String, U> diffModel) {
        if (Objects.isNull(diffModel)) {
            return false;
        }
        return Objects.nonNull(diffModel.getAdded())
                || Objects.nonNull(diffModel.getDeleted())
                || Objects.nonNull(diffModel.getUpdated());
    }

    private static <T, U> boolean isCollectionChanged(DiffModel<List<T>, List<U>> diffModel) {
        if (Objects.isNull(diffModel)) {
            return false;
        }
        return CollectionUtils.isNotEmpty(diffModel.getAdded())
                || CollectionUtils.isNotEmpty(diffModel.getDeleted())
                || CollectionUtils.isNotEmpty(diffModel.getUpdated());
    }

    private static ClassDiffModel processUpdatedClass(String name, ClassReader firstClassReader, ClassReader secondClassReader) {
        Map<String, MethodNode> firstMethods = getClassMethods(firstClassReader);
        Map<String, MethodNode> secondMethods = getClassMethods(secondClassReader);

        var methodDiffs = MethodDiffUtil.getMethodDiff(firstClassReader, firstMethods, secondMethods);
        var interfaceDiffs = getInterfaceDiff(firstClassReader.getInterfaces(), secondClassReader.getInterfaces());
        DiffModel<String, String> superClassDiff = getSuperClassDiff(firstClassReader.getSuperName(), secondClassReader.getSuperName());

        return ClassDiffModel.builder()
                .name(name)
                .methods(methodDiffs)
                .interfaces(interfaceDiffs)
                .supperClass(superClassDiff)
                .build();
    }

    private static DiffModel<String, String> getSuperClassDiff(String firstSupperClass, String secondSupperClass) {
        if (Objects.equals(firstSupperClass, secondSupperClass)) {
            return DiffModel.<String, String>builder()
                    .unchanged(getReadableClassName(firstSupperClass))
                    .build();
        }
        return DiffModel.<String, String>builder()
                .deleted(getReadableClassName(firstSupperClass))
                .added(getReadableClassName(secondSupperClass))
                .build();
    }

    private static DiffModel<List<String>, List<String>> getInterfaceDiff(String[] first, String[] second) {
        //TODO the number of interfaces for single class is usually small but still should be checked
        Set<String> addedInterfaces = new HashSet<>(Arrays.asList(second));
        List<String> unchangedInterfaces = new ArrayList<>();

        List<String> deletedReadableInterfaces = Arrays.stream(first)
                .filter(name -> !(addedInterfaces.remove(name) && unchangedInterfaces.add(name)))
                .map(DiffUtil::getReadableClassName)
                .collect(Collectors.toUnmodifiableList());

        List<String> unchangedReadableInterfaces = unchangedInterfaces.stream()
                .map(DiffUtil::getReadableClassName)
                .collect(Collectors.toList());

        List<String> addedReadableInterfaces = addedInterfaces.stream()
                .map(DiffUtil::getReadableClassName)
                .collect(Collectors.toList());

        return new DiffModel<>("interfaces", addedReadableInterfaces, null,
                deletedReadableInterfaces, unchangedReadableInterfaces);
    }

    private static Map<String, MethodNode> getClassMethods(ClassReader classReader) {
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return Stream.ofNullable(classNode.methods)
                .flatMap(List::stream)
                .collect(Collectors.toMap(method -> method.name + method.desc, method -> method));
    }

}
