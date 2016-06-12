package org.rundeck.plugin.filetransfer;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.Password;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.io.Util;
import org.rundeck.plugin.filetransfer.endpoints.FTPEndpoint;
import org.rundeck.plugin.filetransfer.endpoints.LocalEndpoint;
import org.rundeck.plugin.filetransfer.endpoints.SFTPEndpoint;
import org.rundeck.plugin.filetransfer.util.FileTransferUtils;
import org.rundeck.plugin.filetransfer.util.URIParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Plugin for transferring files using multiple protocols.
 *
 * @author Alberto Hormazabal Cespedes
 */
@Plugin(service = ServiceNameConstants.WorkflowNodeStep, name = FileTransferNodeStepPlugin.TYPE)
@PluginDescription(title = "Transfer File", description = "Transfer a file to or from a remote node.")
public class FileTransferNodeStepPlugin implements NodeStepPlugin {

  public static final String TYPE = "filetransfer";

  @PluginProperty(title = "Source URL", required = true, defaultValue = "file:///", description = "URL for the source host. Supported protocols are: file, ftp, sftp")
  private String sourceURLString;

  @PluginProperty(title = "Source Username", required = false, description = "Username for the source server. Required only if protocol is not file://")
  private String sourceUsername;

  @Password
  @PluginProperty(title = "Source Password", required = false, description = "Password for the source ftp client. Required only if protocol is not file://")
  private String sourcePassword;

  @PluginProperty(title = "Destination URL", required = true, defaultValue = "file:///", description = "URL for the destination file. Supported protocols are: file, ftp, sftp")
  private String destURLString;

  @PluginProperty(title = "Destination Username", required = false, description = "Username for the destination server. Required only if protocol is not file://")
  private String destUsername;

  @Password
  @PluginProperty(title = "Destination Password", required = false, description = "Password for the source ftp client. Required only if protocol is not file://")
  private String destPassword;


  public enum Reason implements FailureReason {
    TRANSFER_ERROR
  }


  FileTransferNodeStepPlugin(String sourceURLString, String sourceUsername, String sourcePassword, String destURLString, String destUsername, String destPassword) {
    this.sourceURLString = sourceURLString;
    this.sourceUsername = sourceUsername;
    this.sourcePassword = sourcePassword;
    this.destURLString = destURLString;
    this.destUsername = destUsername;
    this.destPassword = destPassword;
  }


  @Override
  public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration,
                              INodeEntry entry) throws NodeStepException {

    try {

      URIParser sourceURL = new URIParser(sourceURLString);
      URIParser destURL = new URIParser(destURLString);

      EndpointHandler sourceEndpoint;
      EndpointHandler destEndpoint;

      // Check SOURCE is not a directory
      if (FileTransferUtils.isDirectory(sourceURL.getFile()))
        throw new IllegalArgumentException("Source must not be a directory");

      // Check source wildcards.
      boolean multipleSourceFiles = FileTransferUtils.hasWildcards(sourceURL.getFile());

      // Check we don't have wildcards on the directory path.
      if (FileTransferUtils.hasWildcards(FilenameUtils.getFullPath(sourceURL.getFile())))
        throw new IllegalArgumentException("Wildcards are only supported for files, not directories.");

      // Check dest path does not have wildcards.
      if (FileTransferUtils.hasWildcards(destURL.getFile()))
        throw new IllegalArgumentException("Wildcards are not allowed on destination URL");

      // If WC, check if DEST is directory (trailing /)
      if (multipleSourceFiles && !FileTransferUtils.isDirectory(destURL.getFile()))
        throw new IllegalArgumentException("Destination URL must be a directory if wildcards are specified at the source URL (remember to put a '/' at the end of your url).");

      // Build list of files.
      List<FileTransferData> transferList = new ArrayList<>();

      // Open source endpoint.
      sourceEndpoint = createEndpointHandler(sourceURL, sourceUsername, sourcePassword);

      if (multipleSourceFiles) {
        // Determine source working directory.
        String srcDir = FilenameUtils.getFullPath(sourceURL.getFile());

        // Get complete file list
        List<String> srcFileList = sourceEndpoint.listFiles(srcDir);

        // Filter it to get list of files.
        for (String filepath : srcFileList) {
          if (FilenameUtils.wildcardMatch(filepath, FilenameUtils.getName(sourceURL.getFile())))
            transferList.add(new FileTransferData(filepath, destURL.getFile() + FilenameUtils.getName(filepath)));
        }

      }
      // One file case.
      else {
        String srcPath = sourceURL.getFile();
        String dstPath = FileTransferUtils.isDirectory(destURL.getFile()) ?
            destURL.getFile() + FilenameUtils.getName(srcPath) :
            destURL.getFile();

        transferList.add(new FileTransferData(srcPath, dstPath));
      }

      // Now we have the list of files to transfer.
      // Open destination endpoint.
      destEndpoint = createEndpointHandler(destURL, destUsername, destPassword);

      // Start Copying files.
      for (FileTransferData transferSpec : transferList) {

        // Copy data between streams.
        try (
            OutputStream destOutputStream = destEndpoint.newTransferOutputStream(transferSpec.getDestPath());
            InputStream sourceInputStream = sourceEndpoint.newTransferInputStream(transferSpec.getSourcePath());
        ) {
          Util.copyStream(sourceInputStream, destOutputStream);
        }
        // At this point the streams were closed automatically.

        // Finish transaction.
        sourceEndpoint.finishTransferTransaction();
        destEndpoint.finishTransferTransaction();
      }

      // DISCONNECT ALL.
      sourceEndpoint.disconnect();
      destEndpoint.disconnect();

    } catch (Exception e) {
      throw new NodeStepException(e, Reason.TRANSFER_ERROR, entry.getNodename());
    }

  }

  private EndpointHandler createEndpointHandler(URIParser url, String user, String password) throws IOException {

    switch (url.getProtocol()) {
      case "file":
        return LocalEndpoint.createEndpointHandler(url);

      case "ftp":
        return FTPEndpoint.createEndpointHandler(url, user, password);

      case "sftp":
        return SFTPEndpoint.createEndpointHandler(url, user, password);

      default:
        throw new IllegalArgumentException("Invalid protocol specified in source URL");
    }
  }


  /**
   * Container for a specific file transfer data.
   */
  private class FileTransferData {

    private String sourcePath;
    private String destPath;


    public FileTransferData(String sourcePath, String destPath) {
      this.sourcePath = sourcePath;
      this.destPath = destPath;
    }


    public String getSourcePath() {
      return sourcePath;
    }

    public String getDestPath() {
      return destPath;
    }
  }


}
