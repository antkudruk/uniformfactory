package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.*;
import lombok.SneakyThrows;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;

public class WrapperDescriptor<W> {
    private final String methodName;
    private final String adaptorField;
    private final String adaptorFactoryField;
    private final Class<W> wrapperClass;
    private final Class<? extends MetaClassFactory<W>> wrapperClassFactory;

    public WrapperDescriptor(
            String methodName,
            String adaptorField,
            String adaptorFactoryField,
            Class<W> wrapperClass,
            Class<? extends MetaClassFactory<W>> wrapperClassFactory) {
        this.methodName = methodName;
        this.adaptorField = adaptorField;
        this.adaptorFactoryField = adaptorFactoryField;
        this.wrapperClass = wrapperClass;
        this.wrapperClassFactory = wrapperClassFactory;
        validate();
    }

    private void validate() {
        if(adaptorField == null) {
            throw new AdaptorFieldNotSpecifiedException();
        }
    }

    @SneakyThrows(ReflectiveOperationException.class)
    void validateForOrigin(Class<?> originClass) {
        if(methodName == null) {
            throw new MethodNameNotSpecifiedException(adaptorField);
        }
        if(adaptorFactoryField == null) {
            throw new AdaptorFactoryFieldNotSpecifiedException(adaptorField);
        }
        if(wrapperClass == null) {
            throw new NoWrapperClassSpecifiedException(adaptorField);
        }
        if(wrapperClassFactory == null) {
            throw new NoClassFactoryException(adaptorField);
        }
        if(new TypeDescription
                .ForLoadedType(originClass)
                .getDeclaredMethods()
                .filter(ElementMatchers.named(methodName).and(ElementMatchers.takesNoArguments()))
                .size() == 0) {
            throw new WrongMethodNameException(methodName, originClass);
        }
        if(originClass.getDeclaredMethod(methodName).getClass().isAssignableFrom(wrapperClass)) {
            throw new GetWrapperMethodWrongTypeException(methodName, originClass, wrapperClass);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public String getAdaptorField() {
        return adaptorField;
    }

    public String getAdaptorFactoryField() {
        return adaptorFactoryField;
    }

    public Class<W> getWrapperClass() {
        return wrapperClass;
    }

    public Class<? extends MetaClassFactory<W>> getWrapperClassFactory() {
        return wrapperClassFactory;
    }
}
