package valtman.jar.reader.model.diff;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class DiffModel<T, U> {
    private final String name;
    private final T added;
    private final U updated;
    private final T deleted;
    private final T unchanged;
}
