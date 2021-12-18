package xianzhan.jndi.context;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * list 枚举
 *
 * @author xianzhan
 */
public class PropertyNamingEnumeration<T extends NameClassPair> implements NamingEnumeration<T> {

    private final Iterator<Map.Entry<Name, Object>> iterator;

    public PropertyNamingEnumeration(Map<Name, Object> store) {
        this.iterator = store.entrySet().iterator();
    }

    @Override
    public T next() {
        return nextElement();
    }

    @Override
    public boolean hasMore() {
        return iterator.hasNext();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T nextElement() {
        Map.Entry<Name, Object> entry = iterator.next();
        Name name = entry.getKey();
        Object value = entry.getValue();
        return (T) new Binding(name.toString(), value.getClass().getName(), value);
    }
}
