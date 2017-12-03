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

import com.dtolabs.rundeck.core.authorization.AuthContext;

import java.io.File;
import java.util.Set;

/**
 * Created by greg on 2/20/15.
 */
public interface IFrameworkNodes {
    /**
     * Gets the value of "framework.server.hostname" property
     *
     * @return Returns value of framework.server.hostname property
     */
    String getFrameworkNodeHostname();

    /**
     * Gets the value of "framework.server.name" property
     *
     * @return Returns value of framework.server.name property
     */
    String getFrameworkNodeName();

    /**
     * @return Generate a node entry for the framework with default values
     */
    NodeEntryImpl createFrameworkNode();


    /**
     * @return the nodeset consisting only of the input nodes where the specified actions are all authorized
     * @param project project name
     * @param actions action set
     * @param unfiltered nodes
     * @param authContext authoriziation
     */
    INodeSet filterAuthorizedNodes(
            String project, Set<String> actions, INodeSet unfiltered,
            AuthContext authContext
    );

    /**
     * Gets the {@link com.dtolabs.rundeck.core.common.INodeDesc} value describing the framework node
     * @return the singleton {@link com.dtolabs.rundeck.core.common.INodeDesc} object for this framework instance
     */
    INodeDesc getNodeDesc();

    /**
     * Return true if the node is the local framework node.  Compares the (logical) node names
     * of the nodes after eliding any embedded 'user@' parts.
     * @param node the node
     * @return true if the node's name is the same as the framework's node name
     */
    boolean isLocalNode(INodeDesc node);
}
