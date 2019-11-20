package valman.jar.viewer;

import lombok.Getter;
import valman.jar.viewer.util.ModelUtil;
import valtman.jar.reader.object.JarObjectReaderService;
import valtman.jar.reader.service.JarReaderService;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class Controller {
    private final JarDiffFrame jarDiffFrame;
    private final JarReaderService jarReaderService;
    private final AtomicReference<CompletableFuture> currentJob = new AtomicReference<>(new CompletableFuture());

    public Controller(JarDiffFrame jarDiffFrame, JarReaderService jarReaderService) {
        this.jarDiffFrame = jarDiffFrame;
        this.jarReaderService = jarReaderService;

        this.jarDiffFrame.setTreeValue(new DefaultMutableTreeNode("Jar is not selected"));
        this.jarDiffFrame.subscribeOnFileSelection((firstFile, secondFile) -> {
            if (Objects.isNull(firstFile) || Objects.isNull(secondFile)) {
                return;
            }
            this.jarDiffFrame.setTreeValue(new DefaultMutableTreeNode("Please wait"));
            try {
                //TODO avoid double cancellation while waiting for service result
//                currentJob.getAndSet(this.jarReaderService.getDiff(firstFile, secondFile)
//                        .thenAccept(jarDiffModel -> this.jarDiffFrame.setTreeValue(ModelUtil.transformJarDiffModels(jarDiffModel)))
//                .exceptionally(throwable -> {
//                    //TODO should check possible exception and avoid explore unexpected one
//                    return new DefaultMutableTreeNode(throwable.getCause().getMessage());
//                }))
//                        .cancel(true);
                this.jarDiffFrame.setTreeValue(ModelUtil.transformJarDiffModels(this.jarReaderService.getDiff(firstFile, secondFile)));
            } catch (Exception e) {
                e.printStackTrace();
                this.jarDiffFrame.setTreeValue(new DefaultMutableTreeNode("Something goes wrong"));
            }
        });
    }

    public static void main(String... args) {
        new Controller(new JarDiffFrame(), new JarObjectReaderService());
    }

}
