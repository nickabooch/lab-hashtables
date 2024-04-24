import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * A simple implementation of hash tables.
 *
 * @author Samuel A. Rebelsky
 * @author Your Name Here
 */
public class ProbedHashTable<K,V> implements HashTable<K,V> {

  // +-------+-----------------------------------------------------------
  // | Notes |
  // +-------+

  /*
   * Our hash table is stored as an array of key/value pairs. Because of the
   * design of Java arrays, we declare that as type Object[] and cast whenever
   * we remove an element. (SamR needs to find a better way to deal with this
   * issue; using ArrayLists doesn't seem like the best idea.)
   * 
   * We use linear probing to handle collisions. (Well, we *will* use linear
   * probing, once the table is finished.)
   * 
   * We expand the hash table when the load factor is greater than LOAD_FACTOR
   * (see constants below).
   * 
   * Since some combinations of data and hash function may lead to a situation
   * in which we get a surprising relationship between values (e.g., all the
   * hash values are 0 mod 32), when expanding the hash table, we incorporate a
   * random number. (Is this likely to make a big difference? Who knows. But
   * it's likely to be fun.)
   * 
   * For experimentation and such, we allow the client to supply a Reporter that
   * is used to report behind-the-scenes work, such as calls to expand the
   * table.
   * 
   * Bugs to squash.
   * 
   * [ ] Doesn't check for repeated keys in set.
   * 
   * [ ] Doesn't check for matching key in get.
   * 
   * [ ] Doesn't handle collisions.
   * 
   * [ ] The `expand` method is not completely implemented.
   * 
   * [ ] The `remove` method is not implemented.
   * 
   * Features to add.
   * 
   * [ ] A full implementation of `containsKey`.
   * 
   * [ ] An iterator.
   */

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;

  /**
   * The offset to use in linear probes. (We choose a prime because that helps
   * ensure that we cover all of the spaces.)
   */
  static final double PROBE_OFFSET = 17;

  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of values currently stored in the hash table. We use this to
   * determine when to expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the key/value pairs. (We use an array,
   * rather than an ArrayList, because we want to control expansion.)
   */
  Object[] pairs;

  /**
   * An optional reporter to let us observe what the hash table is doing.
   */
  Reporter reporter;

  /**
   * Do we report basic calls?
   */
  boolean REPORT_BASIC_CALLS = false;

  /**
   * Our helpful random number generator, used primarily when expanding the size
   * of the table..
   */
  Random rand;

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new hash table.
   */
  public ProbedHashTable() {
    this.rand = new Random();
    this.clear();
    this.reporter = null;
  } // ProbedHashTable

  /**
   * Create a new hash table that reports activities using a reporter.
   */
  public ProbedHashTable(Reporter reporter) {
    this();
    this.reporter = reporter;
  } // ProbedHashTable(Reporter)

  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  /**
   * Determine if the hash table contains a particular key.
   */
  @Override
  public boolean containsKey(K key) {
    // STUB/HACK
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    } // try/catch
  } // containsKey(K)

  /**
   * Apply a function to each key/value pair.
   */
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (Pair<K,V> pair : this) {
      action.accept(pair.key(), pair.value());
    } // for
  } // forEach(BiConsumer)

  /**
   * Get the value for a particular key.
   */
  // @Override
  // public V get(K key) {
  //   int index = find(key);
  //   @SuppressWarnings("unchecked")
  //   Pair<K,V> pair = (Pair<K,V>) pairs[index];
  //   if (pair == null) {
  //     if (REPORT_BASIC_CALLS && (reporter != null)) {
  //       reporter.report("get(" + key + ") failed");
  //     } // if reporter != null
  //     throw new IndexOutOfBoundsException("Invalid key: " + key);
  //   } else {
  //     if (REPORT_BASIC_CALLS && (reporter != null)) {
  //       reporter.report("get(" + key + ") => " + pair.value());
  //     } // if reporter != null
  //     return pair.value();
  //   } // get
  // } // get(K)


  @Override
  public V get(K key) {
      int index = getPairIndex(key);
      if (index != -1) {
          @SuppressWarnings("unchecked")
          Pair<K, V> pair = (Pair<K, V>) pairs[index];
          return pair.value();
      } else {
          throw new IndexOutOfBoundsException("Invalid key: " + key);
      }
  }


  /**
   * Iterate the keys in some order.
   */
  public Iterator<K> keys() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.key());
  } // keys()

  // /**
  //  * Remove a key/value pair.
  //  */
  // @Override
  // public V remove(K key) {
  //   // STUB
  //   return null;
  // } // remov
  
  @Override
  public V remove(K key) {
      int index = getPairIndex(key);
      if (index != -1) {
          @SuppressWarnings("unchecked")
          Pair<K, V> pair = (Pair<K, V>) pairs[index];
          V value = pair.value();
          pairs[index] = null;
          size--;
          return value;
      } else {
          return null;
      }
  }



  /**
   * Set a value.
   */
  // @SuppressWarnings("unchecked")
  // public V set(K key, V value) {
  //   V result = null;
  //   // If there are too many entries, expand the table.
  //   if (this.size > (this.pairs.length * LOAD_FACTOR)) {
  //     expand();
  //   } // if there are too many entries
  //   // Find out where the key belongs and put the pair there.
  //   int index = find(key);
  //   if (this.pairs[index] != null) {
  //     result = ((Pair<K,V>) this.pairs[index]).value();
  //   } // if
  //   this.pairs[index] = new Pair<K,V>(key, value);
  //   // Report activity, if appropriate
  //   if (REPORT_BASIC_CALLS && (reporter != null)) {
  //     reporter.report("pairs[" + index + "] = " + key + ":" + value);
  //   } // if reporter != null
  //   // Note that we've incremented the size.
  //   ++this.size;
  //   // And we're done
  //   return result;
  // } // set(K,V)


   @SuppressWarnings("unchecked")
    @Override
    public V set(K key, V value) {
        if (containsKey(key)) {
            int index = getPairIndex(key);
            Pair<K, V> pair = (Pair<K, V>) pairs[index];
            V oldValue = pair.value();
            pair.setValue(value);
            return oldValue;
        } else {
            if (this.size > (this.pairs.length * LOAD_FACTOR)) {
                expand();
            }
            int index = findEmptyIndex(key);
            pairs[index] = new Pair<K, V>(key, value);
            size++;
            return null;
        }
    }

  /**
   * Get the size of the dictionary - the number of values stored.
   */
  @Override
  public int size() {
    return this.size;
  } // size()

  /**
   * Iterate the values in some order.
   */
  public Iterator<V> values() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.value());
  } // values()

  // +------------------+--------------------------------------------
  // | Iterator methods |
  // +------------------+

  /**
   * Iterate the key/value pairs in some order.
   */
  public Iterator<Pair<K,V>> iterator() {
    return new Iterator<Pair<K,V>>() {
      public boolean hasNext() {
        // STUB
        return false;
      } // hasNext()

      public Pair<K,V> next() {
        // STUB
        return null;
      } // next()
    }; // new Iterator
  } // iterator()

  // +-------------------+-------------------------------------------
  // | HashTable methods |
  // +-------------------+

  /**
   * Clear the whole table.
   */
  @Override
  public void clear() {
    this.pairs = new Object[41];
    this.size = 0;
  } // clear()

  /**
   * Dump the hash table.
   */
  @Override
  public void dump(PrintWriter pen) {
    pen.print("{");
    int printed = 0; // Number of elements printed
    for (int i = 0; i < this.pairs.length; i++) {
      @SuppressWarnings("unchecked")
      Pair<K,V> pair = (Pair<K,V>) this.pairs[i];
      if (pair != null) {
        pen.print(i + ":" + pair.key() + "(" + pair.key().hashCode() + "):"
            + pair.value());
        if (++printed < this.size) {
          pen.print(", ");
        } // if
      } // if the current element is not null
    } // for
    pen.println("}");
  } // dump(PrintWriter)

  // +------+------------------------------------------------------------
  // | Misc |
  // +------+

  /**
   * Should we report basic calls? Intended mostly for tracing.
   */
  public void reportBasicCalls(boolean report) {
    REPORT_BASIC_CALLS = report;
  } // reportBasicCalls

  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Expand the size of the table.
   */
  // void expand() {
  //   // Figure out the size of the new table.
  //   int newSize = 2 * this.pairs.length + rand.nextInt(10);
  //   if (REPORT_BASIC_CALLS && (reporter != null)) {
  //     reporter.report("Expanding to " + newSize + " elements.");
  //   } // if reporter != null
  //   // Create a new table of that size.
  //   Object[] newPairs = new Object[newSize];
  //   // Move all pairs from the old table to their appropriate
  //   // location in the new table.
  //   // STUB
  //   // And update our pairs
  // } // expand()



  void expand() {
    int newSize = 2 * this.pairs.length + rand.nextInt(10);
    if (REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("Expanding to " + newSize + " elements.");
    }
    Object[] newPairs = new Object[newSize];
    for (int i = 0; i < this.pairs.length; i++) {
        if (pairs[i] != null) {
            Pair<K, V> pair = (Pair<K, V>) pairs[i];
            int newIndex = findEmptyIndex(pair.key(), newPairs);
            newPairs[newIndex] = pair;
        }
    }
    this.pairs = newPairs;
}



int find(K key) {
    return Math.abs(key.hashCode()) % this.pairs.length;
}

int findEmptyIndex(K key) {
    int index = find(key);
    while (pairs[index] != null) {
        index = (index + 1) % pairs.length; // Linear probing
    }
    return index;
}

int findEmptyIndex(K key, Object[] array) {
    int index = find(key) % array.length;
    while (array[index] != null) {
        index = (index + 1) % array.length; // Linear probing
    }
    return index;
}

int getPairIndex(K key) {
    int startIndex = find(key);
    int index = startIndex;
    do {
        if (pairs[index] != null) {
            @SuppressWarnings("unchecked")
            Pair<K, V> pair = (Pair<K, V>) pairs[index];
            if (pair.key().equals(key)) {
                return index;
            }
        }
        index = (index + 1) % pairs.length; // Linear probing
    } while (index != startIndex);
    return -1;
}
}

//   /**
//    * Find the index of the entry with a given key. If there is no such entry,
//    * return the index of an entry we can use to store that key.
//    */
//   int find(K key) {
//     return Math.abs(key.hashCode()) % this.pairs.length;
//   } // find(K)

// } // class ProbedHashTable<K,V>

