package org.lucassouza.navigation.model;

/**
 *
 * @author Lucas Souza [sorack@gmail.com]
 */
public enum Browser {
  CHROME_55("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36"),
  CHROME_54("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36"),
  CHROME_53("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36"),
  CHROME_51("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"),
  CHROME("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36)"),
  IE_11("Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko"),
  IE_7("Mozilla/4.0 (Windows; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)");

  public final String userAgent;

  private Browser(String userAgent) {
    this.userAgent = userAgent;
  }

  public static Browser getDefault() {
    return CHROME_55;
  }

  public String getUserAgent() {
    return this.userAgent;
  }
}
