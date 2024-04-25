package DefArgGen.CLI;

import DefArgGen.Utils.DefaultArgumentWrapperIntern;

public class Runner {

    public static void main(String[] argv) throws Exception {

        var processor = CLIArgumentProcessor.process(argv);
        DefaultArgumentWrapperIntern daw = processor.createDefaultArgumentWrapper();
        if (processor.getOutput() != null) {
            daw.wrappersToFile(processor.getOutput());
        } else {
            daw.wrappersToStdout();
        }

    }
}
