package chalkbox.api;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Finish;
import chalkbox.api.annotations.GroupPipe;
import chalkbox.api.annotations.Output;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.config.ChalkboxConfig;
import chalkbox.api.config.ConfigParseException;
import chalkbox.api.config.ConfigParser;
import chalkbox.api.config.FieldAssigner;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
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
    private ChalkboxConfig config;
    private boolean hasError;

    private PrintStream outputStream = System.out;

    /**
     * Constructor a new ChalkBox instance without loading any configuration.
     *
     * This should only be
     */
    private ChalkBox() {

    }

    /**
     * Construct a new ChalkBox instance based on the configuration file.
     *
     * @param configuration Chalkbox configuration settings.
     */
    public ChalkBox(ChalkboxConfig configuration) {
        this.config = configuration;

        /* Ensure that all the required classes are defined */
        for (String clazz : new String[]{"collector", "processor", "output"}) {
            if (!config.isSet(clazz)) {
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
     * Set the output stream of running the chalkbox to the given print stream.
     *
     * @param stream Stream to output run output to.
     */
    public void setOutput(PrintStream stream) {
        outputStream = stream;
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
        /* Also check the processor dependency processors */
        if (clazz.isAnnotationPresent(Processor.class)) {
            Processor annotation = (Processor) clazz.getAnnotation(Processor.class);
            for (Class dependency : annotation.depends()) {
                validateConfigItems(dependency);
            }
        }
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
            if (!config.isSet(key)) {
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
            return Class.forName(config.value(classConfig));
        } catch (ClassNotFoundException cnf) {
            System.err.println("Unable to find " + classConfig + " class: "
                    + config.value(classConfig));
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
        for (Method method : clazz.getMethods()) {
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

    private void runPriors(Class<?> clazz, Object instance) {
        for (Method prior : methodsByAnnotation(clazz, Prior.class)) {
            try {
                if (prior.getParameterCount() > 0) {
                    prior.invoke(instance, config.toMap());
                } else {
                    prior.invoke(instance);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                hasError = true;
            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
                hasError = true;
            }
        }
    }

    private void runFinish(Class<?> clazz, Object instance) {
        for (Method prior : methodsByAnnotation(clazz, Finish.class)) {
            try {
                prior.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                hasError = true;
            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
                hasError = true;
            }
        }
    }

    private void assignConfigItems(Class<?> clazz, Object instance) {
        FieldAssigner assigner = FieldAssigner.getInstance(instance);

        for (Field field : fieldsByAnnotation(clazz, ConfigItem.class)) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);

            String key = annotation.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            if (!config.isSet(key)) {
                continue;
            }

            Method assign = FieldAssigner.getMethod(assigner, field);
            if (assign == null) {
                System.err.println("Unable to find assign method for config item type of " + field.getType());
                continue;
            }

            boolean result;
            try {
                result = (boolean) assign.invoke(assigner, field.get(instance), field, config.value(key));
            } catch (Exception e) {
                e.printStackTrace();
                hasError = true;
                return;
            }

            if (!result) {
                hasError = true;
            }
        }
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

        if (instance == null) {
            hasError = true;
            return null;
        }

        assignConfigItems(clazz, instance);
        runPriors(clazz, instance);

        return instance;
    }

    public void executeCollection(Class collectorClass) {
        if (!collectorClass.isAnnotationPresent(Collector.class)) {
            hasError = true;
            System.err.println("Collector class does not have @Collector annotation");
            return;
        }

        List<Method> collectors = methodsByAnnotation(collectorClass, DataSet.class);

        Object instance = initClass(collectorClass);
        if (hasError) {
            return;
        }

        for (Method collector : collectors) {
            try {
                PrintStream oldOut = System.out;
                System.setOut(outputStream);
                PrintStream oldErr = System.err;
                System.setErr(outputStream);
                Object result = collector.invoke(instance, config.toMap());
                System.setOut(oldOut);
                System.setErr(oldErr);
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

        runFinish(collectorClass, instance);
    }

    public void executeProcess(Class processorClass) {
        if (!processorClass.isAnnotationPresent(Processor.class)) {
            hasError = true;
            System.err.println("Processor class does not have @Processor annotation");
            return;
        }
        Processor annotation = (Processor) processorClass.getAnnotation(Processor.class);

        for (Class dependency : annotation.depends()) {
            executeProcess(dependency);
        }

        List<Method> pipes = methodsByAnnotation(processorClass, Pipe.class);

        Object instance = initClass(processorClass);
        if (hasError) {
            return;
        }

        for (Method pipe : pipes) {
            String stream = pipe.getAnnotation(Pipe.class).stream();
            List<Object> data = streams.get(stream);

            streams.put(stream, ProcessRunner.executeProcess(data, instance,
                    pipe, annotation.threads()));
        }

        List<Method> groupPipes = methodsByAnnotation(processorClass, GroupPipe.class);
        for (Method method : groupPipes) {
            String stream = method.getAnnotation(GroupPipe.class).stream();
            List<Object> data = streams.get(stream);

            try {
                method.invoke(instance, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        runFinish(processorClass, instance);
    }

    public void sendOutput(Class outputClass) {
        List<Method> outputs = methodsByAnnotation(outputClass, Output.class);

        Object instance = initClass(outputClass);
        if (hasError) {
            return;
        }

        for (Method output : outputs) {
            String stream = output.getAnnotation(Output.class).stream();
            try {
                output.invoke(instance, outputStream, streams.get(stream));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String classHelp(String className) {
        StringBuilder builder = new StringBuilder();
        builder.append(className).append(System.lineSeparator());
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException cnf) {
            return "Unable to find class: " + className;
        }

        Collector collector = (Collector) clazz.getAnnotation(Collector.class);
        if (collector != null) {
            builder.append(collector.description())
                    .append(System.lineSeparator());
        }
        Processor processor = (Processor) clazz.getAnnotation(Processor.class);
        StringBuilder dependants = new StringBuilder();
        if (processor != null) {
            builder.append(processor.description())
                    .append(System.lineSeparator());
            for (Class dependency : processor.depends()) {
                dependants.append("Depends on ").append(classHelp(dependency.getName()));
            }
        }

        for (Field field : fieldsByAnnotation(clazz, ConfigItem.class)) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);

            /* Get the key of the config item */
            String key = annotation.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            builder.append("\t").append(key).append(": ")
                    .append(annotation.description())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    public static void main(String[] args) throws ConfigParseException {
        if (args.length == 2) {
            if (!args[0].equals("help")) {
                System.err.println(USAGE);
                return;
            }

            System.out.println(classHelp(args[1]));
            return;
        }

        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        ChalkboxConfig config = ConfigParser.box().read(Paths.get(args[0]));
        ChalkBox box = new ChalkBox(config);
        box.run();
    }
}
