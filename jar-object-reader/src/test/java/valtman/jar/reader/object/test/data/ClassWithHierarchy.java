package valtman.jar.reader.object.test.data;

import java.util.Comparator;
import java.util.function.Supplier;

public class ClassWithHierarchy extends ClassWithTwoMethods implements Comparator, Supplier {
    @Override
    public int compare(Object o1, Object o2) {
        return 0;
    }

    @Override
    public Object get() {
        return new Object();
    }
}
