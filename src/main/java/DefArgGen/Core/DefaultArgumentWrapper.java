package DefArgGen.Core;

import DefArgGen.Utils.DefaultArgumentWrapperIntern;

public final class DefaultArgumentWrapper extends DefaultArgumentWrapperIntern {

    public DefaultArgumentWrapper(Visibility v, Class returnType, String name) {
        super(v.toString(), returnType.getSimpleName(), name);
    }

    public DefaultArgumentWrapper(Class returnType, String name) {
        super(Visibility.PACKAGE.toString(), returnType.getSimpleName(), name);
    }

    public DefaultArgumentWrapperIntern addArgument(Class type, String name, String value) {
        return super.addArgument(type.getSimpleName(), name, value);

    }

    public DefaultArgumentWrapperIntern addArgument(Class type, String name) {
        return super.addArgument(type.getSimpleName(), name);

    }

}
