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

package com.dtolabs.rundeck.plugins.logging;

import com.dtolabs.rundeck.core.logging.StreamingLogWriter;

import java.util.Map;

/**
 * Plugin interface for streaming log writers
 */
public interface StreamingLogWriterPlugin extends StreamingLogWriter {
    /**
     * Sets the execution context information for the log information being written, will be called prior to other
     * methods {@link #openStream()}
     *
     * @param context context data
     */
    public void initialize(Map<String, ? extends Object> context);
}
