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
* PluginFileCache.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 4/12/11 6:24 PM
* 
*/
package com.dtolabs.rundeck.core.utils.cache;

import java.io.File;
import java.util.*;

/**
 * FileCache stores items associated with a file and expires them if the file is removed or modified. The {@link
 * ItemCreator} interface is used to create cacheable items
 *
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
public class FileCache<T extends FileCache.Cacheable> {
    private HashMap<File, T> cache = new HashMap<File, T>();
    private HashMap<File, Long> expiry = new HashMap<File, Long>();

    final private Expiration<T> expiration;

    /**
     * Use the {@link LastModifiedExpiration} expiration policy by default.
     */
    public FileCache() {
        this(new LastModifiedExpiration<T>());
    }

    /**
     * Use a specific expiration policy
     * @param expiration policy
     */
    public FileCache(final Expiration<T> expiration) {
        this.expiration = expiration;
    }

    /**
     * Remove entry for a file.
     * @param file file
     */
    public synchronized void remove(final File file) {
        final T t = cache.get(file);
        expiry.remove(file);
        cache.remove(file);
        if (null != t && t instanceof Expireable) {
            final Expireable exp = (Expireable) t;
            exp.expire();
        }
    }

    public Set<File> cachedFiles(){
        return Collections.unmodifiableSet(cache.keySet());
    }

    public static interface MemoFile {
        public File getFile();

        public String getMemo();
    }

    public static class MemoFileImpl implements MemoFile {
        private File file;
        private String memo;

        public MemoFileImpl(final File file, final String memo) {
            this.file = file;
            this.memo = memo;
        }

        @Override
        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        @Override
        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final MemoFileImpl memoFile = (MemoFileImpl) o;

            if (!file.equals(memoFile.file)) {
                return false;
            }
            return memo.equals(memoFile.memo);
        }

        @Override
        public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + memo.hashCode();
            return result;
        }
    }

    public static String memoFile(final File file) {
        return file.getName() + ":" + file.lastModified() + ":" + file.length();
    }

    public static MemoFile memoize(File file) {
        return new MemoFileImpl(file, memoFile(file));
    }
    /**
     * Get entry for a file, and use the creator if necessary to create it. The creator will be called if the cache is
     * out of date for the file. If the created item is equals to any existing cached item for the file then the old
     * item will not be replaced.  Otherwise, if the old item is {@link Expireable} then the {@link Expireable#expire()}
     * method will be called on it.
     *
     * @param file    the ifle
     * @param creator the item creator
     *
     * @return the item associated with the file, or null if none is found
     */
    public synchronized T get(final File file, final ItemCreator<T> creator) {
        if (!file.exists()) {
            remove(file);
            return null;
        }
        final long lastmod = file.lastModified();
        final Long cachetime = expiry.get(file);
        final T entry;
        final T orig = cache.get(file);
        if (null == cachetime || expiration.isExpired(file, cachetime, orig)) {
            entry = creator.createCacheItemForFile(file);
            if (null == entry) {
                remove(file);
                return null;
            }
            if (null != orig && !entry.equals(orig)) {
                remove(file);
                cache.put(file, entry);
            } else if (null == orig) {
                cache.put(file, entry);
            } else {
                //noop, retain orig
            }
            expiry.put(file, lastmod);
        } else {
            entry = cache.get(file);
        }
        return entry;
    }

    /**
     * Creates an item to store in the cache
     */
    public static interface ItemCreator<T> {
        /**
         * @return item to store for the file, or null to remove the association.
         * @param file file
         */
        public T createCacheItemForFile(File file);
    }

    /**
     * Determines whether a cached item has expired
     */
    public static interface Expiration<T> {
        /**
         * Return true if the item associated with the file has expired, given the last timestamp associated with the
         * item
         *
         * @param file      the file
         * @param cacheTime the time the item was last cached
         * @param item      the cached item
         *
         * @return true if the item should expire
         */
        public boolean isExpired(File file, Long cacheTime, T item);
    }

    /**
     * Expires items if the lastModified of the file is greater than the cached time for the item.
     */
    public static class LastModifiedExpiration<T> implements Expiration<T> {
        public boolean isExpired(final File file, final Long cacheTime, final T item) {
            return file.lastModified() > cacheTime;
        }
    }

    /**
     * Cacheable item for FileCache.
     */
    public static interface Cacheable {
    }

    /**
     * Allows expire method to be called for a cached item when it is expired from the cache
     */
    public static interface Expireable extends Cacheable {
        /**
         * Perform any expiration cleanup
         */
        public void expire();
    }
}
