import com.github.antkudruk.uniformfactory.classfactory.ClassFactory;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.ClassFactoryGeneratorImpl;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.Origin;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.Wrapper;
import com.github.antkudruk.uniformfactory.test.gradleplugin.typemapper.domain.OriginImpl;
import org.junit.Test;

import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;

public class MapperTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() throws NoSuchMethodException {
        // given
        OriginImpl origin = new OriginImpl();
        ClassFactoryGeneratorImpl factoryGenerator = new ClassFactoryGeneratorImpl();

        // when
        Function<OriginImpl, ? extends Wrapper> metaClass = factoryGenerator
                .generateMetaClass(OriginImpl.class);
        Wrapper wrapper = metaClass.apply(origin);

        // then
        assertEquals("2", wrapper.processFirst(10));
        assertEquals("100", wrapper.processSecond(10));
    }
}
