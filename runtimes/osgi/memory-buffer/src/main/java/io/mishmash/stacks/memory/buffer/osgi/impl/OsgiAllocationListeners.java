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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.arrow.memory.AllocationListener;
import org.apache.arrow.memory.AllocationOutcome;
import org.apache.arrow.memory.BufferAllocator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.mishmash.stacks.memory.buffer.osgi.api.MemoryBufferServices;

/**
 * Listens for service registrations on the {@link AllocationListener}
 * interface and then calls them.
 *
 * AllocationListeners should set the 'memoryAllocatorName' property when
 * registering to be attached to the correct buffer allocator.
 */
@Component(immediate=true)
public class OsgiAllocationListeners {

    private static final Logger LOG = Logger.getLogger(
            OsgiAllocationListeners.class.getName());

    private static Map<
                    String,
                    ConcurrentLinkedQueue<AllocationListener>> listeners =
            new ConcurrentHashMap<>();

    @Reference(
            service=AllocationListener.class,
            cardinality=ReferenceCardinality.MULTIPLE,
            policy=ReferencePolicy.DYNAMIC)
    protected void addListener(
            final AllocationListener listener,
            final Map<String, Object> props) {
        String name = (String) props.get(
                MemoryBufferServices.OPT_ALLOCATOR_NAME);

        if (name != null) {
            listeners.compute(name,
                    (k, v) -> {
                        ConcurrentLinkedQueue<AllocationListener> q =
                                v == null
                                ? new ConcurrentLinkedQueue<>()
                                : v;
                        q.add(listener);
                        return q;
                    });
        } else {
            LOG.warning("""
                Attempted to register a memory buffer allocation listener \
                without a name, ignoring""");
        }
    }

    protected void updatedListener(
            final AllocationListener listener,
            final Map<String, Object> props) {
        // nothing to do
    }

    protected void removeListener(
            final AllocationListener listener,
            final Map<String, Object> props) {
        String name = (String) props.get(
                MemoryBufferServices.OPT_ALLOCATOR_NAME);

        if (name != null) {
            listeners.computeIfPresent(name,
                    (k, v) -> {
                        v.remove(listener);
                        return v.isEmpty() ? null : v;
                    });
        } else {
            LOG.warning("""
                Attempted to remove a memory buffer allocation listener \
                without a name, ignoring""");
        }
    }

    protected Listener forAllocator(final String name) {
        return new Listener(name);
    }

    protected void unset(final BufferAllocator buf) {
        unset(buf.getName());
    }

    protected void unset(final String name) {
        listeners.remove(name);
    }

    protected void onListeners(
            final String bufferName,
            final Consumer<AllocationListener> consumer) {
        onListeners(bufferName,
                l -> {
                    consumer.accept(l);
                    return (Void) null;
                },
                (a, b) -> (Void) null);
    }

    protected <T> T onListeners(
            final String bufferName,
            final Function<AllocationListener, T> function,
            final BinaryOperator<T> reducer) {
        return listeners.getOrDefault(
                bufferName,
                new ConcurrentLinkedQueue<>())
            .stream()
            .map(l -> {
                try {
                    return function.apply(l);
                } catch (Throwable e) {
                    LOG.log(Level.SEVERE,
                            "'"
                            + bufferName
                            + "' memory buffer allocation listener failed",
                            e);

                    return null;
                }
            })
            .reduce(null, reducer);
    }

    protected class Listener implements AllocationListener {
        private BufferAllocator bufferAllocator;
        private String bufferAllocatorName;

        protected Listener(final String name) {
            bufferAllocatorName = name;
        }

        protected void setBufferAllocator(final BufferAllocator allocator) {
            bufferAllocator = allocator;
        }

        @Override
        public void onPreAllocation(final long size) {
            onListeners(bufferAllocatorName,
                    l -> l.onPreAllocation(size));
        }

        @Override
        public void onAllocation(final long size) {
            onListeners(bufferAllocatorName,
                    l -> l.onAllocation(size));
        }

        @Override
        public void onRelease(final long size) {
            onListeners(bufferAllocatorName,
                    l -> l.onRelease(size));
        }

        @Override
        public boolean onFailedAllocation(
                final long size,
                final AllocationOutcome outcome) {
            return onListeners(bufferAllocatorName,
                    l -> l.onFailedAllocation(size, outcome),
                    (a, b) -> Boolean.TRUE.equals(a)
                                || Boolean.TRUE.equals(b));
        }

        @Override
        public void onChildAdded(
                final BufferAllocator parentAllocator,
                final BufferAllocator childAllocator) {
            onListeners(bufferAllocatorName,
                    l -> l.onChildAdded(parentAllocator, childAllocator));
        }

        @Override
        public void onChildRemoved(
                final BufferAllocator parentAllocator,
                final BufferAllocator childAllocator) {
            onListeners(bufferAllocatorName,
                    l -> l.onChildRemoved(parentAllocator, childAllocator));
        }
    }
}
