import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.domain.OriginImpl;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MapperTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();
        assertEquals("2", ((Origin)origin).getWrapper().processFirst(10));
        assertEquals("100", ((Origin)origin).getWrapper().processSecond(10));
    }
}
