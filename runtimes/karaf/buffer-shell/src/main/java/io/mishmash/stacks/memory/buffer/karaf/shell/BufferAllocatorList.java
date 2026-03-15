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

package io.mishmash.stacks.memory.buffer.karaf.shell;

import java.util.Collection;
import java.util.List;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.rounding.DefaultRoundingPolicy;
import org.apache.arrow.memory.rounding.RoundingPolicy;
import org.apache.arrow.memory.rounding.SegmentRoundingPolicy;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

import io.mishmash.stacks.memory.buffer.osgi.api.MemoryBufferServices;

/**
 * Prints all open memory buffer allocators.
 */
@Service
@Command(
        scope="memory",
        name="allocator-list",
        description="List the open memory buffer allocators")
public class BufferAllocatorList implements Action {

    @Reference(
            optional=true,
            filter="("
                + MemoryBufferServices.OPT_ALLOCATOR_NAME
                + "="
                + MemoryBufferServices.ROOT_ALLOCATOR_NAME
                + ")")
    private BufferAllocator rootAllocator;

    @Override
    public Object execute() throws Exception {
        ShellTable tbl = new ShellTable();
        tbl.emptyTableText("No open memory buffer allocators");

        tbl.column("Name");
        tbl.column("Allocated");
        tbl.column("Available");
        tbl.column("Total");
        tbl.column("Should free");

        tbl.column("Peak usage");

        tbl.column("Initial reservation");
        tbl.column("Rounding policy");
        tbl.column("Children");

        if (rootAllocator != null) {
            printAllocator(rootAllocator, tbl, 0);
        }

        tbl.print(System.out);

        return null;
    }

    private void printAllocator(
            final BufferAllocator allocator,
            final ShellTable table,
            final int depth) {
        Collection<BufferAllocator> children = allocator.getChildAllocators();

        table.addRow().addContent(List.of(
                nameWithDepth(allocator, depth),
                allocator.getAllocatedMemory(),
                limitToString(allocator.getHeadroom()),
                limitToString(allocator.getLimit()),
                allocator.isOverLimit(),
                allocator.getPeakMemoryAllocation(),
                allocator.getInitReservation(),
                policyToString(allocator.getRoundingPolicy()),
                children == null ? 0 : children.size()));

        if (children != null && !children.isEmpty()) {
            for (BufferAllocator child : children) {
                printAllocator(child, table, depth + 1);
            }
        }
    }

    private String nameWithDepth(
            final BufferAllocator allocator,
            final int depth) {
        if (depth == 0) {
            return allocator.getName();
        } else {
            return " ".repeat((depth - 1) * 4 + 1)
                    + "|- " + allocator.getName();
        }
    }

    private String policyToString(final RoundingPolicy policy) {
        if (policy == null) {
            return "<not set>";
        } else if (policy instanceof DefaultRoundingPolicy d) {
            return MemoryBufferServices.ROUNDING_POLICY_DEFAULT
                    + ", chunk size: " + d.chunkSize;
        } else if (policy instanceof SegmentRoundingPolicy s) {
            return MemoryBufferServices.ROUNDING_POLICY_SEGMENT
                    + ", segment size: " + s.getSegmentSizeAsLong();
        } else {
            return "<" + policy.getClass().getName() + ">";
        }
    }

    private String limitToString(final long limit) {
        return limit == Long.MAX_VALUE
                ? "<unlimited>"
                : Long.toString(limit);
    }
}
