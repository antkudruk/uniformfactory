package com.github.antkudruk.uniformfactory.container;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;

/**
 *
 * @param <W> Adapter class
 */
public interface WrapperFactory<W> {
    <E> W get(E object) throws ClassGeneratorException;
}
