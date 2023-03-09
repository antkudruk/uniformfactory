package com.github.antkudruk.example.gameexample;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import com.github.antkudruk.example.gameexample.domain.Human;
import com.github.antkudruk.example.gameexample.gameengine.Color;
import com.github.antkudruk.example.gameexample.gameobject.GameObjectClassFactory;
import com.github.antkudruk.example.gameexample.gameengine.Bone;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class GameObjectMetaFactoryTest {

    GameObjectClassFactory testSubject = new GameObjectClassFactory();

    @Test
    public void givenHuman_whenBuild() throws ClassGeneratorException {
        // given
        var human = new Human();
        var rightArm = mock(Bone.class);
        var leftArm = mock(Bone.class);
        var head = mock(Bone.class);
        var pantsColor = mock(Color.class);

        // when
        var result = testSubject
                .getClassFactory()
                .buildWrapperFactory()
                .get(human);
        result.nodeSetters().get("rightArm").setNode(rightArm);
        result.nodeSetters().get("leftArm").setNode(leftArm);
        result.nodeSetters().get("head").setNode(head);
        result.nodeProperties().get("pantsColor").set(pantsColor);
        result.nodeProperties().get("age").set(33);
        result.nodeProperties().get("salary").set(new BigDecimal("500.00"));

        // then
        assertEquals("Mike", result.identity());
        assertEquals(rightArm, human.getRightArm());
        assertEquals(Bone.class, result.nodeSetters().get("rightArm").nodeType());
        assertEquals(leftArm, human.getLeftArm());
        assertEquals(Bone.class, result.nodeSetters().get("leftArm").nodeType());
        assertEquals(head, human.getHead());
        assertEquals(Bone.class, result.nodeSetters().get("head").nodeType());
        assertEquals(pantsColor, human.getPantsColor());
        assertEquals(Color.class, result.nodeProperties().get("pantsColor").getPropertyType());
        assertEquals(33, human.getAge());
        assertEquals(int.class, result.nodeProperties().get("age").getPropertyType());
        assertEquals(new BigDecimal("500.00"), human.getSalary());
        assertEquals(BigDecimal.class, result.nodeProperties().get("salary").getPropertyType());
    }
}
