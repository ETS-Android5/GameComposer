package de.mirkosertic.gameengine.type;

public abstract class Method<T> {

    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] arguments;

    public Method(String aName, Class<?> aReturnType, Class<?>[] aArguments) {
        name = aName;
        returnType = aReturnType;
        arguments = aArguments;
    }

    public String getName() {
        return name;
    }

    public abstract Object invoke(T aTarget, Object[] aArguments);
}
