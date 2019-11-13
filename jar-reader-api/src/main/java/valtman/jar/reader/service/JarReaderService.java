package valtman.jar.reader.service;

import valtman.jar.reader.model.diff.JarDiffModel;

import java.io.File;

public interface JarReaderService {

    JarDiffModel getDiff(File firstFile, File secondFile);
}
