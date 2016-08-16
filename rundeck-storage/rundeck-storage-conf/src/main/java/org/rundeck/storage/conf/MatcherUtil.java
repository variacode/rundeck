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

package org.rundeck.storage.conf;

import org.rundeck.storage.api.ContentMeta;
import org.rundeck.storage.api.Path;
import org.rundeck.storage.api.PathSelector;
import org.rundeck.storage.api.ResourceSelector;

/**
 * MatcherUtil provides path and resource matching semantics
 *
 * @author greg
 * @since 2014-03-14
 */
class MatcherUtil {
    /**
     * Return true if the path and content match the specified path selector and resource selector
     *
     * @param path              path
     * @param content           content
     * @param pathSelector1     selector, or null matches all paths
     * @param resourceSelector1 resource selector, or null matches all resources
     * @param <T>               type
     *
     * @return true if both match
     */
    static <T extends ContentMeta> boolean matches(Path path, T content, PathSelector pathSelector1,
            ResourceSelector<T> resourceSelector1) {
        return matches(path, content, pathSelector1, true, resourceSelector1, true);
    }

    /**
     * Return true if the path and content match the specified path selector and resource selector
     *
     * @param path              path
     * @param content           content
     * @param pathSelector1     selector, or null matches all paths
     * @param default1          default result for path match if the path selector is null
     * @param resourceSelector1 resource selector, or null matches all resources
     * @param default2          default result for resource match if the resource selector or resource content is null
     * @param <T>               type
     *
     * @return true if both match
     */
    static <T extends ContentMeta> boolean matches(Path path, T content, PathSelector pathSelector1, boolean default1,
            ResourceSelector<T> resourceSelector1, boolean default2) {

        boolean result;
        if (null != pathSelector1) {
            result = pathSelector1.matchesPath(path);
        } else {
            result = default1;
        }
        boolean result1;
        if (null != resourceSelector1 && null != content) {
            result1 = resourceSelector1.matchesContent(content);
        } else {
            result1 = default2;
        }
        return result && result1;
    }
}
