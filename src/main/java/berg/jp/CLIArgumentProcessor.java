package berg.jp;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.util.HashSet;
import java.util.List;

public final class CLIArgumentProcessor {

    public boolean isStatic() {
        return isStatic;
    }

    public String getVisibility() {
        return visibility;
    }

    public static final class VisibilityValidator implements IParameterValidator {

        private static final List<String> visibilityNames = List.of(
                "package", "public", "private", "protected"
        );

        private static final HashSet<String> possibleVisibilities = new HashSet<>(visibilityNames);

        public void validate(String name, String value) throws ParameterException {
            if (value.isEmpty()) return;
            if (!possibleVisibilities.contains(value)) {
                throw new ParameterException(
                        "Parameter " + name + " should be one of " + visibilityNames
                                + " (found " + value + ")"
                );
            }
        }
    }

    public String getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getOutput() {
        return output;
    }

    public boolean isHelp() {
        return help;
    }

    @Parameter(
            names = {"-s", "--static"},
            description = "Whether the declared method is static"
    )
    private boolean isStatic = false;

    @Parameter(
            names = {"-v", "--visibility"},
            description = "The visibility of the target method",
            validateWith = VisibilityValidator.class
    )
    private String visibility = "package";

    @Parameter(
            names = {"-r", "--return-type"},
            description = "The return-type of the target method"
    )
    private String returnType = void.class.getSimpleName();

    @Parameter(
            names = {"-n", "--method-name"},
            required = true,
            description = "The name of the target method"
    )
    private String methodName;

    @Parameter(
            names = {"-o", "--output"},
            description = "The output-file (if not provided the output will go to stdout"
    )
    private String output;

    @Parameter(names = {"-h", "--help"}, description = "Show this message", help = true)
    private boolean help = false;

    public DefaultArgumentWrapper createDefaultArgumentWrapper() {
        return new DefaultArgumentWrapper(
                getVisibility(),
                getReturnType(),
                getMethodName(),
                isStatic()
        );
    }

    private CLIArgumentProcessor() {
    }

    public static CLIArgumentProcessor process(String[] argv) {
        var args = new CLIArgumentProcessor();
        JCommander jc = JCommander.newBuilder()
                .addObject(args)
                .build();
        boolean printHelp;
        int exitCode = 0;
        try {
            jc.parse(argv);
            printHelp = args.isHelp();
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            printHelp = true;
            exitCode = 2;
        }
        if (printHelp) {
            jc.usage();
            System.exit(exitCode);
        }
        return args;
    }

    private void addAttribute(StringBuilder sb, String name, String value) {
        sb.append(name);
        sb.append(": '");
        sb.append(value);
        sb.append("'\n");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PARSED ARGUMENTS:\n");

        addAttribute(sb, "visibility", getVisibility());
        addAttribute(sb, "is static", String.valueOf(isStatic()));
        addAttribute(sb, "return type", getReturnType());
        addAttribute(sb, "method name", getMethodName());
        addAttribute(sb, "output", getOutput());

        return sb.toString();
    }


}
