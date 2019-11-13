package valtman.jar.reader.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ClassModel {
    private final String className;
    private final ClassDataModel classDataModel;
}
