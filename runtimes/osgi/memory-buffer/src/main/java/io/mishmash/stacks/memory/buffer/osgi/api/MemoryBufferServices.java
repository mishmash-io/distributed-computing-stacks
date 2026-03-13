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

package io.mishmash.stacks.memory.buffer.osgi.api;

/**
 * Holds a number of shared constants and utility methods.
 */
public class MemoryBufferServices {

    /**
     * Configuration property: the buffer allocator name.
     */
    public static final String OPT_ALLOCATOR_NAME = "memoryAllocatorName";

    /**
     * Configuration value: the name of the root buffer allocator.
     */
    public static final String ROOT_ALLOCATOR_NAME = "ROOT";

    /**
     * Configuration property: sets an initial buffer
     * reservation, zero if not specified.
     */
    public static final String OPT_INITIAL_RESERVATION = "initialReservation";

    /**
     * Configuration property: sets the maximum memory that can
     * be allocated from a buffer, Long.MAX_VALUE by default.
     */
    public static final String OPT_MAX_MEMORY = "maxMemory";

    /**
     * Configuration property: the rounding policy to use
     * (applies only to the ROOT allocator).
     */
    public static final String OPT_ROUNDING_POLICY = "roundingPolicy";

    /**
     * Configuration value: default rounding policy for the
     * ROOT allocator.
     */
    public static final String ROUNDING_POLICY_DEFAULT = "default";

    /**
     * Configuration value: the 'Segment' rounding policy for the
     * ROOT allocator.
     */
    public static final String ROUNDING_POLICY_SEGMENT = "segment";

    /**
     * Configuration property: sets the segment size when using
     * the 'Segment' rounding policy.
     */
    public static final String OPT_SEGMENT_SIZE = "segmentSize";

    /**
     * Configuration value: the default segment size when using
     * the 'Segment' rounding policy.
     */
    public static final long DEFAULT_SEGMENT_SIZE = 4096;

    /**
     * Parses a (potentially missing) 'long' configuration property.
     * Throws an IllegalArgumentException or UnsupportedOperationException
     * if the value can't be parsed from string or if it is of unknown
     * type.
     *
     * @param propValue the value given (can be null)
     * @param defaultValue a default value if null
     * @param optName the name of the configuration property
     * @return a parsed long value or the default
     */
    public static long getLong(
            final Object propValue,
            final long defaultValue,
            final String optName) {
        if (propValue == null) {
            return defaultValue;
        }

        if (propValue instanceof Long l) {
            return l;
        } else if (propValue instanceof Integer i) {
            return i;
        } else if (propValue instanceof Short s) {
            return s;
        } else if (propValue instanceof Byte b) {
            return b;
        } else if (propValue instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Buffer allocator configuration error: '"
                        + optName
                        + "' must be given as an integer", e);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Buffer allocator configuration error: '"
                    + optName
                    + "' value of unknown type, must be an integer");
        }
    }
}
