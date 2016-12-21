package org.lucassouza.navigation.model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lucassouza.tools.GeneralTool;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Navigation {

  private final HashMap<String, String> fields;
  private final HashMap<String, String> cookies;
  private final Content.Initializer defaults;
  private Response lastResponse;
  private Document page;
  private int count;

  public Navigation() {
    this(new HashMap<>(), new HashMap<>());
  }

  public Navigation(HashMap<String, String> fields, HashMap<String, String> cookies) {
    this.fields = fields;
    this.cookies = cookies;
    this.count = 0;
    this.defaults = Content.initializer();
  }

  public void request(Content content) throws IOException {
    this.request(this.fields, content);
  }

  private void request(HashMap<String, String> subfields, Content content) throws IOException {
    Connection connection;
    String[] toSend;

    this.count++;
    connection = Jsoup.connect(content.getUrl())
            .timeout(content.getTimeout() * 1000) // O método recebe o valor em ms
            .userAgent(content.getBrowser().getUserAgent())
            .cookies(this.cookies)
            .followRedirects(content.getRedirect())
            .ignoreContentType(true)
            .ignoreHttpErrors(content.getIgnore())
            .maxBodySize(0); // unlimited

    connection.headers(content.getHeaders());

    if (content.getRaw() == null) {
      toSend = content.getFields().toArray(new String[content.getFields().size()]); // Transforma em array
      connection.data(GeneralTool.extract(subfields, toSend));
    } else {
      connection.requestBody(content.getRaw());
    }

    this.execute(connection, content.getMethod(), content.getAttempts());
  }

  private void execute(Connection connection, Method method, int max) throws IOException {
    this.execute(connection, method, max, 1);
  }

  private void execute(Connection connection, Method method, int max, int attempt) throws IOException {
    try {
      this.lastResponse = connection.method(method).execute();
      this.page = null;
      this.cookies.putAll(this.lastResponse.cookies()); // Update cookies
    } catch (SocketTimeoutException exception) {
      attempt++;

      if (attempt == max) {
        System.out.println("Attempts exceeded.");
        throw exception;
      } else {
        this.sleep(100 * 2 ^ (attempt - 1)); // Starting in 100 millis, the time will double in every attempt
        this.execute(connection, method, max, attempt);
      }
    }
  }

  public void submit(Element form) throws IOException {
    HashMap<String, String> subfields = new HashMap<>();
    LinkedHashSet<String> names = new LinkedHashSet<>();
    Elements inputs;
    Content content;
    String method;

    if (form.tagName().equalsIgnoreCase("form")) {
      throw new RuntimeException("The element submitted should be a <form>");
    }

    inputs = form.select("input,select");

    inputs.forEach(input -> {
      String name;
      String value;

      name = input.attr("name");
      value = GeneralTool.nvl(this.fields.get(name), input.val(), "");
      subfields.put(name, value);
      names.add(name);
    });

    method = form.attr("method").toUpperCase();

    if (method.isEmpty()) {
      method = "GET";
    }

    content = this.defaults.initialize()
            .complement(form.attr("action"))
            .method(Method.valueOf(method))
            .fields(names)
            .build();

    this.request(subfields, content);
  }

  public HashMap<String, String> retrieve(String... names) {
    Map<String, String> subfields;
    Document current;
    List<String> list;

    current = this.getPage(); // update the page
    list = Arrays.asList(names);

    list.forEach(name -> {
      String value;

      value = current.getElementsByAttributeValue("name", name).val();
      this.fields.put(name, value);
    });

    subfields = this.fields
            .entrySet()
            .stream()
            .filter(item -> list.contains(item.getKey()))
            .collect(Collectors.toMap(item -> item.getKey(), item -> item.getValue()));

    return new HashMap<>(subfields);
  }

  public Content.Initializer getDefaults() {
    return this.defaults;
  }

  public int count() {
    return count;
  }

  public Response getLastResponse() {
    return this.lastResponse;
  }

  public Document getPage() {
    // Só realiza o parse se a página estiver vazia
    if (this.page == null) {
      try {
        this.page = this.lastResponse.parse();
      } catch (IOException ex) {
        return null;
      }
    }

    return this.page;
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      // There's nothing to do with this
    }
  }
}
