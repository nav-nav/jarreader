package valtman.jar.reader.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ClassDataModel {
    private final List<String> hierarchy;
    private final List<String> annotations;
    private final List<MethodModel> methods;

}
