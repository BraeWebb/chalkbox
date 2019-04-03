package chalkbox.api;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.annotations.Parser;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChalkBox {
    private Map<String, List<Object>> streams = new HashMap<>();
    private Class collector;
    private Class processor;
    private boolean hasError;

    public ChalkBox(String collector, String processor) {
        try {
            this.collector = Class.forName(collector);
        } catch (ClassNotFoundException cnf) {
            System.err.println("Unable to find collector class: " + collector);
            hasError = true;
        }

        try {
            this.processor = Class.forName(processor);
        } catch (ClassNotFoundException cnf) {
            System.err.println("Unable to find collector class: " + processor);
            hasError = true;
        }
    }

    public void run(String collectorParams, String processorParams) {
        if (!hasError) {
            executeCollection(collector, collectorParams.split(" "));
            executeProcess(processor, processorParams.split(" "));
        }
    }

    public void executeCollection(Class collectorClass, String[] args) {
        if (!collectorClass.isAnnotationPresent(Collector.class)) {
            System.err.println("Collector class does not have @Collector annotation");
            return;
        }

        List<Method> parsers = new ArrayList<>();
        List<Method> collectors = new ArrayList<>();
        for (Method method : collectorClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DataSet.class)) {
                collectors.add(method);
            }
            if (method.isAnnotationPresent(Parser.class)) {
                parsers.add(method);
            }
        }

        Object instance;
        try {
            instance = collectorClass.getConstructor().newInstance();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        for (Method parser : parsers) {
            try {
                Object result = parser.invoke(instance, new Object[]{args});
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e);
            }
        }

        for (Method collector : collectors) {
            try {
                Object result = collector.invoke(instance, new Object[]{args});
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

    public void executeProcess(Class processorClass, String[] args) {
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
            executeProcess(dependency, args);
        }

        List<Method> pipes = new ArrayList<>();
        for (Method method : processorClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Pipe.class)) {
                pipes.add(method);
            }
        }

        Object instance;
        try {
            instance = processorClass.getConstructor().newInstance();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }

        for (Method pipe : pipes) {
            String stream = pipe.getAnnotation(Pipe.class).stream();
            List<Object> data = streams.get(stream);

            for (Object item : data) {
                try {
                    Object result = pipe.invoke(instance, item);
                    System.out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        CommandLine config = Configurations.parseArgs(args);
        if (config == null) {
            return;
        }

        ChalkBox run = new ChalkBox(
                config.getOptionValue("collector"),
                config.getOptionValue("processor"));

        run.run(config.getOptionValue("collector:args", ""),
                config.getOptionValue("processor:args", ""));
    }
}


class Configurations {
    private static Options buildOptions() {
        Options options = new Options();

        Option collector = Option.builder().longOpt("collector")
                .desc("Collector class to gather submissions")
                .argName("collector").required().hasArg().build();
        options.addOption(collector);

        Option processor = Option.builder().longOpt("processor")
                .desc("Processor class to gather submissions")
                .argName("processor").required().hasArg().build();
        options.addOption(processor);

        Option collectorArgs = Option.builder().longOpt("collector:args")
                .desc("Arguments for the collector")
                .argName("collector:args").hasArg().build();
        options.addOption(collectorArgs);

        Option processorArgs = Option.builder().longOpt("processor:args")
                .desc("Arguments for the processor")
                .argName("processor:args").hasArg().build();
        options.addOption(processorArgs);

        return options;
    }

    public static CommandLine parseArgs(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine config;
        try {
            config = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println("Invalid Usage: " + exp.getMessage());
            printHelp(options);
            return null;
        }

        if (config.hasOption("help")) {
            printHelp(options);
            return null;
        }

        if (config.getArgs().length > 1) {
            System.err.println("Invalid Usage: Extra arguments given");
            printHelp(options);
            return null;
        }

        return config;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("chalkbox",
                "Collect and process submissions",
                options, "", true);
    }
}