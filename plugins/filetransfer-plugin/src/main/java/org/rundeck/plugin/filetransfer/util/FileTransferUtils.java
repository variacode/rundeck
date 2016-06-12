package org.rundeck.plugin.filetransfer.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.io.Util;

import java.io.File;

/**
 * Created by Alberto Hormazabal on 12-06-16.
 */
public class FileTransferUtils {

  static final String FILE_SEPARATOR = "/";

  public static boolean hasWildcards(String path) {
    return path.matches(".*[?*]+.*");
  }

  public static boolean isDirectory(String path) {
    return path.endsWith(FILE_SEPARATOR);
  }
}
