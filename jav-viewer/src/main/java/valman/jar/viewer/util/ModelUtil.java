package valman.jar.viewer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import valtman.jar.reader.model.MethodModel;
import valtman.jar.reader.model.diff.ClassDiffModel;
import valtman.jar.reader.model.diff.DiffModel;
import valtman.jar.reader.model.diff.JarDiffModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelUtil {

    private static final String CLASS = "classes";
    private static final String CLASS_DIFF = "class diffs";
    private static final String ANNOTATION = "annotations";
    private static final String ARGUMENT = "arguments";
    private static final String EXCEPTION = "exceptions";
    private static final String RETURN_TYPE = "return types";
    private static final String ACCESS = "access";
    private static final String METHOD = "methods";
    private static final String HIERARCHY = "hierarchy";
    private static final String ADDED = "Added";
    private static final String REMOVED = "Removed";
    private static final String UNCHANGED = "Unchanged";
    private static final String UPDATED = "Updated";

    private static <T> Optional<DefaultMutableTreeNode> transformCollections(List<T> collection, String type, Function<T, DefaultMutableTreeNode> transform) {
        if (CollectionUtils.isEmpty(collection)) {
            return Optional.empty();
        }
        DefaultMutableTreeNode classRootNode = new DefaultMutableTreeNode(type);
        collection.stream()
                .map(transform)
                .forEach(classRootNode::add);
        return Optional.of(classRootNode);
    }

    public static Optional<DefaultMutableTreeNode> transformString(String name) {
        return Objects.isNull(name) || name.isEmpty() ? Optional.empty() : Optional.of(new DefaultMutableTreeNode(name));
    }

    public static Optional<DefaultMutableTreeNode> transformToString(Object object) {
        return Objects.isNull(object) ? Optional.empty() : Optional.of(new DefaultMutableTreeNode(object.toString()));
    }

    public static <T> Optional<List<DefaultMutableTreeNode>> transformCollection(Collection<T> collection,
                                                                                 Function<T, Optional<DefaultMutableTreeNode>> transformation) {
        if (CollectionUtils.isEmpty(collection)) {
            return Optional.empty();
        }
        List<DefaultMutableTreeNode> resultCollection = collection.stream()
                .map(transformation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableList());
        return CollectionUtils.isNotEmpty(resultCollection) ? Optional.of(resultCollection) : Optional.empty();
    }

    public static <T, U> Optional<DefaultMutableTreeNode> transformDiffModel(DiffModel<T, U> diffModel, String name,
                                                                             Function<T, Optional<List<DefaultMutableTreeNode>>> tTransformation,
                                                                             Function<U, Optional<List<DefaultMutableTreeNode>>> uTransformation) {
        if (Objects.isNull(diffModel)) {
            return Optional.empty();
        }
        DefaultMutableTreeNode classDiffRootNode = new DefaultMutableTreeNode(Optional.ofNullable(name).orElse(diffModel.getName()));
        tTransformation.apply(diffModel.getUnchanged())
                .ifPresent(child -> addChildNode(classDiffRootNode, UNCHANGED, child));
        tTransformation.apply(diffModel.getAdded())
                .ifPresent(child -> addChildNode(classDiffRootNode, ADDED, child));
        tTransformation.apply(diffModel.getDeleted())
                .ifPresent(child -> addChildNode(classDiffRootNode, REMOVED, child));
        uTransformation.apply(diffModel.getUpdated())
                .ifPresent(child -> addChildNode(classDiffRootNode, UPDATED, child));

        return classDiffRootNode.isLeaf() ? Optional.empty() : Optional.of(classDiffRootNode);
    }

    private static void addChildNode(DefaultMutableTreeNode rootNode,
                                     String nodeName,
                                     List<DefaultMutableTreeNode> childNodes) {

        if (CollectionUtils.isNotEmpty(childNodes)) {
            DefaultMutableTreeNode innerNode = new DefaultMutableTreeNode(nodeName);
            childNodes.forEach(innerNode::add);
            rootNode.add(innerNode);
        }
    }

    private static Optional<DefaultMutableTreeNode> transformClassDiffModel(ClassDiffModel classDataModel) {
        if (Objects.isNull(classDataModel)) {
            return Optional.empty();
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(classDataModel.getName());
        transformDiffModel(classDataModel.getAnnotations(), ANNOTATION, ModelUtil::transformToStringCollection,
                ModelUtil::transformToStringCollection)
                .ifPresent(rootNode::add);
        transformDiffModel(classDataModel.getInterfaces(), HIERARCHY, ModelUtil::transformToStringCollection,
                ModelUtil::transformToStringCollection)
                .ifPresent(rootNode::add);
        transformDiffModel(classDataModel.getMethods(), METHOD, ModelUtil::transformToStringCollection,
                updatedMethods -> transformCollection(updatedMethods, ModelUtil::transformMethodDiffModel))
                .ifPresent(rootNode::add);
        return Optional.of(rootNode);
    }

    public static Optional<DefaultMutableTreeNode> transformMethodDiffModel(DiffModel<MethodModel, MethodModel> methodDiffModel) {
        return transformDiffModel(methodDiffModel, null, ModelUtil::transformMethodModel, ModelUtil::transformMethodModel);
    }

    public static Optional<List<DefaultMutableTreeNode>> transformMethodModel(MethodModel methodModel) {

        DefaultMutableTreeNode methodRootNode = new DefaultMutableTreeNode(methodModel.getMethodName());

        DefaultMutableTreeNode returnTypeNode = new DefaultMutableTreeNode(RETURN_TYPE);
        returnTypeNode.add(new DefaultMutableTreeNode(methodModel.getReturnType()));
        methodRootNode.add(returnTypeNode);

        transformString(methodModel.getReturnType())
                .ifPresent(access -> {
                    DefaultMutableTreeNode accessNode = new DefaultMutableTreeNode(RETURN_TYPE);
                    accessNode.add(access);
                    methodRootNode.add(accessNode);
                });

        transformToStringCollection(methodModel.getAnnotations())
                .ifPresent(annotations -> addChildNode(methodRootNode, ANNOTATION, annotations));
        transformToStringCollection(methodModel.getArgumentTypes())
                .ifPresent(arguments -> addChildNode(methodRootNode, ARGUMENT, arguments));
        transformToStringCollection(methodModel.getExceptions())
                .ifPresent(exceptions -> addChildNode(methodRootNode, EXCEPTION, exceptions));
        return Optional.of(Collections.singletonList(methodRootNode));
    }

    private static Optional<DefaultMutableTreeNode> getChildTree(String childName, List<String> childList) {
        if (CollectionUtils.isNotEmpty(childList)) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childName);
            childList
                    .forEach(annotation -> childNode.add(new DefaultMutableTreeNode(annotation)));
            return Optional.of(childNode);
        }
        return Optional.empty();
    }

    public static DefaultMutableTreeNode transformJarDiffModels(JarDiffModel diff) {
        if (Objects.isNull(diff)) {
            return new DefaultMutableTreeNode("Something goes wrong");
        }
        return transformDiffModel(diff, CLASS, ModelUtil::transformToStringCollection,
                updatedClasses -> transformCollection(updatedClasses, ModelUtil::transformClassDiffModel))
                .orElse(new DefaultMutableTreeNode("Nothing to show"));
    }

    private static Optional<List<DefaultMutableTreeNode>> transformToStringCollection(List<?> classNames) {
        return transformCollection(classNames, ModelUtil::transformToString);
    }
}
