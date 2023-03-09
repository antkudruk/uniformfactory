package com.github.antkudruk.example.gameexample.gameobject;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.uniformfactory.methodcollection.ElementFactory;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedFieldSelector;
import com.github.antkudruk.uniformfactory.methodcollection.seletor.SpecifiedMethodSelector;
import com.github.antkudruk.example.gameexample.gameengine.Node;
import lombok.SneakyThrows;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.function.Function;

public class GameObjectClassFactory {
    @SneakyThrows(ReflectiveOperationException.class)
    public ClassFactory<GameObjectAdapter> getClassFactory() {
        return new ClassFactory
                .Builder<>(GameObjectAdapter.class)

                .addMethodMap(GameObjectAdapter.class.getMethod("nodeSetters"), NodeSetter.class)
                .annotationMapElementSource()
                .setMarkerAnnotation(JmeObject.class)
                .setMethodKeyGetter(e -> e.getDeclaredAnnotations().ofType(JmeObject.class).load().nodeName())
                .setFieldKeyGetter(e -> e.getDeclaredAnnotations().ofType(JmeObject.class).load().nodeName())
                .setElementFactory(
                        new ElementFactory<>() {
                            @Override
                            @SneakyThrows(ReflectiveOperationException.class)
                            public ClassFactory<? extends NodeSetter> getFieldElement(TypeDescription origin, FieldDescription fieldDescription) throws ClassGeneratorException {
                                return new ClassFactory
                                        .Builder<>(NodeSetter.class)
                                        .addSetter(NodeSetter.class.getDeclaredMethod("setNode", Node.class))
                                        .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                                        .parameterSource(Node.class, 0)
                                        .applyToAny()
                                        .addSuper(Node.class, t -> t)
                                        .finishParameterDescription()
                                        .endMethodDescription()
                                        .addByteBuddyImplementation(NodeSetter.class.getDeclaredMethod("nodeType"))
                                        .typeConstant(fieldDescription.getType().asErasure())
                                        .endMethodDescription()
                                        .build();
                            }

                            @Override
                            @SneakyThrows(ReflectiveOperationException.class)
                            public ClassFactory<? extends NodeSetter> getMethodElement(TypeDescription origin, MethodDescription methodDescription) throws ClassGeneratorException {
                                return new ClassFactory
                                        .Builder<>(NodeSetter.class)
                                        .addMethodSingleton(NodeSetter.class.getDeclaredMethod("setNode", Node.class), void.class)
                                        .setMemberSelector(new SpecifiedMethodSelector(methodDescription))
                                        .parameterSource(Node.class, 0)
                                        .applyToAny()
                                        .addSuper(Node.class, t -> t)
                                        .finishParameterDescription()
                                        .endMethodDescription()
                                        .addByteBuddyImplementation(NodeSetter.class.getDeclaredMethod("nodeType"))
                                        .typeConstant(methodDescription.getParameters().getOnly().getType().asErasure())  // TODO: describe this chain
                                        .endMethodDescription()
                                        .build();
                            }
                        })
                .endElementSource()
                .endMethodDescription()

                .addMethodMap(GameObjectAdapter.class.getDeclaredMethod("nodeProperties"), PropertyAdapter.class)
                .annotationMapElementSource()
                .setMarkerAnnotation(Property.class)
                .setFieldKeyGetter(e -> e.getDeclaredAnnotations().ofType(Property.class).load().name())
                .setMethodKeyGetter(e -> e.getDeclaredAnnotations().ofType(Property.class).load().name())
                .setElementFactory(new ElementFactory<>() {
                    @Override
                    @SneakyThrows(ReflectiveOperationException.class)
                    public ClassFactory<? extends PropertyAdapter> getFieldElement(TypeDescription origin, FieldDescription fieldDescription) throws ClassGeneratorException {
                        return new ClassFactory
                                .Builder<>(PropertyAdapter.class)
                                .addByteBuddyImplementation(PropertyAdapter.class.getMethod("getPropertyType"))
                                .typeConstant(fieldDescription.getType().asErasure())
                                .endMethodDescription()
                                .addSetter(PropertyAdapter.class.getMethod("set", Object.class))
                                .parameterSource(Object.class, 0)
                                .applyToAny()
                                .addSuper(Object.class, Function.identity())
                                .finishParameterDescription()
                                .setMemberSelector(new SpecifiedFieldSelector(fieldDescription))
                                .endMethodDescription()
                                .build();
                    }

                    @Override
                    @SneakyThrows(ReflectiveOperationException.class)
                    public ClassFactory<? extends PropertyAdapter> getMethodElement(TypeDescription origin, MethodDescription methodDescription) throws ClassGeneratorException {
                        return new ClassFactory
                                .Builder<>(PropertyAdapter.class)
                                .addByteBuddyImplementation(PropertyAdapter.class.getMethod("getPropertyType"))
                                .typeConstant(methodDescription.getParameters().getOnly().getType().asErasure())
                                .endMethodDescription()
                                .addMethodSingleton(PropertyAdapter.class.getMethod("set", Object.class), void.class)
                                .parameterSource(Object.class, 0)
                                .applyToAny()
                                .addSuper(Object.class, Function.identity())
                                .finishParameterDescription()
                                .setMemberSelector(new SpecifiedMethodSelector(methodDescription))
                                .endMethodDescription()
                                .build();
                    }
                })
                .endElementSource()
                .endMethodDescription()

                .addMethodSingleton(GameObjectAdapter.class.getDeclaredMethod("identity"), String.class)
                .setMarkerAnnotation(Identity.class)
                .endMethodDescription()

                .build();
    }
}
