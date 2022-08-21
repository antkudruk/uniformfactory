import com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap.Point;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodmap.domain.PointTypeA;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MethodMapTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        PointTypeA pointTypeA = new PointTypeA();
        Point point = (Point) pointTypeA;

        assertEquals(10L, point.getWrapper().getCoords().get("x").getCoordinate(10L));
        assertEquals(20L, point.getWrapper().getCoords().get("y").getCoordinate(15L));
        assertEquals(1L, point.getWrapper().getCoords().get("z").getCoordinate(20L));

        assertEquals(10L, point.getWrapper().getCoords().get("x").getCoordinate(-10L));
        assertEquals(20L, point.getWrapper().getCoords().get("y").getCoordinate(-15L));
        assertEquals(1L, point.getWrapper().getCoords().get("z").getCoordinate(-20L));

    }
}
