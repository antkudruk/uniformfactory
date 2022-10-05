
package com.github.antkudruk.uniformfactory.methodcollection;

import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Describes how to implement elements for collections (List or Map)
 *
 * @param <F> Element interface
 */
public interface ElementFactory<F> {
    ClassFactory<? extends F> getFieldElement(
            TypeDescription origin,
            FieldDescription fieldDescription) throws ClassGeneratorException;
    ClassFactory<? extends F> getMethodElement(
            TypeDescription origin,
            MethodDescription methodDescription) throws ClassGeneratorException ;
}
