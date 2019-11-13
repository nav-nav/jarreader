package valtman.jar.reader.model.diff;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class MethodDiffModel {
    private final String name;
    private final List<String> arguments;
}
