package berg.jp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class DefaultArgumentWrapper {

    public final int MAX_NUMBER_ARGUMENTS_WITH_DEFAULT_VALUES = 22;

    private int argumentsWithDefaultValues = 0;

    private boolean isStatic = false;
    private String visibility = "";

    private final String signature,
                        methodBody;

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

    public DefaultArgumentWrapper(Visibility v, Class returnType, String name, boolean isStatic){
        this(v.toString(), returnType.getSimpleName(), name, isStatic);
    }

    public DefaultArgumentWrapper(Visibility v, Class returnType, String name){
        this(v, returnType, name, false);
    }

    public DefaultArgumentWrapper(Class returnType, String name){
        this(Visibility.PACKAGE, returnType, name);
    }

    public DefaultArgumentWrapper(Class returnType, String name, boolean isStatic){
        this(Visibility.PACKAGE, returnType, name, isStatic);
    }

    DefaultArgumentWrapper(String returnType, String name){
        this(Visibility.PACKAGE.toString(), returnType, name);
    }

    DefaultArgumentWrapper(String returnType, String name, boolean isStatic){
        this(Visibility.PACKAGE.toString(), returnType, name, isStatic);
    }

    DefaultArgumentWrapper(String visibility, String returnType, String name){
        this(visibility, returnType, name, false);
    }

    DefaultArgumentWrapper(String visibility, String returnType, String name, boolean isStatic){
        Objects.requireNonNull(visibility);
        Objects.requireNonNull(returnType);
        validateIdentifier(name);

        this.isStatic = isStatic;
        this.visibility = visibility;
        String signature = visibility;
        signature = (isStatic) ? signature + " static" : signature;
        signature += " " + returnType + " " + name + "(";
        this.signature = signature;

        if(returnType.equals("void")){
            methodBody = "){\n\t" + name + "(";
        }else{
            methodBody = "){\n\treturn " + name + "(";
        }
    }

    private boolean isStatic() {
        return isStatic;
    }

    private String getVisibility() {
        return visibility;
    }

    private String getSignature() {
        return signature;
    }

    public DefaultArgumentWrapper addArgument(Class type, String name, String value) {
        return addArgument(type.getSimpleName(), name, value);

    }

    public DefaultArgumentWrapper addArgument(Class type, String name) {
        return addArgument(type.getSimpleName(), name);

    }

    DefaultArgumentWrapper addArgument(String type, String name, String value) {
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

    DefaultArgumentWrapper addArgument(String type, String name) {
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
        if(filepath.trim().isEmpty()){
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
