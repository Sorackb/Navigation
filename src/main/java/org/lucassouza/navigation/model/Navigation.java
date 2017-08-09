package org.lucassouza.navigation.model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lucassouza.tools.GeneralTool;
import org.lucassouza.tools.Herald;
import org.lucassouza.tools.MessageType;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Navigation {

  public CompletableFuture<Response> request(Map<String, String> fields, Map<String, String> cookies, Content content) {
    return CompletableFuture.supplyAsync(() -> requestSync(fields, cookies, content));
  }

  public Response requestSync(Map<String, String> fields, Map<String, String> cookies, Content content) {
    Map<String, String> data = new LinkedHashMap<>();
    String body = null;
    Connection connection;
    String[] toSend;
    String url = "";
    Response response;

    Herald.notify(MessageType.START_TIMER, content.getUrl());

    // Organiza o conteúdo que será enviado, seja por URL como pelo corpo da resposta
    toSend = content.getFields().toArray(new String[content.getFields().size()]);

    if (content.getRaw() == null) {
      data.putAll(GeneralTool.extract(fields, toSend));
    } else {
      body = content.getRaw();
    }

    url = content.getUrl() + url;

    connection = Jsoup.connect(url)
            .timeout(content.getTimeout() * 1000) // O método recebe o valor em ms
            .userAgent(content.getBrowser().getUserAgent())
            .cookies(cookies)
            .headers(content.getHeaders())
            .followRedirects(content.getRedirect())
            .ignoreContentType(true)
            .ignoreHttpErrors(content.getIgnore())
            .maxBodySize(0) // unlimited
            .data(data)
            .requestBody(body);

    response = this.execute(cookies, connection, content.getMethod(), content.getAttempts());
    Herald.notify(MessageType.FINISH_TIMER, content.getUrl());
    return response;
  }

  private Response execute(Map<String, String> cookies, Connection connection, Method method, int max) {
    return this.execute(cookies, connection, method, max, 1);
  }

  private Response execute(Map<String, String> cookies, Connection connection, Method method, int max, int attempt) {
    Response response;

    try {
      response = connection.method(method).execute();
      cookies.putAll(response.cookies()); // Update cookies
    } catch (SocketTimeoutException exception) {
      response = this.verifyAttempt(cookies, connection, method, exception, max, attempt);
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }

    return response;
  }

  private Response verifyAttempt(Map<String, String> cookies, Connection connection, Method method, IOException exception, int max, int attempt) {
    long sleep;

    if (attempt == max) {
      Herald.notify(MessageType.WARN, "Attempts exceeded.");
      throw new RuntimeException(exception);
    } else {
      Herald.notify(MessageType.WARN, "Timeout exceeded for the " + attempt + " attempt.");
      sleep = 100 * 2 ^ (attempt - 1); // Starting in 100 millis, the time will double in every attempt
      this.sleep(sleep);
      attempt++;
      Herald.notify(MessageType.INFO, "Starting the " + attempt + " attempt.");
      return this.execute(cookies, connection, method, max, attempt);
    }
  }

  public void submit(Element form, Content.Initializer defaults, Map<String, String> fields, Map<String, String> cookies) throws IOException {
    Map<String, String> subfields = new HashMap<>();
    Set<String> names = new LinkedHashSet<>();
    Elements inputs;
    Content content;
    String method;

    if (form.tagName().equalsIgnoreCase("form")) {
      throw new RuntimeException("The element submitted should be a <form>");
    }

    inputs = form.select("input,select");

    inputs.forEach((Element input) -> {
      String name;
      String value;

      name = input.attr("name");
      value = GeneralTool.nvl(fields.get(name), input.val(), "");
      subfields.put(name, value);
      names.add(name);
    });

    method = form.attr("method").toUpperCase();

    if (method.isEmpty()) {
      method = "GET";
    }

    content = defaults.initialize()
            .complement(form.attr("action"))
            .method(Method.valueOf(method))
            .fields(names)
            .build();

    this.requestSync(subfields, cookies, content);
  }

  protected Map<String, String> retrieve(Document page, Attribute attribute, String... names) {
    Map<String, String> subfields = new HashMap<>();
    List<String> list;

    list = Arrays.asList(names);

    list.forEach((String name) -> {
      Element element;
      String value;

      element = page.getElementsByAttributeValue(attribute.toString().toLowerCase(), name).first();

      if (element != null) {
        value = element.val();
        subfields.put(name, value);
      }
    });

    return subfields;
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      // There's nothing to do with this
    }
  }
}
