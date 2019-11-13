package valtman.jar.reader.model.diff;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import valtman.jar.reader.model.MethodModel;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ClassDiffModel {
    private final String name;
    private final DiffModel<String, String> supperClass;
    private final DiffModel<String, String> access;
    private final DiffModel<List<String>, List<String>> interfaces;
    private final DiffModel<List<String>, List<String>> annotations;
    private final DiffModel<List<String>, List<DiffModel<MethodModel, MethodModel>>> methods;
}
