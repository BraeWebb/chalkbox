package chalkbox.java.conformance;

import chalkbox.api.files.FileLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceLoader extends ClassLoader {
    private File classDirectory;
    private List<String> files;

    public SourceLoader(String classDirectory) throws IOException {
        File file = new File(classDirectory);

        if (!file.exists() || !file.isDirectory()) {
            throw new IOException("Provided class directory does not exist");
        }

        FileLoader loader = new FileLoader(file.getPath(), "", ".class");
        loader.setRemoveSuffix(true);

        this.classDirectory = file;
        this.files = new ArrayList<>();
        for (String fileName : loader.loadFiles(file)) {
            this.files.add(fileName.replace("/", "."));
        }
    }

    public List<String> getSourceFiles() {
        return files;
    }

    public Map<String, Class> getClassMap() throws ClassNotFoundException {
        Map<String, Class> classes = new HashMap<>();
        for (String file : files) {
            classes.put(file, loadClass(file));
        }
        return classes;
    }

    private File getFile(String className) {
        return new File(classDirectory.getPath() + File.separator
                + className.replace(".", File.separator) + ".class");
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);

        if (loadedClass != null || !files.contains(name)) {
            return super.loadClass(name);
        }

        try {
            byte[] classData = Files.readAllBytes(getFile(name).toPath());

            return defineClass(name,
                    classData, 0, classData.length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
