import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ToasterContext {
  private final Map<String, Consumer<Map<String, Object>>> tasks = new HashMap<>();

  public void registerTask(String name, Consumer<Map<String, Object>> handler) {
    tasks.put(name, handler);
  }

  public Optional<Consumer<Map<String, Object>>> getTaskHandler(String name) {
    return Optional.ofNullable(tasks.get(name));
  }
}
