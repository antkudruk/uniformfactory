package com.github.antkudruk.uniformfactory.pluginbuilder;

import com.github.antkudruk.uniformfactory.pluginbuilder.exceptions.*;
import lombok.Getter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;

@Getter
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

    @SuppressWarnings("unchecked")
    private static class AbstractBuilder<T extends AbstractBuilder<T, W>,  W> {
        private final Class<W> wrapperClass;
        private String methodName;
        private String wrapperField = "wrapper";
        private String wrapperFactoryField = "wrapperFactory";
        private Class<? extends MetaClassFactory<W>> wrapperClassFactory;

        public AbstractBuilder(Class<W> wrapperClass) {
            this.wrapperClass = wrapperClass;
        }

        public T setMethodName(String methodName) {
            this.methodName = methodName;
            return (T) this;
        }

        public T setWrapperField(String wrapperField) {
            this.wrapperField = wrapperField;
            return (T) this;
        }

        public T setWrapperFactoryField(String wrapperFactoryField) {
            this.wrapperFactoryField = wrapperFactoryField;
            return (T) this;
        }

        public T setClassFactoryGenerator(Class<? extends MetaClassFactory<W>> wrapperClassFactory) {
            this.wrapperClassFactory = wrapperClassFactory;
            return (T) this;
        }

        public WrapperDescriptor<W> build() {
            return new WrapperDescriptor<>(
                    methodName,
                    wrapperField,
                    wrapperFactoryField,
                    wrapperClass,
                    wrapperClassFactory);
        }
    }

    public static class Builder<W> extends AbstractBuilder<Builder<W>, W> {
        public Builder(Class<W> wrapperClass) {
            super(wrapperClass);
        }
    }

    public static class ShortcutBuilder<P extends WrapperPlugin.Builder, W>
            extends AbstractBuilder<WrapperDescriptor.ShortcutBuilder<P, W>, W> {
        private final P parent;

        public ShortcutBuilder(P parent, Class<W> wrapperClass) {
            super(wrapperClass);
            this.parent = parent;
        }

        public P endWrapperDescriptor() {
            parent.addWrapperDescriptor(build());
            return parent;
        }
    }
}
