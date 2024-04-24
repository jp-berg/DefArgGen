package berg.jp;

public enum Visibility {
    PUBLIC,
    PRIVATE,
    PACKAGE,
    PROTECTED;

    public String toString(){
        if(this == Visibility.PACKAGE) return "";
        return this.name().toLowerCase();
    }
}
