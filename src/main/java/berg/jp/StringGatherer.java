package berg.jp;

import java.util.ArrayList;

final class StringGatherer {
    private final ArrayList<String> strings = new ArrayList<>();
    private final StringBuilder stringBuilder = new StringBuilder();


    public void accept(String s) {
        strings.add(s);
    }

    public String toString() {
        int minStrLength = 0;
        for (String method : strings) {
            minStrLength += method.length();
        }

        stringBuilder.setLength(0);
        stringBuilder.ensureCapacity(minStrLength);
        for (String method : strings) {
            stringBuilder.append(method);
        }

        return stringBuilder.toString();
    }
}
