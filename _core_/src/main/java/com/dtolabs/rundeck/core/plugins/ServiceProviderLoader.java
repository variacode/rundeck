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
* ProviderLoaderService.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 4/12/11 2:42 PM
* 
*/
package com.dtolabs.rundeck.core.plugins;

import com.dtolabs.rundeck.core.execution.service.ProviderLoaderException;

import java.util.List;

/**
 * ServiceProviderLoader creates a service provider instance given a provider name
 *
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
public interface ServiceProviderLoader {
    /**
     * Create a provider instance for the service
     *
     * @param <T> service class
     * @param service      the service
     * @param providerName the name of the provider to load
     *
     * @return the provider instance
     *
     * @throws ProviderLoaderException if the provider cannot be found or there is an error loading it
     */
    public <T> T loadProvider(PluggableService<T> service, String providerName) throws
        ProviderLoaderException;

    /**
     * Create a provider instance for the service
     *
     * @param service      service
     * @param providerName provider
     * @param <T>          service type
     *
     * @return closeable provider which can close loaded resources at completion
     */
    <T> CloseableProvider<T> loadCloseableProvider(PluggableService<T> service, String providerName) throws
            ProviderLoaderException;

    public PluginResourceLoader getResourceLoader(String service, String provider) throws ProviderLoaderException;

    public PluginMetadata getPluginMetadata(String service, String provider) throws ProviderLoaderException;

    /**
     * Create a plugin service for the given type
     * @param type base interface class for the service
     * @param serviceName name of the service
     * @param <T> base type
     * @return service
     */
    <T> PluggableProviderService<T> createPluginService(Class<T> type, final String serviceName);

    /**
     * @return the available providers
     */
    public List<ProviderIdent> listProviders();
}
