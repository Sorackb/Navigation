package org.lucassouza.navigation.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Navigator extends Navigation {

  protected final Map<String, String> fields;
  protected final Map<String, String> cookies;
  protected final Content.Initializer defaults;

  protected Navigator() {
    this(new HashMap<>(), new HashMap<>());
  }

  protected Navigator(Map<String, String> fields, Map<String, String> cookies) {
    this.fields = fields;
    this.cookies = cookies;
    this.defaults = Content.initializer();
  }

  protected CompletableFuture<Connection.Response> request(Content content) {
    return request(this.fields, this.cookies, content);
  }

  protected Connection.Response requestSync(Content content) {
    return requestSync(this.fields, this.cookies, content);
  }

  protected void submit(Element form) throws IOException {
    this.submit(form, this.defaults, this.fields, this.cookies);
  }

  @Override
  protected Map<String, String> retrieve(Document page, Attribute attribute, String... names) {
    Map<String, String> subfields;

    subfields = super.retrieve(page, attribute, names);
    this.fields.putAll(subfields);

    return subfields;
  }
}
