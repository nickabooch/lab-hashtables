import java.io.PrintWriter;

/**
 * A simple experiment with probed hash tables.
 */
public class ChainedHashTableExperiments {

  // +------+------------------------------------------------------------
  // | Main |
  // +------+

  /**
   * Do whatever experiments seem reasonable.
   */
  public static void main(String[] args) {
    // Create our normal output mechanism.
    final PrintWriter pen = new PrintWriter(System.out, true);
    // Convert the PrintWriter to a Reporter.
    Reporter rept = new Reporter() {
      public void report(String str) {
        pen.println("*** " + str);
      } // report(String)
    }; // new Reporter()

    // Create a new hash table
    ChainedHashTable<String, String> htab =
        new ChainedHashTable<String, String>(rept);

    // Most of the time, we don't care about the basic calls
    htab.reportBasicCalls(false);

  // Conduct some of the experiments
   HashTableExperiments.matchingKeysExpt(pen, htab);
   HashTableExperiments.repeatedSetExpt(pen, htab);
   HashTableExperiments.matchingSetExpt(pen, htab);
   HashTableExperiments.multipleSetExpt(pen, htab);
   HashTableExperiments.removeExpt(pen, htab);
   htab.reportBasicCalls(true);
   htab.set("alpha", "alpha");
   htab.dump(pen);
   htab.set("beta", "beta");
   htab.dump(pen);
   htab.set("bravo", "bravo");
   htab.dump(pen);
   htab.set("beta", "max");
   htab.dump(pen);
   htab.reportBasicCalls(false);
   pen.println();
  } // main(String[])

} // class ChainedHashTableExperiments
