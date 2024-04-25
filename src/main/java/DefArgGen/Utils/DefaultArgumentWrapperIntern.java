package DefArgGen.Utils;

import DefArgGen.Core.DefaultArgumentWrapper;
import DefArgGen.Core.Visibility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public sealed class DefaultArgumentWrapperIntern permits DefaultArgumentWrapper {

    public final int MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES = 22;

    private int argumentsWithDefaultValues = 0;

    private boolean isStatic = false, isFinal = false;
    private String visibility = "",
            signature;
    private final String methodBody,
            returnType,
            name;

    private final StringBuilder stringBuilder = new StringBuilder(),
                                stringBuilder2 = new StringBuilder();

    private static final HashSet<String> ILLEGAL_WORDS = new HashSet<>(
            List.of(
                    "abstract",
                    "continue",
                    "for",
                    "new",
                    "switch",
                    "assert",
                    "boolean",
                    "break",
                    "byte",
                    "case",
                    "catch",
                    "char",
                    "class",
                    "const",
                    "default",
                    "do",
                    "double",
                    "else",
                    "enum",
                    "extends",
                    "final",
                    "finally",
                    "float",
                    "goto",
                    "if",
                    "implements",
                    "import",
                    "instanceof",
                    "int",
                    "interface",
                    "long",
                    "native",
                    "package",
                    "private",
                    "protected",
                    "public",
                    "return",
                    "short",
                    "static",
                    "strictfp",
                    "super",
                    "synchronized",
                    "this",
                    "throw",
                    "throws",
                    "transient",
                    "try",
                    "void",
                    "volatile",
                    "while"
            )
    );

    private final ArrayList<Argument> arguments = new ArrayList<>(MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES);

    private final HashSet<String> usedIdentifiers = new HashSet<>(MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES);

    //Match beginning and end of line
    //Between beginning and end the string should start with an alphabetical character
    //Followed by zero or more alphanumeric or '_' or '$' characters
    private final static Pattern LEGAL_SYMBOLS = Pattern.compile("^\\p{Alpha}(\\p{Alnum}|_|\\$)*$");

    private void validateIdentifier(String identifier){
        Objects.requireNonNull(identifier);
        identifier = identifier.trim();
        if(identifier.isEmpty()){
            throw new IllegalArgumentException("Identifier is empty.");
        }

        if(!LEGAL_SYMBOLS.matcher(identifier).matches()){
            throw new IllegalArgumentException("Identifier '" + identifier + "' contains illegal symbols");
        }

        if(ILLEGAL_WORDS.contains(identifier)){
            throw new IllegalArgumentException("Identifier '" + identifier + "' is a reserved keyword.");
        }
    }


    public DefaultArgumentWrapperIntern(String returnType, String name) {
        this(Visibility.PACKAGE.toString(), returnType, name);
    }

    public DefaultArgumentWrapperIntern(String visibility, String returnType, String name) {
        Objects.requireNonNull(visibility);
        Objects.requireNonNull(returnType);
        validateIdentifier(name);

        this.visibility = visibility;
        this.returnType = returnType;
        this.name = name;

        if(returnType.equals("void")){
            methodBody = "){\n\t" + name + "(";
        }else{
            methodBody = "){\n\treturn " + name + "(";
        }
    }

    public DefaultArgumentWrapperIntern setStatic() {
        isStatic = true;
        return this;
    }

    private boolean isStatic() {
        return isStatic;
    }

    public DefaultArgumentWrapperIntern setFinal() {
        isFinal = true;
        return this;
    }

    private boolean isFinal() {
        return isFinal;
    }

    private String getVisibility() {
        return visibility;
    }

    private String getReturnType() {
        return returnType;
    }

    private String getName() {
        return name;
    }

    private String getSignature() {
        if (signature == null) {
            stringBuilder.setLength(0);
            stringBuilder.append(getVisibility());
            if (isStatic()) stringBuilder.append(" static");
            if (isFinal()) stringBuilder.append(" final");
            stringBuilder.append(" ");
            stringBuilder.append(getReturnType());
            stringBuilder.append(" ");
            stringBuilder.append(getName());
            stringBuilder.append("(");
            signature = stringBuilder.toString();
        }
        return signature;
    }


    public DefaultArgumentWrapperIntern addArgument(String type, String name, String value) {
        Objects.requireNonNull(type);
        validateIdentifier(name);
        if(usedIdentifiers.contains(name)){
            throw new IllegalArgumentException("Idetifier '" + name + "' is already in use");
        }
        if(value != null){
            if(++argumentsWithDefaultValues >= MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES){
                throw new IllegalArgumentException("Using more than " + MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES + " arguments with default values is restricted. When generating permutations of the method with " + MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES + " default-valued-arguments the resulting output will probably contain more than a gigabyte of method definitions, which is probably not what you want.");
            }
        }
        arguments.add(new Argument(type, name, value));
        usedIdentifiers.add(name);
        return this;
    }

    public DefaultArgumentWrapperIntern addArgument(String type, String name) {
        return addArgument(type, name, null);
    }

    private void trimTrailingCommas(StringBuilder sb){
        if(sb.charAt(sb.length()-1) == ' ' && sb.charAt(sb.length()-2) == ','){
            sb.delete(sb.length()-2, sb.length());
        }
    }

    private String constructMethod(ArrayList<Argument> arguments){
        stringBuilder.setLength(0);
        stringBuilder.append(getSignature());

        stringBuilder2.setLength(0);
        stringBuilder2.append(methodBody);

        for(Argument argument: arguments){
            if(argument.getValue() == null){
                stringBuilder.append(argument.type);
                stringBuilder.append(" ");
                stringBuilder.append(argument.name);
                stringBuilder.append(", ");
                stringBuilder2.append(argument.name);
                stringBuilder2.append(", ");
            }else{
                stringBuilder2.append(argument.getValue());
                stringBuilder2.append(", ");
            }
        }

        trimTrailingCommas(stringBuilder);
        trimTrailingCommas(stringBuilder2);
        stringBuilder.append(stringBuilder2);
        stringBuilder.append(");\n}\n\n");
        return stringBuilder.toString();
    }


    private void exploreCombinations(Consumer<String> consumer, ArrayList<Argument> arguments, int currentPos){
        if(currentPos >= arguments.size()) return;
        var arg = arguments.get(currentPos);
        if(arg.getValue() != null){
            exploreCombinations(consumer, arguments, currentPos +1);
            arguments.set(currentPos, arg.withNoValue());
            consumer.accept(constructMethod(arguments));
            exploreCombinations(consumer, arguments, currentPos +1);
            arguments.set(currentPos, arg);
        }else{
            exploreCombinations(consumer, arguments, currentPos + 1);
        }

    }

    public void generateWrappers(Consumer<String> consumer){
        if(arguments.isEmpty()){
            throw new IllegalStateException("No Arguments provided");
        }
        var args = new ArrayList<Argument>(this.arguments.size());
        for(Argument argument: arguments){args.add(argument.clone());}
        consumer.accept(constructMethod(args));
        exploreCombinations(consumer, args, 0);
    }

    public String wrappersToString(){
        var g = new StringGatherer();
        generateWrappers(g::accept);
        return g.toString();
    }

    public void wrappersToStdout(){
        generateWrappers(System.out::print);
    }

    public void wrappersToFile(String filepath) throws Exception {
        Objects.requireNonNull(filepath);
        if (filepath.isBlank()) {
            throw new IllegalArgumentException("filepath is not defined");
        }
        wrappersToFile(new File(filepath));
    }

    public void wrappersToFile(File file) throws Exception {
        Objects.requireNonNull(file);
        try (RuntimeExceptionWriter writer = new RuntimeExceptionWriter(file)) {
            generateWrappers(writer::write);
        }
    }



}
