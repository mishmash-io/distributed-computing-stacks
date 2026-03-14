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
import java.util.logging.Logger;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.memory.rounding.DefaultRoundingPolicy;
import org.apache.arrow.memory.rounding.SegmentRoundingPolicy;
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
import io.mishmash.stacks.memory.buffer.osgi.impl.OsgiAllocationListeners.OsgiAllocationListener;

@Component(
        immediate=true,
        configurationPolicy=ConfigurationPolicy.REQUIRE,
        configurationPid={"memoryBufferRoot"}
        )
public class RootAllocatorFactory {

    private static final Logger LOG =
            Logger.getLogger(AllocatorFactory.class.getName());

    private RootAllocator rootAllocator;
    private ServiceRegistration<BufferAllocator> serviceReg;

    @Reference
    private OsgiAllocationListeners listeners;

    @Activate
    protected void activate(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.info("Creating the root buffer allocator, props: " + props);

        var cfg = RootAllocator.configBuilder()
                .from(RootAllocator.defaultConfig());

        Dictionary<String, Object> serviceProps = new Hashtable<>();
        for (Map.Entry<String, Object> ent : props.entrySet()) {
            String propName = ent.getKey();
            Object propValue = ent.getValue();

            serviceProps.put(propName, propValue);

            switch (propName) {
            case MemoryBufferServices.OPT_INITIAL_RESERVATION:
                cfg = cfg.initReservation(
                        getLong(propValue, 0, propName));
                break;
            case MemoryBufferServices.OPT_MAX_MEMORY:
                cfg = cfg.maxAllocation(
                        getLong(propValue, Long.MAX_VALUE, propName));
                break;
            case MemoryBufferServices.OPT_ROUNDING_POLICY:
                if (propValue == null || MemoryBufferServices
                        .ROUNDING_POLICY_DEFAULT
                        .equals(propValue)) {
                    cfg = cfg.roundingPolicy(
                            DefaultRoundingPolicy.DEFAULT_ROUNDING_POLICY);
                } else if (MemoryBufferServices
                        .ROUNDING_POLICY_SEGMENT
                        .equals(propValue)) {
                    cfg = cfg.roundingPolicy(
                            new SegmentRoundingPolicy(
                                getLong(
                                    props.get(MemoryBufferServices
                                        .OPT_SEGMENT_SIZE),
                                    MemoryBufferServices
                                        .DEFAULT_SEGMENT_SIZE,
                                    MemoryBufferServices
                                        .OPT_SEGMENT_SIZE)));
                } else {
                    throw new IllegalArgumentException(
                        "Root buffer allocator configuration error: "
                        + MemoryBufferServices.OPT_ROUNDING_POLICY
                        + " should be set to one of '"
                        + MemoryBufferServices.ROUNDING_POLICY_DEFAULT
                        + "' or '"
                        + MemoryBufferServices.ROUNDING_POLICY_SEGMENT);
                }
                break;
            }
        }
        serviceProps.put(
                MemoryBufferServices.OPT_ALLOCATOR_NAME,
                MemoryBufferServices.ROOT_ALLOCATOR_NAME);

        OsgiAllocationListener listener = listeners.forAllocator(
                MemoryBufferServices.ROOT_ALLOCATOR_NAME);
        cfg = cfg.listener(listener);

        rootAllocator = new RootAllocator(cfg.build());

        try {
            serviceReg = ctx.registerService(
                BufferAllocator.class,
                rootAllocator,
                serviceProps);
            listener.setBufferAllocator(rootAllocator);
        } catch (Exception e) {
            rootAllocator.close();
            rootAllocator = null;
            throw e;
        }
    }

    @Modified
    protected void modify(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.warning("""
                Cannot dynamically reconfigure the root buffer allocator, \
                configuration changes will take effect on next start.""");
    }

    @Deactivate
    protected void deactivate(
            final BundleContext ctx,
            final Map<String, Object> props) {
        LOG.info("Releasing the root buffer allocator");

        try {
            serviceReg.unregister();
            listeners.unset(rootAllocator);
        } finally {
            rootAllocator.close();
            rootAllocator = null;
        }
    }
}
