import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

public class JavaPluginLoader {
  public static void loadPlugins(ToasterContext context, File pluginDir) throws Exception {
    File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
    if (jars == null) return;

    URL[] urls = new URL[jars.length];
    for (int i = 0; i < jars.length; i++) {
      urls[i] = jars[i].toURI().toURL();
    }

    try (URLClassLoader loader = new URLClassLoader(urls, JavaPluginLoader.class.getClassLoader())) {
      ServiceLoader<ToasterPlugin> plugins = ServiceLoader.load(ToasterPlugin.class, loader);
      for (ToasterPlugin : plugins) {
        plugin.register(context);
      }
    }
  }
}
