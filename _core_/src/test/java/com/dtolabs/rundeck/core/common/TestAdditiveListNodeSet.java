/*
 * Copyright 2016 SimplifyOps, Inc. (http://simplifyops.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtolabs.rundeck.core.common;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * $INTERFACE is ... User: greg Date: 1/25/14 Time: 3:01 PM
 */
@RunWith(JUnit4.class)
public class TestAdditiveListNodeSet {

    @Test
    public void test() {
        AdditiveListNodeSet merged = new AdditiveListNodeSet();

        NodeSetImpl nodeSet1 = new NodeSetImpl();
        NodeEntryImpl nodeEntry1 = new NodeEntryImpl("abc");
        nodeEntry1.setAttribute("blahblah", "blah");
        nodeEntry1.setAttribute("wakawaka", "something");
        nodeSet1.putNode(nodeEntry1);
        Assert.assertEquals("blah", nodeEntry1.getAttribute("blahblah"));
        Assert.assertEquals("something", nodeEntry1.getAttribute("wakawaka"));
        Assert.assertEquals(null, nodeEntry1.getAttribute("bloobloo"));

        NodeSetImpl nodeSet2 = new NodeSetImpl();
        NodeEntryImpl nodeEntry2 = new NodeEntryImpl("abc");
        nodeEntry2.setAttribute("bloobloo", "bloo");
        nodeEntry2.setAttribute("wakawaka", "something-else");
        nodeSet2.putNode(nodeEntry2);
        Assert.assertEquals("bloo", nodeEntry2.getAttribute("bloobloo"));
        Assert.assertEquals("something-else", nodeEntry2.getAttribute("wakawaka"));
        Assert.assertEquals(null, nodeEntry2.getAttribute("blahblah"));

        merged.addNodeSet(nodeSet1);
        merged.addNodeSet(nodeSet2);
        //result, set2 overrides set1
        INodeEntry abc = merged.getNode("abc");
        Assert.assertNotNull("should not be null", abc);
        Assert.assertNotNull("should not be null", abc.getAttributes());
        Assert.assertEquals(null, abc.getAttributes().get("blahblah"));
        Assert.assertEquals("bloo", abc.getAttributes().get("bloobloo"));
        Assert.assertEquals("something-else", abc.getAttributes().get("wakawaka"));
    }
}
