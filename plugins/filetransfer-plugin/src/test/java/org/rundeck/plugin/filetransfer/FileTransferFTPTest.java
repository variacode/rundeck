package org.rundeck.plugin.filetransfer;

import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import org.junit.Test;

/**
 * Created by tuto
 */
public class FileTransferFTPTest {


  //  @Test
  public void testFtpTOFtpTransfer() {

    String surl = "ftp://10.10.10.4/home/tuto/rundecktests/ftpsource/testfile.rnd";
//    String surl = "file:///Users/tuto/wezeeeee.tar.gz";
    String suser = "tuto";
    String spass = "ppl123";

    String durl = "ftp://10.10.10.4/home/tuto/rundecktests/ftpdest/";
    String duser = "tuto";
    String dpass = "ppl123";


    NodeEntryImpl nodeEntry = new NodeEntryImpl("testnode");

    FileTransferNodeStepPlugin ftpplugin = new FileTransferNodeStepPlugin(surl, suser, spass, durl, duser, dpass);


    try {
      ftpplugin.executeNodeStep(null, null, nodeEntry);
    } catch (NodeStepException e) {
      throw new RuntimeException(e);
    }

  }

//  @Test
  public void testFtpTOFtpWildcards() {

    String surl = "sftp://10.10.10.4/home/tuto/rundecktests/ftpsource/*.lol";
//        String surl = "file:///Users/tuto/*";
    String suser = "tuto";
    String spass = "ppl123";

    String durl = "sftp://10.10.10.4/home/tuto/rundecktests/ftpdest/";
    String duser = "tuto";
    String dpass = "ppl123";


    NodeEntryImpl nodeEntry = new NodeEntryImpl("testnode");

    FileTransferNodeStepPlugin ftpplugin = new FileTransferNodeStepPlugin(surl, suser, spass, durl, duser, dpass);


    try {
      ftpplugin.executeNodeStep(null, null, nodeEntry);
    } catch (NodeStepException e) {
      throw new RuntimeException(e);
    }

  }
}
