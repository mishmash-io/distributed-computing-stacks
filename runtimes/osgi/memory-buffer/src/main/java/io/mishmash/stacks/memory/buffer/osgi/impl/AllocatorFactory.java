/*
 *    Copyright 2026 Mishmash IO UK Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.mishmash.stacks.memory.buffer.osgi.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.arrow.memory.BufferAllocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import io.mishmash.stacks.memory.buffer.osgi.api.MemoryBufferServices;
import static io.mishmash.stacks.memory.buffer.osgi.api.MemoryBufferServices.getLong;
import io.mishmash.stacks.memory.buffer.osgi.impl.OsgiAllocationListeners.Listener;

@Component(
        service={AllocatorFactory.class},
        immediate=true,
        configurationPolicy=ConfigurationPolicy.REQUIRE,
        configurationPid={AllocatorFactory.PID}
        )
public class AllocatorFactory {

    public static final String PID = "memoryBuffer";

    private static final Logger LOG =
            Logger.getLogger(AllocatorFactory.class.getName());

    @Reference(target="("
            + MemoryBufferServices.OPT_ALLOCATOR_NAME
            + "="
            + MemoryBufferServices.ROOT_ALLOCATOR_NAME
            + ")")
    private BufferAllocator rootAllocator;

    @Reference
    private OsgiAllocationListeners allocationListeners;

    private BufferAllocator allocator;
    private ServiceRegistration<BufferAllocator> serviceReg;

    @Activate
    protected void activate(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.info("Initializing a new memory buffer allocator, props: "
            + props);

        long initReservation = 0;
        long maxAllocation = Long.MAX_VALUE;
        String name = null;
        Dictionary<String, Object> serviceProps = new Hashtable<>();

        for (Map.Entry<String, Object> ent : props.entrySet()) {
            String propName = ent.getKey();
            Object propValue = ent.getValue();

            serviceProps.put(propName, propValue);

            switch (propName) {
            case MemoryBufferServices.OPT_INITIAL_RESERVATION:
                initReservation = getLong(
                        propValue,
                        0,
                        propName);
                break;
            case MemoryBufferServices.OPT_MAX_MEMORY:
                maxAllocation = getLong(
                        propValue,
                        Long.MAX_VALUE,
                        propName);
                break;
            case "service.pid":
                name = getName(propValue);
                break;
            }
        }

        name = (String) props.getOrDefault(
                MemoryBufferServices.OPT_ALLOCATOR_NAME,
                name == null ? UUID.randomUUID().toString() : name);

        Listener listener = allocationListeners.forAllocator(name);
        allocator = rootAllocator.newChildAllocator(
                name,
                listener,
                initReservation,
                maxAllocation);
        if (allocator == null) {
            throw new RuntimeException(
                    "Failed to create the '"
                    + name
                    + "' memory buffer allocator");
        } else {
            serviceReg = ctx.registerService(
                    BufferAllocator.class,
                    allocator,
                    serviceProps);
            listener.setBufferAllocator(allocator);
        }
    }

    @Modified
    protected void modify(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.warning(
                "Cannot dinamically reconfigure the '"
                + allocator.getName()
                + """
                ' buffer allocator, configuration changes will take \
                effect on next start.""");
    }

    @Deactivate
    protected void deactivate(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.info("Releasing the '"
                + allocator.getName()
                + "' buffer allocator");

        try {
            serviceReg.unregister();
            allocationListeners.unset(allocator);
        } finally {
            allocator.close();
            allocator = null;
        }
    }

    protected String getName(final Object propValue) {
        if (propValue != null
                && propValue instanceof String s
                && s.length() > PID.length() + 1) {
            return s.substring(PID.length());
        }

        return null;
    }
}
