package org.lucassouza.navigation.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jsoup.Connection.Method;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public class Content {

  private final Map<String, String> headers;
  private final Set<String> fields;
  private final Browser browser;
  private final String domain;
  private final String complement;
  private final Method method;
  private final Boolean redirect;
  private final Boolean ignore;
  private final int attempts;
  private final int timeout;
  private final String raw;

  public static class Initializer {

    private Browser browser;
    private String domain;
    private int attempts;
    private int timeout;

    public Initializer() {
      this.browser = Browser.getDefault();
      this.attempts = 3;
      this.timeout = 30;
    }

    public Initializer browser(final Browser value) {
      this.browser = value;
      return this;
    }

    public Initializer domain(final String value) {
      this.domain = value;
      return this;
    }

    public Initializer attempts(final int value) {
      this.attempts = value;
      return this;
    }

    public Initializer timeout(final int value) {
      this.timeout = value;
      return this;
    }

    public Builder initialize() {
      return new Builder(this.browser, this.domain, this.attempts, this.timeout);
    }
  }

  public static class Builder {

    private Map<String, String> headers;
    private Set<String> fields;
    private Browser browser;
    private String domain;
    private String complement;
    private Method method;
    private Boolean redirect;
    private Boolean ignore;
    private int attempts;
    private int timeout;
    private String raw;

    private Builder(final Browser browser, final String domain, final int attempts, final int timeout) {
      this();
      this.browser = browser;
      this.domain = domain;
      this.attempts = attempts;
      this.timeout = timeout;
    }

    private Builder() {
      this.headers = new LinkedHashMap<>();
      this.fields = new LinkedHashSet();
      // Preenche os valores padrão
      this.complement = "";
      this.method = Method.GET;
      this.redirect = true;
      this.ignore = false;
    }

    public Builder headers(final Map<String, String> value) {
      this.headers = value;
      return this;
    }

    public Builder fields(final String... values) {
      return this.fields(new LinkedHashSet<>(Arrays.asList(values)));
    }

    public Builder fields(final Set<String> value) {
      this.fields.addAll(value);

      return this;
    }

    public Builder browser(final Browser value) {
      this.browser = value;
      return this;
    }

    public Builder domain(final String value) {
      this.domain = value;
      return this;
    }

    public Builder complement(final String value) {
      this.complement = value;
      return this;
    }

    public Builder method(final Method value) {
      this.method = value;
      return this;
    }

    public Builder redirect(final Boolean value) {
      this.redirect = value;
      return this;
    }

    public Builder ignore(final Boolean value) {
      this.ignore = value;
      return this;
    }

    public Builder attempts(final int value) {
      this.attempts = value;
      return this;
    }

    public Builder timeout(final int value) {
      this.timeout = value;
      return this;
    }

    public Builder raw(final String value) {
      this.raw = value;
      return this;
    }

    public Content build() {
      if (this.domain == null) {
        throw new IllegalStateException("The domain has to be setted.");
      }

      return new Content(this.headers, this.fields, this.browser, this.domain,
              this.complement, this.method, this.redirect, this.ignore,
              this.attempts, this.timeout, this.raw);
    }
  }

  public static Initializer initializer() {
    return new Initializer();
  }

  public static Builder builder() {
    return new Builder();
  }

  private Content(final Map<String, String> headers, final Set<String> fields,
          final Browser browser, final String domain, final String complement, final Method method,
          final Boolean redirect, final Boolean ignore, final int attempts,
          final int timeout, final String raw) {
    this.headers = headers;
    this.fields = fields;
    this.browser = browser;
    this.domain = domain;
    this.complement = complement;
    this.method = method;
    this.redirect = redirect;
    this.ignore = ignore;
    this.attempts = attempts;
    this.timeout = timeout;
    this.raw = raw;

    this.headers.put("User-Agent", this.browser.getUserAgent());
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Set<String> getFields() {
    return fields;
  }

  public Browser getBrowser() {
    return browser;
  }

  public Method getMethod() {
    return method;
  }

  public Boolean getRedirect() {
    return redirect;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public int getAttempts() {
    return attempts;
  }

  public int getTimeout() {
    return timeout;
  }

  public String getRaw() {
    return raw;
  }

  public String getUrl() {
    String ajuste;

    if (this.complement.startsWith("https:") || complement.startsWith("http:")) {
      return this.complement;
    }

    if (this.complement.startsWith("./")) {
      ajuste = this.complement.substring(2);
    } else if (this.complement.startsWith("/")) {
      ajuste = this.complement.substring(1);
    } else {
      ajuste = this.complement;
    }

    if (!this.domain.endsWith("/")) {
      ajuste = "/" + ajuste;
    }

    return this.domain + ajuste;
  }
}
