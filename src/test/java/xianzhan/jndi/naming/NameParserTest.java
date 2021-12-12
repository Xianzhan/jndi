package xianzhan.jndi.naming;

import org.junit.Test;
import xianzhan.jndi.context.PropertyContext;

import javax.naming.Name;
import java.util.Hashtable;

public class NameParserTest {

    @Test
    public void testParse() throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("jndi.syntax.direction", "left_to_right");
//        env.put("jndi.syntax.direction", "right_to_left");
        env.put("jndi.syntax.separator", "/");

        PropertyContext context = new PropertyContext(env);
        PropertyNameParser nameParser = new PropertyNameParser(context);

        Name parse = nameParser.parse("a/b/c");
        System.out.println(parse);

        Name prefix = parse.getPrefix(1);
        System.out.println(prefix);

        Name suffix = parse.getSuffix(1);
        System.out.println(suffix);
    }
}
