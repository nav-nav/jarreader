package valtman.jar.reader.model.diff;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class JarDiffModel extends DiffModel<List<String>, List<ClassDiffModel>> {

    @Builder(builderMethodName = "jarDiffBuilder")
    public JarDiffModel(String name, List<String> added, List<ClassDiffModel> updated, List<String> deleted, List<String> unchanged) {
        super(name, added, updated, deleted, unchanged);
    }
}
