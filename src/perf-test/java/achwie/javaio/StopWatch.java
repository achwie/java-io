package achwie.javaio;

/**
 * 
 * @author Achim Wiedemann, Dec 28, 2013
 */
public class StopWatch {
  private long start = -1;
  private long end = -1;
  private long lap = -1;

  public void start() {
    reset();
    start = System.nanoTime();
    lap = start;
  }

  public long elapsed() {
    return System.nanoTime() - start;
  }

  public long lap() {
    final long now = System.nanoTime();
    final long lastLap = now - lap;
    lap = now;
    return lastLap;
  }

  public long stop() {
    if (end == -1)
      end = System.nanoTime();

    return deltaNanos();
  }

  public long deltaNanos() {
    return end - start;
  }

  public double deltaMicros() {
    return (double) deltaNanos() / 1000;
  }

  public double deltaMillis() {
    return (double) deltaNanos() / (1000 * 1000);
  }

  public void reset() {
    start = -1;
    end = -1;
  }
}
