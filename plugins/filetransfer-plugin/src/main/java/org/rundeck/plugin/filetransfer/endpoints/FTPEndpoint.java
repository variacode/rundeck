package org.rundeck.plugin.filetransfer.endpoints;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.rundeck.plugin.filetransfer.EndpointHandler;
import org.rundeck.plugin.filetransfer.util.URIParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto Hormazabal C.
 */
public class FTPEndpoint {

  private static final int DEFAULT_PORT = 21;

  /**
   * Returns a SourceEndpointHandler for FTP protocol, already connected and logged in.
   *
   * @param url Complete Server/File URL
   * @param username Server username
   * @param password Server password
   * @return The newly created source handler.
   * @throws IOException On any comm error.
   */
  public static EndpointHandler createEndpointHandler(URIParser url, final String username, final String password) throws IOException {

    final FTPClient ftpClient = new FTPClient();
    ftpClient.connect(url.getHost(), url.getPort() < 0 ? DEFAULT_PORT : url.getPort());
    ftpClient.login(username, password);
    ftpClient.enterLocalPassiveMode();
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

    return new EndpointHandler() {

      @Override
      public List<String> listFiles(String path) throws IOException {

        FTPFile[] flist = ftpClient.listFiles(path, new FTPFileFilter() {
          @Override
          public boolean accept(FTPFile file) {
            // TODO symlink case.
            return file.isFile();
          }
        });

        List<String> fileList = new ArrayList<>();
        for (FTPFile file : flist) {
          fileList.add(path + file.getName());
        }

        return fileList;
      }

      @Override
      public InputStream newTransferInputStream(String path) throws IOException {
        InputStream is = ftpClient.retrieveFileStream(path);
        if (is == null) {
          throw new IOException(String.format("Error (%d) creating stream for file [%s]: %s", ftpClient.getReplyCode(), path, ftpClient.getReplyString()));

        }
        return is;
      }

      @Override
      public OutputStream newTransferOutputStream(String path) throws IOException {
        OutputStream os = ftpClient.storeFileStream(path);
        if (os == null) {
          throw new IOException(String.format("Error (%d) creating stream for file [%s]: %s", ftpClient.getReplyCode(), path, ftpClient.getReplyString()));
        }

        return os;
      }

      @Override
      public boolean finishTransferTransaction() throws IOException {
        return ftpClient.completePendingCommand();
      }

      @Override
      public void disconnect() throws IOException {
        ftpClient.logout();
        ftpClient.disconnect();
      }
    };

  }
}
