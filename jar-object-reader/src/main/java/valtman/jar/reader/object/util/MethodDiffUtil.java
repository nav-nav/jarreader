package valtman.jar.reader.object.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import valtman.jar.reader.model.MethodModel;
import valtman.jar.reader.model.diff.DiffModel;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodDiffUtil {

    public static DiffModel<List<String>, List<DiffModel<MethodModel, MethodModel>>> getMethodDiff(
            ClassReader firstClassReader,
            Map<String, MethodNode> firstMethods,
            Map<String, MethodNode> secondMethods
    ) {
        Set<String> allMethodNames = new HashSet<>(firstMethods.keySet());
        allMethodNames.addAll(secondMethods.keySet());

        var methodDiffModels = allMethodNames
                .parallelStream()
                .map(method -> compareMethod(firstMethods.get(method), secondMethods.get(method)))
                .collect(Collectors.toList());

        return methodDiffModels
                .stream()
                .reduce(DiffModel.<List<String>, List<DiffModel<MethodModel, MethodModel>>>builder()
                                .name(getReadableName(firstClassReader.getAccess(), firstClassReader.getClassName()))
                                .updated(new LinkedList<>())
                                .unchanged(new LinkedList<>())
                                .deleted(new LinkedList<>())
                                .added(new LinkedList<>())
                                .build(),
                        MethodDiffUtil::reduceDiffModel);
    }

    public static String getReadableAccess(int access) {
        ArrayList<String> accessLine = new ArrayList<>();

        if (Modifier.isPrivate(access)) {
            accessLine.add("private");
        } else if (Modifier.isProtected(access)) {
            accessLine.add("protected");
        } else if (Modifier.isPublic(access)) {
            accessLine.add("public");
        }

        if (Modifier.isStatic(access)) {
            accessLine.add("static");
        }

        if (Modifier.isFinal(access)) {
            accessLine.add("final");
        }

        if (Modifier.isAbstract(access)) {
            accessLine.add("abstract");
        }

        return accessLine.stream()
                .filter(Objects::nonNull)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.joining(" "));
    }

    private static DiffModel<List<String>, List<DiffModel<MethodModel, MethodModel>>> compareMethod(
            MethodNode firstMethodNode, MethodNode secondMethodNode) {
        if (Objects.isNull(firstMethodNode)) {
            return DiffModel.<List<String>, List<DiffModel<MethodModel, MethodModel>>>builder()
                    .added(Collections.singletonList(getReadableMethodName(secondMethodNode)))
                    .build();
        }

        if (Objects.isNull(secondMethodNode)) {
            return DiffModel.<List<String>, List<DiffModel<MethodModel, MethodModel>>>builder()
                    .deleted(Collections.singletonList(getReadableMethodName(firstMethodNode)))
                    .build();
        }

        if (equals(firstMethodNode, secondMethodNode)) {
            return DiffModel.<List<String>, List<DiffModel<MethodModel, MethodModel>>>builder()
                    .unchanged(Collections.singletonList(getReadableMethodName(firstMethodNode)))
                    .build();
        }

        return DiffModel.<List<String>, List<DiffModel<MethodModel, MethodModel>>>builder()
                .updated(Collections.singletonList(transformMethodNode(firstMethodNode, secondMethodNode)))
                .build();
    }

    private static boolean equals(MethodNode firstNode, MethodNode secondNode) {
        return firstNode.name.equals(secondNode.name)
                && firstNode.desc.equals(secondNode.desc)
                && CollectionUtils.isEqualCollection(new HashSet<>(firstNode.exceptions), new HashSet<>(secondNode.exceptions));

    }

    private static String getReadableMethodName(MethodNode methodNode) {
        return getReadableName(methodNode.access, methodNode.name)
                + "(" +
                Arrays.stream(Type.getArgumentTypes(methodNode.desc))
                        .map(Type::getClassName)
                        .collect(Collectors.joining(",")) +
                ")" + ":" + Type.getReturnType(methodNode.desc).getClassName();

    }

    private static String getReadableName(int access, String name) {
        String readableAccess = getReadableAccess(access);
        if (readableAccess.isEmpty()) {
            return name;
        }
        return readableAccess + " " + name;
    }

    private static MethodModel transformMethodNode(MethodNode methodNode) {
        return MethodModel.builder()
                .methodName(methodNode.name)
                .returnType(Type.getReturnType(methodNode.desc).getClassName())
                .exceptions(transformToReadableClasses(methodNode.exceptions))
                .argumentTypes(Arrays.stream(Type.getArgumentTypes(methodNode.desc)).map(Type::getClassName).collect(Collectors.toUnmodifiableList()))
                .build();
    }

    private static DiffModel<MethodModel, MethodModel> transformMethodNode(MethodNode firstMethodNode, MethodNode secondMethodNode) {

        MethodModel.MethodModelBuilder unchangedMethodData = MethodModel.builder();
        MethodModel.MethodModelBuilder deletedMethodData = MethodModel.builder();
        MethodModel.MethodModelBuilder addedMethodData = MethodModel.builder();

        compareReturnType(firstMethodNode, secondMethodNode, unchangedMethodData, deletedMethodData, addedMethodData);
        compareAccess(firstMethodNode, secondMethodNode, unchangedMethodData, deletedMethodData, addedMethodData);

        unchangedMethodData.methodName(firstMethodNode.name);
        unchangedMethodData.argumentTypes(Arrays.stream(Type.getArgumentTypes(firstMethodNode.desc))
                .map(Type::getClassName)
                .collect(Collectors.toUnmodifiableList()));

        compareExceptions(firstMethodNode, secondMethodNode, unchangedMethodData, deletedMethodData, addedMethodData);

        return DiffModel.<MethodModel, MethodModel>builder()
                .name(firstMethodNode.name)
                .unchanged(unchangedMethodData.build())
                .deleted(deletedMethodData.build())
                .added(addedMethodData.build())
                .build();
    }

    private static void compareExceptions(MethodNode firstMethodNode, MethodNode secondMethodNode, MethodModel.MethodModelBuilder unchangedMethodData, MethodModel.MethodModelBuilder deletedMethodData, MethodModel.MethodModelBuilder addedMethodData) {
        List<String> deletedExceptions = new ArrayList<>(firstMethodNode.exceptions);
        List<String> unchangedReadableExceptions = new ArrayList<>();
        List<String> addedReadableExceptions = secondMethodNode.exceptions.stream()
                .filter(exceptionClass -> !(deletedExceptions.remove(exceptionClass)
                        && unchangedReadableExceptions.add(DiffUtil.getReadableClassName(exceptionClass))))
                .map(DiffUtil::getReadableClassName)
                .collect(Collectors.toUnmodifiableList());

        unchangedMethodData.exceptions(unchangedReadableExceptions);
        addedMethodData.exceptions(addedReadableExceptions);
        deletedMethodData.exceptions(deletedExceptions.stream()
                .map(DiffUtil::getReadableClassName)
                .collect(Collectors.toUnmodifiableList()));
    }

    private static void compareReturnType(MethodNode firstMethodNode, MethodNode secondMethodNode, MethodModel.MethodModelBuilder unchangedMethodData, MethodModel.MethodModelBuilder deletedMethodData, MethodModel.MethodModelBuilder addedMethodData) {
        Type firstReturnType = Type.getReturnType(firstMethodNode.desc);
        Type secondReturnType = Type.getReturnType(secondMethodNode.desc);

        if (firstReturnType.equals(secondReturnType)) {
            unchangedMethodData.returnType(firstReturnType.getClassName());
        } else {
            deletedMethodData.returnType(firstReturnType.getClassName());
            addedMethodData.returnType(secondReturnType.getClassName());
        }
    }

    private static void compareAccess(MethodNode firstMethodNode, MethodNode secondMethodNode, MethodModel.MethodModelBuilder unchangedMethodData, MethodModel.MethodModelBuilder deletedMethodData, MethodModel.MethodModelBuilder addedMethodData) {
        String firstAccess = getReadableAccess(firstMethodNode.access);
        String secondAccess = getReadableAccess(secondMethodNode.access);

        if (firstAccess.equals(secondAccess)) {
            unchangedMethodData.access(firstAccess);
        } else {
            deletedMethodData.access(firstAccess);
            addedMethodData.access(secondAccess);
        }
    }

    private static <T, U> DiffModel<List<T>, List<U>> reduceDiffModel(DiffModel<List<T>, List<U>> first, DiffModel<List<T>, List<U>> second) {
        Optional.ofNullable(second.getAdded())
                .ifPresent(first.getAdded()::addAll);
        Optional.ofNullable(second.getDeleted())
                .ifPresent(first.getDeleted()::addAll);
        Optional.ofNullable(second.getUpdated())
                .ifPresent(first.getUpdated()::addAll);
        Optional.ofNullable(second.getUnchanged())
                .ifPresent(first.getUnchanged()::addAll);
        return first;
    }

    private static List<String> transformToReadableClasses(List<String> classes) {
        return Stream.ofNullable(classes)
                .flatMap(List::stream)
                .map(Type::getObjectType)
                .map(Type::getClassName)
                .collect(Collectors.toUnmodifiableList());
    }


}
