import fj.*;
import fj.data.HashMap;
import fj.data.List;
import fj.data.Set;
import fj.test.Gen;
import fj.test.Property;
import fj.test.runner.PropertyTestRunner;
import org.junit.runner.RunWith;

import java.util.NoSuchElementException;

import static fj.Equal.*;
import static fj.Ord.*;
import static fj.data.List.fromIterator;
import static fj.test.Arbitrary.*;
import static fj.test.Cogen.cogenCharacter;
import static fj.test.Cogen.cogenInteger;
import static fj.test.Gen.*;
import static fj.test.Property.*;

@RunWith(PropertyTestRunner.class)
public class SortedTreeMapTest {

    /**
     * Generates generators for specifically typed sortedTreeMaps.
     * @param ks key generator
     * @param vs value generator
     * @param ord Function that determines order between keys
     * @param <K> The type of keys
     * @param <V> The type of values
     * @return Generator of SortedTreeMap<K, V>
     */
    public static <K extends Comparable<? super K>, V> Gen<SortedTreeMap<K, V>> arbitrarySortedTreeMap(
            final Gen<K> ks, final Gen<V> vs, Ord<K> ord
    ) {
        return listOf(arbP2(ks, vs)).map(kvs -> {
            final SortedTreeMap<K, V> tm = new SortedTreeMap<>(ord.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));
            return tm;
        });
    }


    /**
     * Generator for a random character in the set a-z
     */
    private static final Gen<Character> azCharacter = choose(97, 122).map(i -> (char) i.intValue());

    /**
     * Generator for a random string with the characters a-z
     */
    private static final Gen<String> arbString = arbList(azCharacter).map(List::asString);

    /**
     * Generator for a random SortedTreeMap
     */
    private static final Gen<SortedTreeMap<Integer, String>> treeMap = arbitrarySortedTreeMap(
            arbInteger,  arbString, Ord.intOrd);

    /**
     * Generator for a List of pairs of Integers and Strings, without repeating Integers, in random order
     */
    private static final Gen<List<P2<Integer, String>>> isKVList = listOf(arbP2(arbInteger, arbString))
            .map(list -> Set.iterableSet(p2Ord1(intOrd), list))
            .map(Set::toList)
            .bind(Gen::somePermutationOf);

    /**
     * Generator for an arbitrary list of pairs of Integers and Strings; Integers might repeat,
     * in random order
     */
    private static final Gen<List<P2<Integer, String>>> arbKVList = listOf(arbP2(arbInteger, arbString));

    /**
     * Generator for a list of pairs of Integers and Strings that has at least 1 element. Integers do not repeat.
     */
    private static final Gen<List<P2<Integer, String>>> nonEmptyKVList = listOf1(arbP2(arbInteger, arbString))
            .map(list -> Set.iterableSet(p2Ord1(intOrd), list))
            .map(Set::toList);

    /**
     * Generator for a list of Integers and Characters, Integers do not repeat. Random order.
     */
    private static final Gen<List<P2<Integer, Character>>> icKVList = listOf(arbP2(arbInteger, azCharacter))
            .map(list -> Set.iterableSet(p2Ord1(intOrd), list))
            .map(Set::toList)
            .bind(Gen::somePermutationOf);

    /**
     * Function to test for List<Integer> equality
     */
    private Equal<List<Integer>> intListEqual = listEqual(intEqual);

    /**
     * Fucntion to test for List<String> equality
     */
    private Equal<List<String>> stringListEqual = listEqual(stringEqual);


    /**
     * Tests if the size of the map is 0, then it is empty as well
     */
    public Property size_zero_means_empty() {
        return property(value(new SortedTreeMap<>(intOrd.toComparator())),
                // The test only runs if tm.size() == 0
                tm -> impliesBoolean(tm.size() == 0, tm.isEmpty()));
    }

    /**
     * If the size is bigger than 0, then the map is not empty
     */
    public Property size_ge_zero_means_not_empty() {
        // Generates a value with treeMap and run the function on that tree map, checking the size is > 0
        // and then the map should not be empty.
        return property(treeMap, tm -> impliesBoolean(tm.size() > 0, !tm.isEmpty()));
    }

    /**
     * If new entries are added to the map, then size update as long as the keys are not already in the map.
     */
    public Property add_updates_size() {
        // Generate a treeMap and a key-value list and run the test function with those as parameters.
        return property(treeMap, isKVList, (tm, kvs) -> {
            int size_before = tm.size();

            // If they key is already in the map, don't count it as it doesn't increase the size.
            int already_in = 0;
            for (P2<Integer, String> kv : kvs) {
                Integer key = kv._1();
                String value = kv._2();
                if (tm.add(key, value) != null) {
                    already_in += 1;
                }
            }
            return prop(tm.size() == size_before + kvs.length() - already_in);
        });
    }

    /**
     * Check that add(Key, Value) returns the previous value
     */
    public Property add_returns_previous_value() {
        return property(isKVList, arbString,
                (kvs, value) ->
                        // Check if the new value is the same as the previous in the map
                        implies(kvs.length() > 0 && !kvs.exists(kv -> value.equals(kv._2())), () ->
                                property(choose(0, kvs.length()-1),  i -> {
                                    P2<Integer, String> entry = kvs.index(i);

                                    SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
                                    kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

                                    String old_value = tm.add(entry._1(), value);
                                    return prop(old_value.equals(entry._2()));

                                })
                        )
        );
    }

    /**
     *  Check that add(Entry) returns the previous value
     */
    public Property add_entry_returns_previous_value() {
        return property(isKVList, arbString,
                (kvs, value) -> implies(kvs.length() > 0 && !kvs.exists(kv -> value.equals(kv._2())),
                        // Choose a random index in the key-value list.
                        () -> property(choose(0, kvs.length()-1),  i -> {
                            P2<Integer, String> entry = kvs.index(i);

                            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
                            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

                            String old_value = tm.add(new Entry<>(entry._1(), value));
                            return prop(old_value.equals(entry._2()));
                        })
                )
        );
    }

    /**
     * If we remove an entry, there should be one less on the size.
     */
    public Property remove_means_less_size() {
        return property(treeMap, arbP2(arbInteger, arbString), (tm, kv) -> {
            tm.add(kv._1(), kv._2());
            int size_after = tm.size();
            tm.remove(kv._1());
            return prop(tm.size() == size_after-1);
        });
    }

    /**
     * Checks that remove returns the correct value.
     */
    public Property remove_returns_value() {
        return property(isKVList,
                kvs -> implies(kvs.length() > 0,
                        () -> property(choose(0, kvs.length()-1),
                                i -> {
                                    P2<Integer, String> entry = kvs.index(i);

                                    SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
                                    kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

                                    String old_value = tm.remove(entry._1());
                                    return prop(old_value.equals(entry._2()));
                                })
                )
        );
    }

    /**
     * Check that entry is no longer in map after remove
     */
    public Property remove_removes_entry() {
        return property(isKVList,
                kvs -> implies(kvs.length() > 0,
                        () -> property(choose(0, kvs.length()-1),
                                i -> {
                                    P2<Integer, String> entry = kvs.index(i);

                                    SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
                                    kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

                                    tm.remove(entry._1());
                                    List<Integer> keys = fromIterator(tm.keys().iterator());
                                    return prop(!keys.exists(key -> key.equals(entry._1())));
                                })
                )
        );
    }

    /**
     * When we only have 1 entry, max and min are the same entry.
     */
    public Property max_eq_min_after_add1() {
        return property(arbInteger, arbString, (key, val) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());

            tm.add(key, val);

            Entry<Integer, String> entry = tm.min();
            return prop(entry.key.equals(key) && entry.value.equals(val)
                    && tm.min().equals(tm.max()));
        });
    }

    /**
     * If we add any entries, then the minimum entry is the smallest entry that was added.
     */
    public Property min_maybe_addn() {
        return property(isKVList, kvs -> {
            final SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));
            int size_before = tm.size();

            final Entry<Integer, String> entry = tm.min();

            P2<Integer, String> minEntry = kvs.sort(p2Ord1(intOrd)).headOption().toNull();
            if (entry == null || minEntry == null) {
                return prop(minEntry == null && entry == null);
            }

            return prop(entry.key.equals(minEntry._1())
                    && entry.value.equals(minEntry._2())
                    && tm.size() == size_before);
        });
    }

    /**
     * If we add any entries, then the maximum entry is the largest entry that was added.
     */
    public Property max_maybe_addn() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));
            int size_before = tm.size();

            P2<Integer, String> maxEntry = kvs.sort(p2Ord1(intOrd)).reverse().headOption().toNull();

            Entry<Integer, String> entry = tm.max();

            if (entry == null || maxEntry == null) {
                return prop(maxEntry == null && entry == null);
            }

            return prop(entry.key.equals(maxEntry._1())
                    && entry.value.equals(maxEntry._2())
                    && tm.size() == size_before);
        });
    }

    /**
     * If more than 1 entry has been added, then max and min are not the same entry.
     */
    public Property max_min_are_different_after_addn() {
       return property(isKVList, kvs -> {
           SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
           kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));
           return implies(tm.size() > 1, () -> prop(!tm.min().equals(tm.max())));
       });

    }

    /**
     * If we try to replace a key that is not in the map, the map throws an exception.
     */
    public Property replace_missing_key() {
        return property(isKVList, arbInteger, arbString, (kvs, key, val) -> {
           SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
           kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

           return implies(!kvs.exists(kv -> kv._1().equals(key)), () -> {
               try {
                   tm.replace(key, val);
               } catch (NoSuchElementException e) {
                   return prop(true);
               }
               return prop(false);
           });
        });
    }

    /**
     * If we try to replace a key that is not in the map, the map throws an exception.
     */
    public Property replace_with_f_missing_key() {
        return property(isKVList, arbInteger, (kvs, key) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            return implies(!kvs.exists(kv -> kv._1().equals(key)), () -> {
                try {
                    tm.replace(key, (k, v) -> "");
                } catch (NoSuchElementException e) {
                    return prop(true);
                }
                return prop(false);
            });
        });
    }


    /**
     * If a key is not in the keys we added, check that it is not contained in the
     * map and that all the keys we added actually are in the map.
     */
    public Property contains_key_but_not_other() {
        return property(isKVList, arbInteger, (kvs, key) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            return implies(!kvs.exists(kv -> kv._1().equals(key)), () -> prop(!tm.containsKey(key)))
                    .and(prop(kvs.forall(kv -> tm.containsKey(kv._1()))));
        });
    }

    /**
     * Check that we can get all values for all keys from the entries that we added to the map.
     */
    public Property get_value_for_key() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            return prop(kvs.forall(kv -> tm.getValue(kv._1()).equals(kv._2())));
        });
    }

    /**
     * If we try to replace a key that is not in the map, the map throws an exception.
     */
    public Property get_value_missing_key() {
        return property(isKVList, arbInteger, (kvs, key) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            return implies(!kvs.exists(kv -> kv._1().equals(key)), () -> {
                try {
                    tm.getValue(key);
                } catch (NoSuchElementException e) {
                    return prop(true);
                }
                return prop(false);
            });
        });
    }


    /**
     * Check that we can replace a value for a key that is already in the map.
     */
    public Property replace_value_for_key() {
        return property(nonEmptyKVList, arbString, (kvs, val) -> {
            final SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            final P2<Integer, String> entry = kvs.head();

            tm.replace(entry._1(), val);

            return implies(!entry._2().equals(val), () -> prop(tm.getValue(entry._1()).equals(val)));
        });
    }

    /**
     * Check that we can apply a function to replace the value for a key
     */
    public Property replace_with_function_for_key() {
        // Generates a Integer/Character key-value list and a arbitrary function from
        // (Integer, Character) -> Character
        return property(icKVList, arbF2(cogenInteger, cogenCharacter, azCharacter),
                (kvs, fun) -> {
                    final SortedTreeMap<Integer, Character> tm = new SortedTreeMap<>(intOrd.toComparator());
                    kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

                    // For all key-values, run the function f(key, value),
                    // Run the same function with the map; check that when you get the value,
                    // you get the same value.
                    return prop(kvs.forall(kv -> {
                        Character c = fun.f(kv._1(), kv._2());
                        tm.replace(kv._1(), fun::f);
                        return tm.getValue(kv._1()).equals(c);
                    }));
                });
    }

    /**
     * Check that the map contains all values that we entered into the map, but not any
     * that we haven't.
     */
    public Property contains_value_but_not_other() {
        return property(isKVList, arbString, (kvs, val) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            return implies(!kvs.exists(kv -> kv._2().equals(val)), () -> prop(!tm.containsValue(val)))
                    .and(prop(kvs.forall(kv -> tm.containsValue(kv._2()))));
        });
    }

    /**
     * Check if all keys we entered are in the map and that they are sorted.
     */
    public Property get_keys() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            List<Integer> keys = fromIterator(tm.keys().iterator());
            List<Integer> sorted = kvs.map(P2::_1).sort(intOrd);

            return prop(intListEqual.eq(keys, sorted));
        });
    }

    /**
     * Check that all values are in the map, and that they are sorted by the key.
     */
    public Property get_values() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            List<String> values = fromIterator(tm.values().iterator());
            List<String> sorted = kvs.sort(p2Ord1(intOrd)).map(P2::_2);

            return prop(stringListEqual.eq(values, sorted));
        });
    }

    /**
     * Check that all entries are in the map, and that they are ordered by the key.
     */
    public Property get_entries() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            List<Entry<Integer, String>> entries = fromIterator(tm.entries().iterator());
            List<P2<Integer, String>> sorted = kvs.sort(p2Ord1(intOrd));

            return prop(entries.zip(sorted).forall(p -> {
                Integer key1 = p._1().key;
                Integer key2 = p._2()._1();
                String val1 = p._1().value;
                String val2 = p._2()._2();
                return key1.equals(key2) && val1.equals(val2);
            }));
        });
    }

    /**
     * Check that processing the iterators does not remove values.
     */
    public Property get_keys_entries_values_map_is_still_present() {
        return property(isKVList, kvs -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));
            for (Integer key : tm.keys()) {}
            for (Entry<Integer, String> entry : tm.entries()) {}
            for (String value : tm.values()) {}
            return prop(kvs.forall(kv -> tm.containsKey(kv._1())));
        });
    }

    /**
     * Check that the tree is empty after clearing it.
     */

    public Property clear_tree() {
        return property(treeMap, tm -> {
            tm.clear();
            boolean empty = tm.isEmpty();
            boolean size0 = tm.size() == 0;
            boolean nokeys = fromIterator(tm.keys().iterator()).isEmpty();
            boolean novalues = fromIterator(tm.values().iterator()).isEmpty();
            boolean noentries = fromIterator(tm.entries().iterator()).isEmpty();

            return prop(nokeys && novalues && noentries && empty && size0);
        });
    }

    /**
     * Check that we can remove entries using a predicate over the key-value pair.
     *
     */
    public Property remove_if() {
        return property(isKVList, arbF(cogenInteger, arbBoolean), (kvs, predicate) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            tm.removeIf((key, value) -> predicate.f(key));

            List<Integer> keys = fromIterator(tm.keys().iterator());
            List<Integer> sorted = kvs.map(P2::_1).filter(key -> !predicate.f(key)).sort(intOrd);

            return prop(intListEqual.eq(keys, sorted));
        });
    }

    /**
     * Check that we can remove entries using a predicate over the key-value pair,
     * but don't check the order.
     *
     */
    public Property remove_if_dont_check_sort() {
        return property(isKVList, arbF(cogenInteger, arbBoolean), (kvs, predicate) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            tm.removeIf((key, value) -> predicate.f(key));

            Set<Integer> keys = Set.iteratorSet(intOrd, tm.keys().iterator());
            Set<Integer> kept = Set.iterableSet(intOrd, kvs.map(P2::_1).filter(key -> !predicate.f(key)));

            return prop(keys.equals(kept));
        });
    }

    /**
     * Check that all new keys are in the map after merging with other tree.
     */
    public Property merge_other() {
        return property(arbKVList, arbKVList, (kvs1, kvs2) -> {
            SortedTreeMap<Integer, String> tm1 = new SortedTreeMap<>(intOrd.toComparator());
            SortedTreeMap<Integer, String> tm2 = new SortedTreeMap<>(intOrd.toComparator());

            kvs1.foreachDoEffect(kv -> tm1.add(kv._1(), kv._2()));
            kvs2.foreachDoEffect(kv -> tm2.add(kv._1(), kv._2()));

            tm1.merge(tm2);

            HashMap<Integer, String> test_map = HashMap.iterableHashMap(kvs1);
            kvs2.foreachDoEffect(kv -> test_map.set(kv._1(), kv._2()));

            List<Entry<Integer, String>> entries = fromIterator(tm1.entries().iterator());

            // Check that for all entries in the map that they are the same in the hashmap.
            return prop(entries.forall(e -> {
                Integer key = e.key;
                String value = e.value;
                // Check if we get the same key from the tree map as from the input.
                String input = test_map.get(key).toNull();
                return value.equals(input);
            }));
        });
    }

    /**
     * Check that we can find the entry that is equal or has a higher key than
     * the one we are looking for.
     */
    public Property higher_or_eq_entry() {
        return property(isKVList, arbInteger, (kvs, key) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            P2<Integer, String> next = kvs.sort(p2Ord1(intOrd)).dropWhile(k -> k._1() < key).headOption().toNull();
            Entry<Integer, String> entry = tm.higherOrEqualEntry(key);

            if (next == null || entry == null) {
                return prop(entry == null && next == null);
            }

            return prop(next._1().equals(entry.key)
                    && next._2().equals(entry.value)
                    && tm.containsKey(next._1()));
        });
    }

    /**
     * Check that we can find the entry that is equal or has a lower key than
     * the one we are looking for.
     */
    public Property lower_or_eq_entry() {
        return property(isKVList, arbInteger, (kvs, key) -> {
            SortedTreeMap<Integer, String> tm = new SortedTreeMap<>(intOrd.toComparator());
            kvs.foreachDoEffect(kv -> tm.add(kv._1(), kv._2()));

            P2<Integer, String> next = kvs.sort(p2Ord1(intOrd.reverse())).dropWhile(k -> k._1() > key).headOption().toNull();
            Entry<Integer, String> entry = tm.lowerOrEqualEntry(key);

            if (next == null || entry == null) {
                return prop(entry == null && next == null);
            }

            return prop(next._1().equals(entry.key) && next._2().equals(entry.value)
                    && tm.containsKey(next._1()));
        });
    }
}
