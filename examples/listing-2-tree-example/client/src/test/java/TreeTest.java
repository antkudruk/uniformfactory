import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.HasTreeElement;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodsingleton.tree.domain.Company;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TreeTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        Company company = new Company();
        HasTreeElement tree = (HasTreeElement) company;
        assertEquals("Managers", tree.getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
        assertEquals("Beavis", tree.getTreeElement()
                .nested().get(0).getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
        assertEquals("Butthead", tree.getTreeElement()
                .nested().get(0).getTreeElement()
                .nested().get(1).getTreeElement().getLabel());
        assertEquals("Labours", tree.getTreeElement()
                .nested().get(1).getTreeElement().getLabel());
        assertEquals("Stewart", tree.getTreeElement()
                .nested().get(1).getTreeElement()
                .nested().get(0).getTreeElement().getLabel());
    }
}
