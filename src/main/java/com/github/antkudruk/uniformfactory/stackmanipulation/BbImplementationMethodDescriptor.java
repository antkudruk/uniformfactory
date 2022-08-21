package com.github.antkudruk.uniformfactory.stackmanipulation;

import com.github.antkudruk.uniformfactory.base.AbstractMethodDescriptorImpl;
import com.github.antkudruk.uniformfactory.base.Enhancer;
import com.github.antkudruk.uniformfactory.classfactory.ChildMethodDescriptionBuilderWrapper;
import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.MemberSelector;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.constant.FieldConstant;
import net.bytebuddy.implementation.bytecode.constant.MethodConstant;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Allows assigning ByteBuddy implementation to the method
 */
public class BbImplementationMethodDescriptor extends AbstractMethodDescriptorImpl {

    @NonNull
    private final Optional<Implementation.Composable> initiation;
    @NonNull
    private final Implementation implementation;

    public BbImplementationMethodDescriptor(BuilderInterface builder) {
        super(builder);
        this.implementation = builder.getImplementation();
        this.initiation = builder.getInitiation();


    }

    @Override
    public Enhancer getEnhancer(TypeDescription originType) {
        return new BbImplementationEnhancer();
    }

    interface BuilderInterface extends AbstractMethodDescriptorImpl.BuilderInterface {
        @NonNull
        Optional<Implementation.Composable> getInitiation();

        @NonNull
        Implementation getImplementation();
    }

    @SuppressWarnings("unchecked")
    @Getter
    private static class AbstractBuilder<T extends AbstractBuilder<T>>
            extends AbstractMethodDescriptorImpl.AbstractBuilder<T>
            implements BuilderInterface {

        private Optional<Implementation.Composable> initiation = Optional.empty();
        private Implementation implementation;

        public AbstractBuilder(Method wrapperMethod) {
            super(wrapperMethod);
        }

        public T setInitiation(Implementation.Composable initiation) {
            this.initiation = Optional.ofNullable(initiation);
            return (T) this;
        }

        public T setImplementation(Implementation implementation) {
            this.implementation = implementation;
            return (T) this;
        }

        public T methodConstant(MethodDescription methodDescription) {
            if (methodDescription != null) {
                setImplementation(new Implementation.Simple(
                        MethodConstant.of(methodDescription.asDefined()),
                        MethodReturn.REFERENCE
                ));
            } else {
                setImplementation(FixedValue.nullValue());
            }
            return (T) this;
        }

        public T fieldConstant(FieldDescription fieldDescription) {
            if (fieldDescription != null) {
                setImplementation(new Implementation.Simple(
                        new FieldConstant(fieldDescription.asDefined()),
                        MethodReturn.REFERENCE
                ));
            } else {
                setImplementation(FixedValue.nullValue());
            }
            return (T) this;
        }

        public T nullConstant() {
            setImplementation(FixedValue.nullValue());
            return (T) this;
        }

        @Override
        public MemberSelector getMemberSelector() {
            return new MemberSelector() {
            };
        }

        /**
         * Has no effect on BbImplementationMethodDescriptor instance
         *
         * @return this builder
         */
        public T setMemberSelector() {
            return (T) this;
        }

        @Override
        public BbImplementationMethodDescriptor build() {
            return new BbImplementationMethodDescriptor(this);
        }
    }

    /**
     * Created an instance of BbImplementationMethodDescription
     */
    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(Method wrapperMethod) {
            super(wrapperMethod);
        }

        @Override
        public BbImplementationMethodDescriptor build() {
            return new BbImplementationMethodDescriptor(this);
        }
    }

    /**
     * Method set up with ByteBuddy Implementation
     *
     * @param <W> Class of wrapper
     */
    public static class ShortcutBuilder<W>
            extends AbstractBuilder<ShortcutBuilder<W>> {

        @Delegate
        private final ChildMethodDescriptionBuilderWrapper<W> classFactoryReference;

        public ShortcutBuilder(
                ClassFactory.Builder<W> parentBuilder,
                Method wrapperMethod) {
            super(wrapperMethod);
            classFactoryReference = new ChildMethodDescriptionBuilderWrapper<>(parentBuilder, this);
        }
    }

    public class BbImplementationEnhancer implements Enhancer {
        @Override
        public Implementation.Composable addInitiation(Implementation.Composable implementation) {
            return initiation.map(implementation::andThen).orElse(implementation);
        }

        @Override
        public <W> DynamicType.Builder<W> addMethod(DynamicType.Builder<W> bbBuilder) {
            return bbBuilder.define(wrapperMethod)
                    .intercept(implementation);
        }
    }
}
