package org.rundeck.plugin.filetransfer.endpoints;

import com.jcraft.jsch.*;
import org.rundeck.plugin.filetransfer.EndpointHandler;
import org.rundeck.plugin.filetransfer.util.URIParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * SFTP Endpoint for file transfer plugin.
 *
 * @author Alberto Hormazabal Cespedes.
 */
public class SFTPEndpoint {

  private static final int DEFAULT_PORT = 22;
  private static final String KNOWN_HOSTS_FILE = "~/.ssh/known_hosts";

  public static EndpointHandler createEndpointHandler(URIParser url, String username, String password) throws IOException {

    String host = url.getHost();
    int port = url.getPort() < 0 ? DEFAULT_PORT : url.getPort();

    try {

      JSch jsch = new JSch();
      jsch.setKnownHosts(KNOWN_HOSTS_FILE);

      final Session session = jsch.getSession(username, host, port);
      session.setPassword(password);
      session.connect();

      final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect();


      return new EndpointHandler() {

        @Override
        public List<String> listFiles(String path) throws IOException {

          try {

            Vector<ChannelSftp.LsEntry> flist = channel.ls(path);

            List<String> fileList = new ArrayList<>();
            for (ChannelSftp.LsEntry fileEntry : flist) {
              // Check if file is regular file. (Stat file to follow symlinks)
              SftpATTRS fattr = channel.stat(path + fileEntry.getFilename());

              if (fattr.isReg())
                fileList.add(path + fileEntry.getFilename());
            }

            return fileList;

          } catch (Exception e) {
            throw new IOException(e);
          }
        }

        @Override
        public InputStream newTransferInputStream(String path) throws IOException {
          try {
            return channel.get(path);
          } catch (SftpException e) {
            throw new IOException(e);
          }

        }

        @Override
        public OutputStream newTransferOutputStream(String path) throws IOException {
          try {
            return channel.put(path);
          } catch (SftpException e) {
            throw new IOException(e);
          }
        }

        @Override
        public boolean finishTransferTransaction() throws IOException {
          return true;
        }

        @Override
        public void disconnect() throws IOException {
          channel.disconnect();
          session.disconnect();
        }
      };

    } catch (Exception e) {
      throw new IOException("Error creating SFTP endpoint.", e);
    }

  }

}
