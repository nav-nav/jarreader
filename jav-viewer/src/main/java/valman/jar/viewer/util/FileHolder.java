package valman.jar.viewer.util;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class FileHolder {
    private volatile File file;
}
