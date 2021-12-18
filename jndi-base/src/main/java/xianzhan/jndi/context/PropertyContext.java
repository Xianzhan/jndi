package xianzhan.jndi.context;

import xianzhan.jndi.JndiConst;
import xianzhan.jndi.naming.PropertyNameParser;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 上下文
 *
 * @author xianzhan
 */
public class PropertyContext implements Context {

    private final Hashtable<? super Object, ? super Object> env;

    private final String             namespace;
    private final NameParser         nameParser;
    private final Map<Name, Context> subcontextMap;
    private final Map<Name, Object>  objectMap;

    public PropertyContext(Hashtable<?, ?> env) throws NamingException {
        this.env = new Hashtable<>(env);
        this.namespace = (String) this.env.getOrDefault(JndiConst.KEY_NAMESPACE, "");

        this.nameParser = new PropertyNameParser(this);
        this.subcontextMap = new ConcurrentHashMap<>();
        this.objectMap = new ConcurrentHashMap<>();
    }

    public PropertyContext load() throws NamingException {
        final boolean filenameToContext = Boolean.parseBoolean((String) env.get(JndiConst.KEY_FILE_NAME_TO_SUBCONTEXT));
        String path = (String) env.get(JndiConst.KEY_FILE_PROPERTY_PATH);
        if (path == null) {
            path = "";
        }

        // 默认处理 classpath 路径下的 properties 文件
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            Objects.requireNonNull(resource);

            String filename = resource.getFile();
            String windowsFormat = ":";
            if (filename.contains(windowsFormat)) {
                // windows 路径包含 :
                filename = filename.substring(1);
            }
            Path file = Paths.get(filename);
            if (Files.isDirectory(file)) {
                List<Path> collect = Files.list(file)
                        .filter(p -> p.getFileName().toString().endsWith(".properties"))
                        .collect(Collectors.toList());
                for (Path p : collect) {
                    try (InputStream is = Files.newInputStream(p)) {
                        Properties properties = new Properties();
                        properties.load(is);

                        String fn = p.getFileName().toString();
                        String f = fn.substring(0, fn.lastIndexOf('.'));
                        if (filenameToContext) {
                            Context subcontext = createSubcontext(f);
                            putPropertiesToContext(subcontext, properties);
                        } else {
                            putPropertiesToContext(this, properties);
                        }
                    }
                }
            } else {
                try (InputStream is = Files.newInputStream(file)) {
                    Properties properties = new Properties();
                    properties.load(is);
                    putPropertiesToContext(this, properties);
                }
            }
        } catch (Exception e) {
            throw new NamingException(e.toString());
        }
        return this;
    }

    private void putPropertiesToContext(Context context, Properties properties) throws NamingException {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            context.bind(key, value);
        }
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        if (name.size() == 0) {
            return this;
        }

        if (name.size() == 1) {
            if (objectMap.containsKey(name)) {
                // 对象
                Object o = objectMap.get(name);
                if (o instanceof Reference) {
                    try {
                        Object objectInstance = NamingManager.getObjectInstance(o, null, null, getEnvironment());
                        o = objectInstance == o ? null : objectInstance;
                        objectMap.put(name, o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return o;
            }

            // 子上下文
            Context subcontext = subcontextMap.get(name);
            if (subcontext == null) {
                throw new NameNotFoundException(name.toString());
            }
            return subcontext;
        }

        // 由子上下文查找
        Name prefix = name.getPrefix(1);
        Context subcontext = subcontextMap.get(prefix);
        if (subcontext == null) {
            throw new NameNotFoundException(name.toString());
        }
        Name suffix = name.getSuffix(1);
        return subcontext.lookup(suffix);
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        checkName(name);

        if (name.size() == 1) {
            if (obj instanceof Context) {
                if (subcontextMap.containsKey(name)) {
                    throw new NameAlreadyBoundException(name.toString());
                }
                subcontextMap.put(name, (Context) obj);
            } else {
                if (objectMap.containsKey(name)) {
                    throw new NameAlreadyBoundException(name.toString());
                }
                objectMap.put(name, obj);
            }
            return;
        }

        Name subName = name.getSuffix(1);
        Context subcontext = subcontextMap.get(subName);
        if (subcontext == null) {
            throw new NameNotFoundException(name.toString());
        }
        subcontext.bind(subName, obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(nameParser.parse(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        checkName(name);
        unbind(name);
        bind(name, obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(nameParser.parse(name), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        checkName(name);

        if (name.size() == 1) {
            subcontextMap.remove(name);
            objectMap.remove(name);
        } else {
            // 获取前一个上下文
            Object targetContext = lookup(name.getPrefix(name.size() - 1));
            if (!(targetContext instanceof Context)) {
                throw new NamingException(name.toString());
            }
            Context context = (Context) targetContext;
            context.unbind(name.getSuffix(name.size() - 1));
        }
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(nameParser.parse(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        Object oldObj = lookup(oldName);
        if (oldObj == null) {
            throw new NamingException(oldName.toString());
        }

        Object newObj = lookup(newName);
        if (newObj != null) {
            throw new NamingException(newName.toString());
        }

        unbind(oldName);
        bind(newName, oldObj);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(nameParser.parse(oldName), nameParser.parse(newName));
    }

    public <T extends NameClassPair> NamingEnumeration<T> listT(Name name, Function<Context, NamingEnumeration<T>> contextList) throws NamingException {
        if (name == null || name.isEmpty()) {
            Map<Name, Object> all = new HashMap<>();
            all.putAll(subcontextMap);
            all.putAll(objectMap);
            return new PropertyNamingEnumeration<>(all);
        }

        Name prefix = name.getPrefix(1);
        if (!subcontextMap.containsKey(prefix)) {
            throw new NamingException(name.toString());
        }
        Context preContext = subcontextMap.get(prefix);
        return contextList.apply(preContext);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return listT(name, ctx -> {
            try {
                return ctx.list(name.getSuffix(1));
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listT(name, ctx -> {
            try {
                return ctx.listBindings(name);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(nameParser.parse(name));
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        checkName(name);

        if (name.size() == 1) {
            Context remove = subcontextMap.remove(name);
            remove.close();
            return;
        }

        Name subName = name.getSuffix(1);
        Context subcontext = subcontextMap.get(subName);
        if (subcontext == null) {
            throw new NameNotFoundException(name.toString());
        }
        subcontext.destroySubcontext(name);
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(nameParser.parse(name));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        checkName(name);

        if (name.size() == 1) {
            if (subcontextMap.containsKey(name) || objectMap.containsKey(name)) {
                throw new NameAlreadyBoundException(name.toString());
            }
            Hashtable<? super Object, ? super Object> environment = getEnvironment();
            environment.put(JndiConst.KEY_NAMESPACE, name.toString());
            Context subcontext = new PropertyContext(environment);
            bind(name, subcontext);
            return subcontext;
        }

        Name subName = name.getPrefix(1);
        Context subcontext = subcontextMap.get(subName);
        if (subcontext != null) {
            Name subSubName = name.getSuffix(1);
            return subcontext.createSubcontext(subSubName);
        }

        // 当前子上下文不存在则创建
        subcontext = new PropertyContext(getEnvironment());
        bind(name, subcontext);
        Name subSubName = name.getSuffix(1);
        return subcontext.createSubcontext(subSubName);
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(nameParser.parse(name));
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        if (name == null || name.isEmpty()) {
            return nameParser;
        }

        Name subName = name.getPrefix(1);
        Context subcontext = subcontextMap.get(subName);
        if (subcontext == null) {
            throw new NotContextException(name.toString());
        }
        return subcontext.getNameParser(subName);
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser(nameParser.parse(name));
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(prefix);

        Name clone = (Name) prefix.clone();
        return clone.addAll(name);
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return composeName(nameParser.parse(name), nameParser.parse(prefix)).toString();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) {
        return env.put(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) {
        return env.remove(propName);
    }

    @Override
    public Hashtable<? super Object, ? super Object> getEnvironment() {
        return new Hashtable<>(env);
    }

    @Override
    public void close() {

    }

    @Override
    public String getNameInNamespace() {
        return namespace;
    }

    private void checkName(Name name) throws InvalidNameException {
        if (name.isEmpty()) {
            throw new InvalidNameException("不能绑定空名");
        }
    }
}
