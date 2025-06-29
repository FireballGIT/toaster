import java.util.HashMap;

class Butter {
    private HashMap<String, String> cache;

    public Butter() {
        cache = new HashMap<>();
    }

    public boolean onBeforeTask(String taskName, Object[] args) {
        System.out.println("[Butter] Smoothing out '" + taskName + "'... Checking cache.");
        if (cache.containsKey(taskName)) {
            System.out.println("[Butter] Found cached result for '" + taskName + "'. Skipping execution.");
            return true;  // Signal to skip task execution
        }
        return false;
    }

    public void onAfterTask(String taskName, Object[] args, String result) {
        System.out.println("[Butter] Caching result of '" + taskName + "'.");
        cache.put(taskName, result);
        System.out.println("[Butter] Task done, buttery smooth.");
    }
}

// Example usage inside ToasterInterpreter:

public class ToasterInterpreter {
    private static Butter butter = new Butter();

    public static String executeTask(String taskName, Object[] args) throws InterruptedException {
        boolean skip = butter.onBeforeTask(taskName, args);
        if (skip) {
            System.out.println("[Toaster] Skipped '" + taskName + "' thanks to Butter cache!");
            return butter.cache.get(taskName);
        }
        // Simulate task running
        System.out.println("[Toaster] Running task '" + taskName + "'...");
        Thread.sleep(1000);  // fake workload
        String result = "Result of " + taskName;
        butter.onAfterTask(taskName, args, result);
        return result;
    }
