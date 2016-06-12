package org.rundeck.plugin.filetransfer.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URI Parser needed because URL class refuses to parse unknown protocols (ex: sftp).
 * Obstacle Corp. did it again...
 *
 * @author Alberto Hormazabal C.
 */
public class URIParser {

  private final URI uri;

  public URIParser(String str) throws URISyntaxException {
    uri = new URI(str);
  }

  public String getFile() {
    return uri.getQuery() == null ? uri.getPath() : uri.getPath() + "?" + uri.getQuery();
  }

  public String getScheme() {
    return uri.getScheme();
  }

  public String getProtocol() {
    return getScheme();
  }

  public String getUserInfo() {
    return uri.getUserInfo();
  }

  public String getHost() {
    return uri.getHost();
  }

  public int getPort() {
    return uri.getPort();
  }

  public String getPath() {
    return uri.getPath();
  }

  public String getQuery() {
    return uri.getQuery();
  }

  public String toString() {
    return uri.toString();
  }

  public String getFragment() {
    return uri.getFragment();
  }

  public String getSchemeSpecificPart() {
    return uri.getSchemeSpecificPart();
  }

  public String getAuthority() {
    return uri.getAuthority();
  }
}

