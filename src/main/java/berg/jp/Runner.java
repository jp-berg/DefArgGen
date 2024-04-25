package berg.jp;

public class Runner {

    public static void main(String[] argv) {

        var processor = CLIArgumentProcessor.process(argv);
        System.out.println(processor);

    }
}
