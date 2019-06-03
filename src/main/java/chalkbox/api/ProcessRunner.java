package chalkbox.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProcessRunner extends Thread {
    private List<Object> dataSegment;
    private List<Object> updatedSegment = new ArrayList<>();

    private Object instance;
    private Method method;

    public ProcessRunner(List<Object> dataSegment, Object instance, Method method) {
        this.dataSegment = dataSegment;
        this.instance = instance;
        this.method = method;
    }

    public List<Object> getResults() {
        return updatedSegment;
    }

    @Override
    public void run() {
        for (Object item : dataSegment) {
            try {
                updatedSegment.add(method.invoke(instance, item));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Object> executeProcess(List<Object> data, Object instance,
                                              Method method, int threadNumber) {
        List<Object> updated = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger();
        int threadCount = data.size() / threadNumber;
        int threadAmount = threadCount > 0 ? threadCount : 1;
        Collection<List<Object>> split = data.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / threadAmount))
                .values();

        List<ProcessRunner> threads = new ArrayList<>();
        for (List<Object> dataSegment : split) {
            ProcessRunner processRunner = new ProcessRunner(dataSegment, instance, method);
            threads.add(processRunner);
            processRunner.start();
        }

        for (ProcessRunner thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updated.addAll(thread.getResults());
        }

        return updated;
    }
}
