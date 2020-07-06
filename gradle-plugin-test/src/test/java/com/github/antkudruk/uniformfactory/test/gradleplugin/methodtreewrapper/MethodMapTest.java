package com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper;

import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.origins.OriginImpl;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.wrapper.Fun;
import com.github.antkudruk.uniformfactory.test.gradleplugin.methodtreewrapper.wrapper.Origin;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class MethodMapTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        OriginImpl origin = new OriginImpl();

        Map<String, Fun> methodMap = ((Origin)origin).getWrapper().getWrappers();

        assertEquals("Alpha Value", methodMap.get("alpha").common("Foo", "bar"));
        assertEquals("Beta Value", methodMap.get("beta").common("Foo", "bar"));
        assertEquals("Foo 10 units", methodMap.get("gamma").common("Foo", "10"));
        assertEquals("Foo No", methodMap.get("delta").common("Foo", "false"));
        assertEquals("Epsilon Value", methodMap.get("epsilon").common("Foo", "false"));
    }
}
