package valtman.jar.reader.object.test.data;

/**
 * Test java class to create .class files
 */
public class ClassWithTwoMethods {

    public String method(Object arg, int arg2, Long arg3) {
        return arg.toString() + arg2 + arg3;
    }

    //would be deleted
    private void methodToUpdate() throws Exception {
        throw new IllegalArgumentException();
    }
    //
//    would be updated to
//    protected void methodToUpdate() {
//        System.out.println("test");
//    }

//    void methodToUpdate(Object[] arg) {}

}
