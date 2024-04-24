package berg.jp;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Objects;

public class CommandLineInterface {

    private final Options options = new Options();

    private CommandLine commandLine;
    protected ArrayList<String> optionStrings = new ArrayList<>();
    private final String[] args;
    private final String APPLICATION_NAME;

    public CommandLineInterface(String applicationName, String[] args) throws ParseException {
        this.APPLICATION_NAME = applicationName;
        this.args = args;

        addOptions();

        if(args.length == 0 || args.length == 1 && args[0].equals("-h") || args[0].equals("--help")){
            printHelpAndExit();
        }
    }

    protected void addOptions(){
        addOption("help", "Print this help-text", 0, false);
    }


    public void addOption(String opt, String description){
        addOption(opt, description, 1, true);
    }

    public void addOption(String opt, String description, boolean required){
        addOption(opt, description, 1, required);
    }

    public void addOption(String opt, String description, int numberOfArgs){
        addOption(opt, description, numberOfArgs, true);
    }

    public void addOption(String opt, String description, int numberOfArgs, boolean required){
        Objects.requireNonNull(description);
        if(description.trim().isEmpty()){
            throw new IllegalArgumentException("description is not defined");
        }
        Objects.requireNonNull(opt);
        if(opt.trim().isEmpty()){
            throw new IllegalArgumentException("longOpt is not defined");
        }
        var o = Option.builder();
        o.desc(description);

        if(opt.length() < 2){
            o.option(opt);
        }else{
            o.option(opt.substring(0, 1)).longOpt(opt);
        }

        if(numberOfArgs == 1){
            o.hasArg();
        } else if (numberOfArgs > 1) {
            o.numberOfArgs(numberOfArgs);
            o.valueSeparator();
        } else if (numberOfArgs < 0){
            o.hasArg();
            o.valueSeparator();
        }

        if(required){
            o.required();
        }

        options.addOption(o.build());
        optionStrings.add(opt);

    }

    protected CommandLine getCommandline() throws ParseException {
        if(commandLine == null) {
            CommandLineParser parser = new DefaultParser();
            commandLine = parser.parse(options, args);
        }
        return commandLine;
    }

    protected String getValue(String valueName) throws ParseException {
        String value = getCommandline().getOptionValue(valueName);
        if(value == null){
            throw new ParseException("No value for '" + valueName + "'");
        }
        return value;
    }

    public void printHelpAndExit(){
        new HelpFormatter().printHelp(APPLICATION_NAME, options, true);
        System.exit(0);
    }


}
