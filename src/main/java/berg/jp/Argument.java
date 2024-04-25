package berg.jp;

final class Argument implements Cloneable {
    private String string;

    public final String type;
    public final String name;

    private String value;


    Argument(String type, String name) {
        this.type = type;
        this.name = name;
    }

    Argument(String type, String name, String value) {
        this(type, name);
        if (value != null && value.isEmpty()) {
            value = "\"\"";
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Argument withNoValue() {
        return new Argument(type, name);
    }

    public Argument clone() {
        return new Argument(type, name, value);
    }


    public String toString() {
        if (string == null) {
            string = type + " " + name;
        }
        return string;
    }
}
