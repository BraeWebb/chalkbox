package chalkbox2.api;

import java.io.IOException;

public interface Component {

    void init() throws Exception;

    void run(String... args);

    void after();
}
