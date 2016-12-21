package org.lucassouza.navigation.model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lucassouza.tools.GeneralTool;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Navigation {

  private final HashMap<String, String> fields;
  private final HashMap<String, String> cookies;
  private Response lastResponse;
  private Document page;
  private int count;

  public Navigation(HashMap<String, String> fields, HashMap<String, String> cookies) {
    this.fields = fields;
    this.cookies = cookies;
    this.count = 0;
  }

  public void request(Content content) throws IOException {
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
            .maxBodySize(5000000);//5MB

    connection.headers(content.getHeaders());

    if (content.getRaw() == null) {
      toSend = content.getFields().toArray(new String[content.getFields().size()]); // Transforma em array
      connection.data(GeneralTool.extract(this.fields, toSend));
    } else {
      connection.requestBody(content.getRaw());
    }

    this.execute(connection, content.getMethod(), content.getAttempts());
  }

  private void execute(Connection connection, Method method, int max) throws IOException {
    this.execute(connection, method, 1, max);
  }

  private void execute(Connection connection, Method method, int attempt, int max) throws IOException {
    try {
      this.lastResponse = connection.method(method).execute();
      this.page = null;
      this.cookies.putAll(this.lastResponse.cookies()); // Update cookies
    } catch (SocketTimeoutException stex) {
      Logger.getLogger(Navigation.class.getName()).log(Level.SEVERE, null, stex);
      attempt++;

      if (attempt == max) {
        System.out.println("Attempts exceeded.");
        throw stex;
      } else {
        try {
          Thread.sleep(100 * 2 ^ attempt); // The time will double in every attempt
        } catch (InterruptedException exie) {
          // There's nothing to do with this
        }

        this.execute(connection, method, attempt, max);
      }
    } catch (IOException ex) {
      throw ex;
    }
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
}
