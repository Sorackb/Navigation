package org.lucassouza.navigation.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Utils {

  public static LinkedHashMap<String, String> extract(Map<String, String> source, String... fields) {
    LinkedHashMap<String, String> group = new LinkedHashMap<>();

    Arrays.asList(fields)
            .stream()
            .filter(field -> source.containsKey(field))
            .forEach(key -> {
              group.put(key, source.get(key));
            });

    return group;
  }

  public static String stripAccents(String text) {
    text = Normalizer.normalize(text, Normalizer.Form.NFD);
    text = text.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return text;
  }
}
