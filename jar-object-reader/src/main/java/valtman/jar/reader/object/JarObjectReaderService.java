package valtman.jar.reader.object;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import valtman.jar.reader.model.ClassDataModel;
import valtman.jar.reader.model.ClassModel;
import valtman.jar.reader.model.MethodModel;
import valtman.jar.reader.model.diff.JarDiffModel;
import valtman.jar.reader.object.util.DiffUtil;
import valtman.jar.reader.service.JarReaderService;
import valtman.jar.reader.util.ReaderUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JarObjectReaderService implements JarReaderService {

    private final Executor executor = Executors.newWorkStealingPool();

    public List<ClassModel> readJar(String path) {
        return parseJar(new File(path))
                .values()
                .stream()
                .map(classNode -> ClassModel.builder()
                        .className(classNode.name)
                        .classDataModel(ClassDataModel.builder()
                                .methods(Stream.ofNullable(classNode.methods)
                                        .flatMap(List::parallelStream)
                                        .map(methodNode -> MethodModel.builder()
                                                .methodName(methodNode.name)
                                                .returnType(methodNode.desc)
                                                .build())
                                        .collect(Collectors.toUnmodifiableList())
                                )
                                .build())
                        .build())
                .collect(Collectors.toList());


    }

    private Map<String, ClassNode> parseJar(File file) {
        return ReaderUtils.load(file).values()
                .parallelStream()
                .map(classReader -> {
                            ClassNode node = new ClassNode();
                            classReader.accept(node, 0);
                            return node;
                        }
                )
                .collect(Collectors.toMap(classNode -> classNode.name, classNode -> classNode));
    }

    @Override
    public JarDiffModel getDiff(File firstJar, File secondJar) {
        CompletableFuture<Map<String, ClassReader>> firstJarParser = CompletableFuture.supplyAsync(() -> ReaderUtils.load(firstJar), executor);
        CompletableFuture<Map<String, ClassReader>> secondJarParser = CompletableFuture.supplyAsync(() -> ReaderUtils.load(secondJar), executor);
        return CompletableFuture.allOf(firstJarParser, secondJarParser)
                .thenApplyAsync(v -> {
                    Map<String, ClassReader> firstClassReaderMap = firstJarParser.join();
                    Map<String, ClassReader> secondClassReaderMap = secondJarParser.join();
                    Set<String> classes = new HashSet<>(firstClassReaderMap.keySet());
                    classes.addAll(secondClassReaderMap.keySet());

                    List<JarDiffModel> classDiffModel = classes.parallelStream()
                            .map(classPath -> DiffUtil.compareClass(classPath, firstClassReaderMap.get(classPath), secondClassReaderMap.get(classPath)))
                            .collect(Collectors.toList());

                    return classDiffModel
                            .stream()
                            .reduce(JarDiffModel.jarDiffBuilder()
                                            .updated(new LinkedList<>())
                                            .added(new LinkedList<>())
                                            .unchanged(new LinkedList<>())
                                            .deleted(new LinkedList<>())
                                            .build(),
                                    (jarDiffModel, jarDiffModel2) -> {
                                        Optional.ofNullable(jarDiffModel2.getAdded())
                                                .ifPresent(jarDiffModel.getAdded()::addAll);
                                        Optional.ofNullable(jarDiffModel2.getDeleted())
                                                .ifPresent(jarDiffModel.getDeleted()::addAll);
                                        Optional.ofNullable(jarDiffModel2.getUpdated())
                                                .ifPresent(jarDiffModel.getUpdated()::addAll);
                                        Optional.ofNullable(jarDiffModel2.getUnchanged())
                                                .ifPresent(jarDiffModel.getUnchanged()::addAll);
                                        return jarDiffModel;
                                    });

                }, executor).join();
    }
}
