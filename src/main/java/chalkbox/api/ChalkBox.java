package chalkbox.api;

import chalkbox.api.annotations.Asset;
import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Output;
import chalkbox.api.annotations.Parser;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChalkBox {
    private static final String USAGE = "Incorrect usage:" + System.lineSeparator()
            + "\tchalkbox <box file>" + System.lineSeparator()
            + "\tchalkbox help <class>";

    private Map<String, List<Object>> streams = new HashMap<>();
    private Class collector;
    private Class processor;
    private Class output;
    private Map<String, String> config = new HashMap<>();
    private boolean hasError;

    /**
     * Construct a new ChalkBox instance based on the configuration file.
     *
     * @param configuration Path to a chalkbox configuration file.
     */
    public ChalkBox(String configuration) {
        /* Attempt to read the configuration file */
        try {
            loadConfig(configuration);
        } catch (IOException e) {
            System.err.println("Unable to read config file: " + configuration);
            hasError = true;
            return;
        }

        /* Ensure that all the required classes are defined */
        for (String clazz : new String[]{"collector", "processor", "output"}) {
            if (!config.containsKey(clazz)) {
                System.err.println("Configuration has no " + clazz + " class");
                hasError = true;
                return;
            }
        }

        this.collector = loadClass("collector");
        this.processor = loadClass("processor");
        this.output = loadClass("output");

        validateConfigItems(collector);
        validateConfigItems(processor);
        validateConfigItems(output);
    }

    /**
     * Validate that the config file has all of the required config items.
     *
     * Checks that all of the {@link ConfigItem} annotations which are required
     * have a value in the config file.
     *
     * @param clazz The class to search for {@link ConfigItem}'s within.
     */
    private void validateConfigItems(Class clazz) {
        for (Field field : fieldsByAnnotation(clazz, ConfigItem.class)) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);

            /* Only focus on required config items */
            if (!annotation.required()) {
                continue;
            }

            /* Get the key of the config item */
            String key = annotation.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            /* Print an error if the key is missing */
            if (!config.containsKey(key)) {
                System.err.println("Config file missing value for "
                        + key + " required by " + clazz.getName());
                System.err.println(key + ": " + annotation.description());
                hasError = true;
            }
        }
    }

    /**
     * Load a class based on the name of the class in the config file.
     *
     * For example, with config file demonstrated below
     * <pre>
     * myClass=package.MyClass
     * </pre>
     * The method call {@code loadClass("myClass")} would load the class
     * {@code package.MyClass}.
     *
     * @param classConfig The configuration option to look for.
     * @return The loaded Class object.
     */
    private Class loadClass(String classConfig) {
        try {
            return Class.forName(config.get(classConfig));
        } catch (ClassNotFoundException cnf) {
            System.err.println("Unable to find " + classConfig + " class: "
                    + config.get(classConfig));
            return null;
        }
    }

    public void run() {
        if (!hasError) {
            executeCollection(collector);
        }

        if (!hasError) {
            executeProcess(processor);
        }

        if (!hasError) {
            sendOutput(output);
        }
    }

    private static List<Method> methodsByAnnotation(Class clazz,
                                                    Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                methods.add(method);
            }
        }
        return methods;
    }

    private static List<Field> fieldsByAnnotation(Class clazz,
                                                  Class<? extends Annotation> annotation) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(annotation)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private void loadConfig(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("#")) {
                line = reader.readLine();
                continue;
            }
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                config.put(parts[0], parts[1]);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    // TODO add helpful error messages for each thing that can go wrong
    private Object initClass(Class<?> clazz) {
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        for (Field field : fieldsByAnnotation(clazz, ConfigItem.class)) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);

            String key = annotation.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            try {
                field.set(instance, config.get(key));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public void executeCollection(Class collectorClass) {
        if (!collectorClass.isAnnotationPresent(Collector.class)) {
            hasError = true;
            System.err.println("Collector class does not have @Collector annotation");
            return;
        }

        List<Method> parsers = methodsByAnnotation(collectorClass, Parser.class);
        List<Method> collectors = methodsByAnnotation(collectorClass, DataSet.class);

        Object instance = initClass(collectorClass);
        if (instance == null) {
            hasError = true;
            return;
        }

        for (Method parser : parsers) {
            try {
                Object result = parser.invoke(instance, config);
                System.out.println(result);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                hasError = true;
                return;
            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
                hasError = true;
                return;
            }
        }

        for (Method collector : collectors) {
            try {
                Object result = collector.invoke(instance, config);
                if (!(result instanceof List)) {
                    System.err.println("wtf dude");
                    return;
                }
                List<Object> data = (List<Object>) result;
                String stream = collector.getAnnotation(DataSet.class).stream();
                streams.put(stream, data);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e);
            }
        }
    }

    public void executeProcess(Class processorClass) {
        if (!processorClass.isAnnotationPresent(Processor.class)) {
            System.err.println("Processor class does not have @Processor annotation");
            return;
        }
        Processor annotation = (Processor) processorClass.getAnnotation(Processor.class);

        for (Class dependency : annotation.depends()) {
            if (processorClass.equals(dependency)) {
                System.err.println("Circular process dependency detected in " + processorClass);
                return;
            }
            executeProcess(dependency);
        }

        List<Method> assets = methodsByAnnotation(processorClass, Asset.class);
        List<Method> pipes = methodsByAnnotation(processorClass, Pipe.class);

        Object instance = initClass(processorClass);
        if (instance == null) {
            hasError = true;
            return;
        }

        for (Method asset : assets) {
            try {
                asset.invoke(instance, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Method pipe : pipes) {
            String stream = pipe.getAnnotation(Pipe.class).stream();
            List<Object> data = streams.get(stream);

            streams.put(stream, ProcessRunner.executeProcess(data, instance, pipe));
        }
    }

    public void sendOutput(Class outputClass) {
        List<Method> outputs = methodsByAnnotation(outputClass, Output.class);

        Object instance = initClass(outputClass);
        if (instance == null) {
            hasError = true;
            return;
        }

        PrintStream outFile;
        try {
            outFile = new PrintStream(new FileOutputStream("./sample/hardcoded"));
        } catch (IOException e) {
            System.err.println("Unable to open output stream");
            return;
        }

        for (Method output : outputs) {
            String stream = output.getAnnotation(Output.class).stream();
            try {
                output.invoke(instance, outFile, streams.get(stream));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        outFile.close();
    }

    public static String classHelp(String className) {
        StringBuilder builder = new StringBuilder();
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException cnf) {
            return "Unable to find class: " + className;
        }

        for (Field field : fieldsByAnnotation(clazz, ConfigItem.class)) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);

            /* Get the key of the config item */
            String key = annotation.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            builder.append(key).append(": ").append(annotation.description())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            if (!args[0].equals("help")) {
                System.err.println(USAGE);
                return;
            }

            System.out.println("Config Items for " + args[1]);
            System.out.println();
            System.out.println(classHelp(args[1]));
            return;
        }

        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        ChalkBox box = new ChalkBox(args[0]);
        box.run();
    }
}
