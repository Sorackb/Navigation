package org.lucassouza.navigation.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Navigation {

  private final HashMap<String, String> fields;
  private final HashMap<String, String> cookies;
  private Response lastResponse;
  private Document page;

  public Navigation(HashMap<String, String> fields, HashMap<String, String> cookies) {
    this.fields = fields;
    this.cookies = cookies;
  }

  public void request(Content content) throws IOException {
    Connection connection;
    String[] sent;

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
      sent = content.getFields().toArray(new String[content.getFields().size()]); // Transforma em array
      connection.data(Utils.extract(this.fields, sent));
    } else {
      connection.requestBody(content.getRaw().toString());
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
    } catch (IOException ex) {
      //this.registrarMensagem(TipoMensagemBasico.AVISO, "Tentativa " + tentativa + " falhou. O seguinte erro ocorreu: " + ex.toString());
      Logger.getLogger(Navigation.class.getName()).log(Level.SEVERE, null, ex);
      attempt++;

      if (attempt == max) {
        throw new IOException("Attempts exceeded.");
      } else {
        try {
          Thread.sleep(50);
        } catch (InterruptedException ex1) {
          // There's nothing to do with this
        }
        this.execute(connection, method, attempt, max);
      }
    }
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
