package org.rundeck.plugin.filetransfer.endpoints;

import org.rundeck.plugin.filetransfer.EndpointHandler;
import org.rundeck.plugin.filetransfer.util.URIParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto Hormazabal C.
 */
public class LocalEndpoint {


  public static EndpointHandler createEndpointHandler(final URIParser url) throws IOException {

    return new EndpointHandler() {

      @Override
      public List<String> listFiles(String path) throws IOException {
        File dir = new File(path);
        File[] list = dir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });

        List<String> fileList =  new ArrayList<>();
        for(File file: list) {
          fileList.add(file.getAbsolutePath());
        }

        return fileList;
      }

      @Override
      public InputStream newTransferInputStream(String path) throws IOException {
        return new BufferedInputStream(new FileInputStream(path));
      }

      @Override
      public OutputStream newTransferOutputStream(String path) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(path));
      }


      @Override
      public boolean finishTransferTransaction() throws IOException {
        // nothing to do.
        return true;
      }

      @Override
      public void disconnect() throws IOException {
        // nothing to do.
      }
    };

  }


}
