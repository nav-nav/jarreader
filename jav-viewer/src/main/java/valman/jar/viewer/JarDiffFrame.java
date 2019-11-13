package valman.jar.viewer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import valman.jar.viewer.util.FileHolder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Getter
public class JarDiffFrame extends JFrame {
    private final JScrollPane scrollPane = new JScrollPane();
    private final Renderer renderer = new Renderer();
    private final JTree tree = new JTree(new DefaultMutableTreeNode("No JAR selected"));

    private final FileHolder firstFile = new FileHolder();
    private final FileHolder secondFile = new FileHolder();

    private final List<BiConsumer<File, File>> listeners = new CopyOnWriteArrayList<>();
    private final Executor executor = Executors.newWorkStealingPool();

    public JarDiffFrame() throws HeadlessException {
        super("Jar classes diff viewer");

        Container header = new Container();
        header.setLayout(new GridLayout(1, 2));

        header.add(fileContainer(firstFile));
        header.add(fileContainer(secondFile));

        getContentPane().setLayout(new BorderLayout());
        tree.setCellRenderer(renderer);
        scrollPane.getViewport().add(tree);
        getContentPane().add(BorderLayout.CENTER, scrollPane);
        getContentPane().add(BorderLayout.NORTH, header);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(500, 500);
        setVisible(true);
    }

    private Container fileContainer(FileHolder fileHolder) {
        Container container = new Container();
        container.setLayout(new BorderLayout());

        Label filePath = new Label("Jar file is not selected");
        filePath.setVisible(true);
        new DropTarget(container, new DropFileListener(fileHolder, filePath));
        container.add(filePath, BorderLayout.NORTH);

        JButton button = new JButton("Open jar file");
        button.setVisible(true);
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            int chooserResult = chooser.showOpenDialog(button);
            while (chooserResult == JFileChooser.APPROVE_OPTION && !validFile(chooser.getSelectedFile())) {
                chooserResult = chooser.showOpenDialog(button);
            }

            if (chooserResult == JFileChooser.APPROVE_OPTION) {
                fileHolder.setFile(chooser.getSelectedFile());
                onFileSelect(fileHolder, filePath);
            }

        });
        container.add(button, BorderLayout.SOUTH);
        return container;
    }

    public void setTreeValue(DefaultMutableTreeNode treeNode) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(treeNode);
        model.reload();
    }

    private void onFileSelect(FileHolder file, Label filePath) {
        if (Objects.nonNull(file.getFile())) {
            filePath.setText(file.getFile().getPath());
            onFileUpdate(firstFile.getFile(), secondFile.getFile());
        }
    }

    private void onFileUpdate(File first, File second) {
        executor.execute(() -> listeners.forEach(consumer -> consumer.accept(first, second)));
    }

    public void subscribeOnFileSelection(BiConsumer<File, File> fileSelectionListener) {
        if (Objects.isNull(fileSelectionListener)) {
            throw new IllegalArgumentException("Null consumer is not allowed");
        }
        listeners.add(fileSelectionListener);
    }

    @RequiredArgsConstructor
    private class DropFileListener implements DropTargetListener {

        private final FileHolder fileHolder;
        private final Label filePath;

        public void dragEnter(DropTargetDragEvent e) {
        }

        public void dragExit(DropTargetEvent e) {
        }

        public void dragOver(DropTargetDragEvent e) {
        }

        public void dropActionChanged(DropTargetDragEvent e) {

        }

        public void drop(DropTargetDropEvent e) {
            try {
                // Accept the drop first, important!
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                // Get the files that are dropped as java.util.List
                java.util.List list = (java.util.List) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                // Now get the first file from the list,
                File file = (File) list.get(0);
                if (validFile(file)) {
                    fileHolder.setFile(file);
                    onFileSelect(fileHolder, filePath);
                }
            } catch (Exception ex) {
            }
        }

    }

    private static boolean validFile(File file) {
        if (!file.getName().endsWith(".jar")) {
            JOptionPane.showMessageDialog(null,
                    "The file " + file.getName()
                            + " is not jar file.", "Open Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private static class Renderer extends JLabel implements TreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            setText(value.toString());
            return this;
        }
    }

}
