/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.arrow.memory.foreign;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.apache.arrow.memory.AllocationManager;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.ReferenceManager;

public class ForeignAllocationManager extends AllocationManager {

    public static final ForeignAllocationManagerFactory FACTORY =
            new ForeignAllocationManagerFactory();

    private static final MemorySegment EMPTY_SEGMENT =
            Arena.global().allocate(0);
    private static final ArrowBuf EMPTY_BUF = new ArrowBuf(
            ReferenceManager.NO_OP,
            null,
            0,
            EMPTY_SEGMENT.address());

    private MemorySegment allocatedSegment;
    private long allocationSize;
    private Arena allocationArena;

    public ForeignAllocationManager(
            final BufferAllocator accountingAllocator,
            final long requestedSize) {
        super(accountingAllocator);
        allocationSize = requestedSize;
        allocationArena = Arena.ofShared();
        allocatedSegment = allocationArena.allocate(allocationSize);
    }

    @Override
    public long getSize() {
        return allocationSize;
    }

    @Override
    protected long memoryAddress() {
        return allocatedSegment.address();
    }

    @Override
    protected void release0() {
        try {
            allocationArena.close();
        } finally {
            allocationArena = null;
            allocatedSegment = null;
        }
    }

    public static class ForeignAllocationManagerFactory
            implements AllocationManager.Factory {

        @Override
        public AllocationManager create(
                final BufferAllocator accountingAllocator,
                final long size) {
            return new ForeignAllocationManager(
                    accountingAllocator,
                    size);
        }

        @Override
        public ArrowBuf empty() {
            return EMPTY_BUF;
        }
    }
}
