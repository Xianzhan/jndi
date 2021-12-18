package xianzhan.jndi.naming;

import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * 上下文路径解析器
 *
 * @author xianzhan
 */
public class PropertyNameParser implements NameParser {

    private final Properties syntax;

    public PropertyNameParser(Context context) throws NamingException {
        Properties syntax = new Properties();
        syntax.putAll(context.getEnvironment());

        this.syntax = syntax;
    }

    @Override
    public Name parse(String name) throws NamingException {
        if (name == null) {
            name = "";
        }
        return new CompoundName(name, syntax);
    }
}
