package berg.jp;

import org.apache.commons.cli.*;

import java.lang.invoke.VarHandle;

public final class DawCli extends CommandLineInterface {

    public DawCli(String[] args) throws ParseException {
        super("DefArgGen", args);
    }

    @Override
    protected void addOptions() {
        super.addOptions();
        addOption("visibility", "The visibility of the target method", false);
        addOption("returnType", "The return-type of the target method");
        addOption("name", "The name of the target method");
        addOption("output", "The output-file (if not provided the output will go to stdout", false);
    }

    public Visibility getVisibility() throws ParseException {
        String value = getCommandline().getOptionValue("visibility");
        if(value == null) return Visibility.PACKAGE;
        return Visibility.valueOf(value.toUpperCase());
    }

    public String getReturnType() throws ParseException {
        return getValue("returnType");

    }

    public String getName() throws ParseException {
        return getValue("name");
    }

    public String getOutput() throws ParseException {
        return getCommandline().getOptionValue("output");
    }

}
