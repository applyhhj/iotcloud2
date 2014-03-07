package cgl.iotcloud.core;

import org.ho.yaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Utils {
    public static Map findAndReadConfigFile(String name, boolean mustExist) {
        try {
            HashSet<URL> resources = new HashSet<URL>(findResources(name));
            if (resources.isEmpty()) {
                if (mustExist) throw new RuntimeException("Could not find config file on classpath " + name);
                else return new HashMap();
            }
            if (resources.size() > 1) {
                throw new RuntimeException("Found multiple " + name + " resources."
                        + resources);
            }
            URL resource = resources.iterator().next();
            Map ret = (Map) Yaml.load(new InputStreamReader(resource.openStream()));

            if (ret == null) ret = new HashMap();

            return new HashMap(ret);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<URL> findResources(String name) {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(name);
            List<URL> ret = new ArrayList<URL>();
            while (resources.hasMoreElements()) {
                ret.add(resources.nextElement());
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map findAndReadConfigFile(String name) {
        return findAndReadConfigFile(name, true);
    }

    public static Map readDefaultConfig() {
        return findAndReadConfigFile("defaults.yaml", true);
    }

    public static Map readConfig() {
        Map ret = readDefaultConfig();
        String confFile = System.getProperty("iot.conf.file");
        Map storm;
        if (confFile==null || confFile.equals("")) {
            storm = findAndReadConfigFile("iot.yaml", false);
        } else {
            storm = findAndReadConfigFile(confFile);
        }
        ret.putAll(storm);
        return ret;
    }

    public <T> T load(URL jar, String className) {
        ClassLoader loader = URLClassLoader.newInstance(
                new URL[]{jar}, getClass().getClassLoader()
        );
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, loader);
            Class<? extends Runnable> runClass = clazz.asSubclass(Runnable.class);
            // Avoid Class.newInstance, for it is evil.
            Constructor<? extends Runnable> ctor = runClass.getConstructor();
            Runnable doRun = ctor.newInstance();
            doRun.run();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
