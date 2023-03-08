package con.github.antkudruk.example.gameexample;

import com.github.antkudruk.uniformfactory.exception.ClassGeneratorException;
import con.github.antkudruk.example.gameexample.domain.Human;
import con.github.antkudruk.example.gameexample.gameengine.Color;
import con.github.antkudruk.example.gameexample.gameobject.GameObjectClassFactory;
import con.github.antkudruk.example.gameexample.gameengine.Bone;
import org.junit.Test;

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
    }
}
