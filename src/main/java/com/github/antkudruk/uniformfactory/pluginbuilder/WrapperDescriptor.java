package com.github.antkudruk.uniformfactory.pluginbuilder;

public class WrapperDescriptor<W> {
    private final String methodName;
    private final String fieldName;
    private final String classFactoryGeneratorFieldName;
    private final Class<W> wrapperClass;
    private final Class<? extends MetaClassFactory<W>> wrapperClassFactory;

    public WrapperDescriptor(
            String methodName,
            String fieldName,
            String classFactoryGeneratorFieldName,
            Class<W> wrapperClass,
            Class<? extends MetaClassFactory<W>> wrapperClassFactory) {
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.classFactoryGeneratorFieldName = classFactoryGeneratorFieldName;
        this.wrapperClass = wrapperClass;
        this.wrapperClassFactory = wrapperClassFactory;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getClassFactoryGeneratorFieldName() {
        return classFactoryGeneratorFieldName;
    }

    public Class<W> getWrapperClass() {
        return wrapperClass;
    }

    public Class<? extends MetaClassFactory<W>> getWrapperClassFactory() {
        return wrapperClassFactory;
    }
}