package xianzhan.jndi.context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xianzhan.jndi.JndiConst;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

public class PropertyContextTest {

    private Context propertyContext;

    @Before
    public void before() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(JndiConst.KEY_FILE_PROPERTY_PATH, "");
//        env.put(JndiConst.KEY_FILE_NAME_TO_SUBCONTEXT, "false");
        env.put(JndiConst.KEY_FILE_NAME_TO_SUBCONTEXT, "true");
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.direction", "right_to_left");
        env.put("jndi.syntax.separator", "/");
        propertyContext = new PropertyContextFactory().getInitialContext(env);
    }

    @Test
    public void testLookup() throws NamingException {
        Object name = propertyContext.lookup("xianzhan/name");
        Assert.assertEquals("xianzhan", name);

        Object javaName = propertyContext.lookup("java/name");
        Assert.assertEquals("java", javaName);
        Object javaVersion = propertyContext.lookup("java/version");
        Assert.assertEquals("17", javaVersion);
    }

    @Test
    public void testBindAndLookup() throws NamingException {
        propertyContext.bind("1", 1);
        Object lookup = propertyContext.lookup("1");
        Assert.assertEquals(1, lookup);

        propertyContext.bind("12", 2);
        Object lookup1 = propertyContext.lookup("12");
        Assert.assertEquals(2, lookup1);
    }

    @Test
    public void testList() throws NamingException {
        propertyContext.bind("1", 1);
        Object lookup = propertyContext.lookup("1");
        Assert.assertEquals(1, lookup);

        propertyContext.bind("12", 2);
        Object lookup1 = propertyContext.lookup("12");
        Assert.assertEquals(2, lookup1);

        NamingEnumeration<NameClassPair> list = propertyContext.list("");
        while (list.hasMore()) {
            System.out.println(list.next());
        }
    }

    @Test
    public void testAddToEnvironment() throws NamingException {
        propertyContext.addToEnvironment("hi", "hello");
        Object hi = propertyContext.getEnvironment().get("hi");
        Assert.assertEquals("hello", hi);
    }
}
