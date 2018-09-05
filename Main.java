package project1;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        LinkedList<VPLFile> files = new LinkedList<>();
//        files.add(new VPLFile("ex1a", 50));
//        files.addFirst(new VPLFile("ex1b", 50));
//        files.addFirst(new VPLFile("ex1c", 50));
//        files.addFirst(new VPLFile("ex1d", 100)); not 100 sure is correct
//        files.addFirst(new VPLFile("ex2", 100));
//files.addFirst(new VPLFile("ex3", 500));
//files.addFirst(new VPLFile("ex4", 200));
files.addFirst(new VPLFile("someUnitTests", 500));
        files.forEach(e -> tryMain(e));

    }

    public static void tryMain(VPLFile file) {
        try {
            VPL.main(file.get());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    static class VPLFile {
        final static String directory = System.getProperty("user.dir") + "/src/project1/";
        final String fileName;
        final Integer memorySize;

        public VPLFile(String fileName, Integer memorySize) {
            this.fileName = directory + fileName;
            this.memorySize = memorySize;
        }

        public String[] get() {
            return new String[]{this.fileName, this.memorySize.toString()};
        }
    }
}
