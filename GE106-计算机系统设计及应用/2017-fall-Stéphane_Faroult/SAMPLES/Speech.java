/*
 *   This program illustrates using collections in Java.
 *   Its purpose is to find the 10 most used words in
 *   a speech.
 *   You can use it in two different ways:
 *
 *     java Speech speech_file.txt
 *  or java Speech speech_file.txt stop_words.txt
 *
 *   In the first case, it's very likely that the 10 most
 *   frequent words will be, for an English speech, words
 *   such as "the", "and", "of", "is", ... In the second 
 *   version, stop_words.txt is read first and if a word
 *   from the speech appears in this file, it's simply
 *   ignored.
 *   Lists of stop words can easily be found on the web. 
 *   However, depending on what you want to analyze in a
 *   speech, you may want to taylor the list. "is" or "will"
 *   may not be significant word generally speaking, they may
 *   become significant if you analyze whether the speech is
 *   more about the present of the future. In the same way,
 *   "I" may become significant when compared to "We".
 */
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Collections;

public class Speech {
    // The first collection stores the words in the
    // speech, and how many times they appear.
    // We have a key (the word) and a value (the count)
    // so a Map is required. HashMap or TreeMap? The main
    // difference is that in a TreeMap entries are ordered
    // - but by key. We don't mind about words being sorted.
    // We would like to sort by count! But for this, we'll
    // need another collection. So a HashMap does the job
    // perfectly.
    static HashMap<String,Integer> words
              = new HashMap<String,Integer>();
    // The second collection stores the words, but by number
    // of times they appear. Once again, two objects, but this
    // time the key is the count. Two objects means Map again.
    // HashMap or TreeMap? TreeMap this time, because the
    // ordering by count matters. There is another problem here,
    // which is that the value cannot be a simple String - several
    // words will probably appear the same number of times. But in
    // a Map, a key appears only once, so the "value" Object needs to
    // be a Collection object. What type of Collection? This time
    // we have a single value (the word), so we don't need a map.
    // We could use almost any type of collection. As I feel that
    // it would be nicer to have words that appear the same number
    // of times being displayed in alphabetical order, and as I know 
    // that each word appears only once, I have chosen a TreeSet.
    //
    // Beware of Syntax, we have a TreeMap<K, V>
    // where V is a TreeSet<T> - hence a lot of angle brackets.
    static TreeMap<Integer,TreeSet<String>> wordCount
              = new TreeMap<Integer,TreeSet<String>>();
    
    public static void main(String[] args) {
       //
       String           word;
       // I'm calling "wordList" a TreeSet, because for me
       // it's a simple list of words, even if it's not,
       // technically, a Java List. It's a list of words
       // that appear an identical number of times.
       TreeSet<String>  wordList;
       // Stop words are also supposed to appear only
       // once, and must be searched fast. I have chosen
       // a TreeSet to store them.
       TreeSet<String>  stopWords = null;

       if (args.length > 0) {
          try {
            Tokenizer tok = new Tokenizer(args[0]);
            Tokenizer stop = null;
            int       cnt;

            // If the name of a stop-words file is provided,
            // I read it before processing the speech (already
            // loaded in memory)
            if (args.length > 1) {
              stop = new Tokenizer(args[1]);
              stopWords = new TreeSet<String>();
              while ((word = stop.nextToken()) != null) {
                stopWords.add(word);
              }
            }
            // Now I count the words in the speech
            while ((word = tok.nextToken()) != null) {
              // I only consider words when there is no
              // list of stop words, or when they aren't 
              // in this list.
              if ((stopWords == null) || ! stopWords.contains(word)) {
                // Have I already encountered this word?
                if (words.containsKey(word)) {
                  cnt = words.get(word);
                } else {
                  cnt = 0;
                }
                // Store the new count
                words.put(word, cnt + 1);
              }
            }
            // OK, words are counted.
            //
            // Reverse counters and values
            // First get a set of keys, then iterate and store into
            // wordCount
            // I have different ways of doing it.
            // One is the one that was shown in class:
            // I use the entrySet() method of the HashMap class
            // that returns the map entries (key, value pairs) as
            // elements of a set on which we can get an iterator()
            // - there are no iterator for maps (remember that they
            // aren't real collections, just a distant relative) 
            //
            // Set<Map.Entry<String,Integer>> mapEntries
            //            = words.entrySet();
            //
            // I had it wrong doing it live, I must specify
            // the type of what my iterator returns:
            //
            // Iterator<Map.Entry<String,Integer>> iter
            //        = mapEntries.iterator();
            //
            // int                        cnt;
            // Map.Entry<String,Integer>  me;
            // // Now I can iterate and store into
            // // the wordCount TreeMap:
            // while (iter.hasNext()) {
            //   me = iter.next();
            //   cnt = me.getValue();
            //   // Do I have already words that I find
            //   // cnt times?
            //   if (wordCount.containsKey(cnt)) {
            //     wordList = wordCount.get(cnt);
            //   } else {
            //     wordList = new TreeSet<String>();
            //   } 
            //   wordList.add(me.getKey());
            //            // Add the word to the TreeSet
            //   wordCount.put(me.getValue(), wordList);
            //            // Put back the TreeSet into the TreeMap
            // }
            //
            // The other way of doing it is to just consider
            // the keys (the words) in the words TreeMap,
            // and get for each one the count directly from
            // the TreeMap rather than from a returned Map.Entry.
            // It should be a few nanoseconds slower, but who
            // cares ... You may find it easier to understand.
            //
            Set<String> wordSet = words.keySet();
            Iterator<String> iter = wordSet.iterator();
            while (iter.hasNext()) {
              word = iter.next();
              cnt = words.get(word);
              if (wordCount.containsKey(cnt)) {
                wordList = wordCount.get(cnt);
              } else {
                wordList = new TreeSet<String>();
              }
              wordList.add(word);
              wordCount.put(cnt, wordList);
            }
            // Now display the 10 most common words
            // lastKey() is a special TreeMap method
            // that returns the highest key value.
            // As my key is how many time a word is used,
            // it will first return the highest number
            // of occurrences of one or several words
            Integer n = wordCount.lastKey();
            int frequencies = 1;
            while ((n != null) && (frequencies <= 10)) {
              // Displaying a number of occurrence
              // followed by the words that appear that
              // number of times on the same line.
              System.out.print(n + "\t");
              wordList = wordCount.get(n);
              iter = wordList.iterator();
              while (iter.hasNext()) {
                System.out.print(iter.next() + "\t");
              }
              System.out.println("");
              frequencies++;
              // lowerKey() is getting the key that precedes
              // n. It's a kind of iterator that moves backwards.
              n = wordCount.lowerKey(n);
            }
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
       }
    }
}
