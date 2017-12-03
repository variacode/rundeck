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

/*
* FilePluginCache.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 4/12/11 3:45 PM
* 
*/
package com.dtolabs.rundeck.core.plugins;

import com.dtolabs.rundeck.core.execution.service.ProviderLoaderException;
import com.dtolabs.rundeck.core.utils.PairImpl;
import com.dtolabs.rundeck.core.utils.cache.FileCache;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * FilePluginCache uses a filecache and a set of {@link PluginScanner}s to cache and create {@link ProviderLoader}
 * instances associated with files.
 * <br>
 * The instances are returned for {@link ProviderIdent} instances.
 *
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
public class FilePluginCache implements PluginCache {
    static Logger log = Logger.getLogger(FilePluginCache.class.getName());

    /**
     * Scanners to use
     */
    public List<PluginScanner> getScanners() {
        return scanners;
    }

    public void setScanners(List<PluginScanner> scanners) {
        this.scanners = scanners;
    }

    /**
     * Cache item is a File, PluginDirScanner pair
     */
    static class cacheItem extends PairImpl<File, PluginScanner> {
        cacheItem(final File first, final PluginScanner second) {
            super(first, second);
        }
    }

    /**
     * Cache a file,pluginscanner for each ident
     */
    final private HashMap<ProviderIdent, cacheItem> cache = new HashMap<ProviderIdent, cacheItem>();

    /**
     * the filecache of loaders associated with files
     */
    final private FileCache<ProviderLoader> filecache;
    private List<PluginScanner> scanners;

    FilePluginCache(final FileCache<ProviderLoader> filecache) {
        this.filecache = filecache;
        scanners = new ArrayList<PluginScanner>();
    }

    /**
     * Add a new scanner
     */
    public void addScanner(final PluginScanner scanner) {
        getScanners().add(scanner);
    }

    /**
     * Remove the association with ident, and remove any filecache association as well.
     */
    private void remove(final ProviderIdent ident) {
        final cacheItem cacheItem = cache.get(ident);
        if (null != cacheItem) {
            filecache.remove(cacheItem.getFirst());
        }
        cache.remove(ident);
    }

    /**
     * Get the loader for the provider
     *
     * @param ident provider ident
     *
     * @return loader for the provider
     */
    public synchronized ProviderLoader getLoaderForIdent(final ProviderIdent ident) throws ProviderLoaderException {
        final cacheItem cacheItem = cache.get(ident);
        if (null == cacheItem) {
            return rescanForItem(ident);
        }

        final File file = cacheItem.getFirst();
        if (cacheItem.getSecond().isExpired(ident, file)) {
            remove(ident);
            return rescanForItem(ident);
        } else {
            return loadFileProvider(cacheItem);
        }
    }

    public List<ProviderIdent> listProviders() {
        final ArrayList<ProviderIdent> providerIdents = new ArrayList<ProviderIdent>();
        for (final PluginScanner scanner : getScanners()) {
            providerIdents.addAll(scanner.listProviders());
        }
        return providerIdents;
    }


    /**
     * return the loader stored in filecache for the file and scanner
     */
    private ProviderLoader loadFileProvider(final cacheItem cached) {
        final File file = cached.getFirst();
        final PluginScanner second = cached.getSecond();
        return filecache.get(file, second);
    }

    /**
     * Rescan for the ident and cache and return the loader
     */
    private synchronized ProviderLoader rescanForItem(final ProviderIdent ident) throws ProviderLoaderException {
        File candidate = null;
        PluginScanner cscanner = null;
        for (final PluginScanner scanner : getScanners()) {
            final File file = scanner.scanForFile(ident);
            if (null != file) {
                if (null != candidate) {
                    throw new ProviderLoaderException(
                            "More than one plugin file matched: " + file + ", and " + candidate,
                            ident.getService(), ident.getProviderName()
                    );
                }
                candidate = file;
                cscanner = scanner;
            }
        }
        if (null != candidate) {
            final cacheItem cacheItem = new cacheItem(candidate, cscanner);
            cache.put(ident, cacheItem);
            return loadFileProvider(cacheItem);
        }
        return null;
    }
}
