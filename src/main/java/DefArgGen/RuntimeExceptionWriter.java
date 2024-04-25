package DefArgGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

final class RuntimeExceptionWriter implements AutoCloseable {
    private final FileWriter fileWriter;


    RuntimeExceptionWriter(File file) throws IOException {
        this.fileWriter = new FileWriter(file);
    }

    public void write(String s) {
        try {
            fileWriter.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        fileWriter.close();
    }
}
