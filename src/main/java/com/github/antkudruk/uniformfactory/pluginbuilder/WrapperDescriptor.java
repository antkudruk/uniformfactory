package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.*;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;

public class WrapperDescriptor<W> {
    private final String methodName;
    private final String wrapperField;
    private final String wrapperFactoryField;
    private final Class<W> wrapperClass;
    private final Class<? extends MetaClassFactory<W>> wrapperClassFactory;

    public WrapperDescriptor(
            String methodName,
            String wrapperField,
            String wrapperFactoryField,
            Class<W> wrapperClass,
            Class<? extends MetaClassFactory<W>> wrapperClassFactory) {
        this.methodName = methodName;
        this.wrapperField = wrapperField;
        this.wrapperFactoryField = wrapperFactoryField;
        this.wrapperClass = wrapperClass;
        this.wrapperClassFactory = wrapperClassFactory;
        validate();
    }

    private void validate() {
        if(wrapperField == null) {
            throw new WrapperFieldNotSpecifiedException();
        }
        if(methodName == null) {
            throw new MethodNameNotSpecifiedException(wrapperField);
        }
        if(wrapperFactoryField == null) {
            throw new WrapperFactoryFieldNotSpecifiedException(wrapperField);
        }
        if(wrapperClass == null) {
            throw new NoWrapperClassSpecifiedException(wrapperField);
        }
        if(wrapperClassFactory == null) {
            throw new NoClassFactoryException(wrapperField);
        }
    }

    void validateForOrigin(Class<?> originClass) {
        MethodList<MethodDescription.InDefinedShape> methods = new TypeDescription
                .ForLoadedType(originClass)
                .getDeclaredMethods()
                .filter(ElementMatchers.named(methodName).and(ElementMatchers.takesNoArguments()));
        if(methods.size() == 0) {
            throw new GetWrapperMethodNotExistsException(methodName, originClass);
        }
        if(!methods.getOnly().getReturnType().asErasure().isAssignableFrom(wrapperClass)) {
            throw new GetWrapperMethodWrongTypeException(methodName, originClass, wrapperClass);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public String getWrapperField() {
        return wrapperField;
    }

    public String getWrapperFactoryField() {
        return wrapperFactoryField;
    }

    public Class<W> getWrapperClass() {
        return wrapperClass;
    }

    public Class<? extends MetaClassFactory<W>> getWrapperClassFactory() {
        return wrapperClassFactory;
    }
}
