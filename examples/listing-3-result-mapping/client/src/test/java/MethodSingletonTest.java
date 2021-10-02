import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain.Origin1;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain.Origin2;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MethodSingletonTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNumberField() {
        Origin1 o1 = new Origin1();
        Origin2 o2 = new Origin2();

        assertEquals("10", ((Origin)o1).getWrapper().getIdentity());
        assertEquals("name", ((Origin)o2).getWrapper().getIdentity());
    }
}

