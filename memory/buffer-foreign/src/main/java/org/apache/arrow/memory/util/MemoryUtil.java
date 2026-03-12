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
package org.apache.arrow.memory.util;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Utilities for memory related operations. */
public class MemoryUtil {
  private static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(MemoryUtil.class);

  /** If the native byte order is little-endian. */
  public static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

  /**
   * Given a {@link ByteBuffer}, gets the address the underlying memory space.
   *
   * @param buf the byte buffer.
   * @return address of the underlying memory.
   */
  public static long getByteBufferAddress(ByteBuffer buf) {
    return MemorySegment.ofBuffer(buf).address();
  }

  private MemoryUtil() {}

  /** Create nio byte buffer. */
  public static ByteBuffer directBuffer(long address, int capacity) {
    return MemorySegment.ofAddress(address).reinterpret(capacity).asByteBuffer();
  }

  public static void copyMemory(long srcAddress, long destAddress, long bytes) {
    MemorySegment.ofAddress(destAddress).reinterpret(bytes)
        .copyFrom(MemorySegment.ofAddress(srcAddress).reinterpret(bytes));
  }

  public static void copyToMemory(byte[] src, int srcIndex, long destAddress, long bytes) {
    MemorySegment.copy(src, srcIndex,
            MemorySegment.ofAddress(destAddress)
                .reinterpret(bytes * ValueLayout.JAVA_BYTE.byteSize()),
            ValueLayout.JAVA_BYTE, 0, (int) bytes);
  }

  public static void copyFromMemory(long srcAddress, byte[] dest, int destIndex, long bytes) {
    MemorySegment.copy(
            MemorySegment.ofAddress(srcAddress)
                .reinterpret(bytes * ValueLayout.JAVA_BYTE.byteSize()),
            ValueLayout.JAVA_BYTE, 0, dest, destIndex, (int) bytes);
  }

  public static byte getByte(long address) {
    return MemorySegment.ofAddress(address)
            .reinterpret(ValueLayout.JAVA_BYTE.byteSize())
            .get(ValueLayout.JAVA_BYTE, 0);
  }

  public static void putByte(long address, byte value) {
    MemorySegment.ofAddress(address)
        .reinterpret(ValueLayout.JAVA_BYTE.byteSize())
        .set(ValueLayout.JAVA_BYTE, 0, value);
  }

  public static short getShort(long address) {
    return MemorySegment.ofAddress(address)
            .reinterpret(ValueLayout.JAVA_SHORT.byteSize())
            .get(ValueLayout.JAVA_SHORT, 0);
  }

  public static void putShort(long address, short value) {
    MemorySegment.ofAddress(address)
        .reinterpret(ValueLayout.JAVA_SHORT.byteSize())
        .set(ValueLayout.JAVA_SHORT, 0, value);
  }

  public static int getInt(long address) {
    return MemorySegment.ofAddress(address)
            .reinterpret(ValueLayout.JAVA_INT.byteSize())
            .get(ValueLayout.JAVA_INT, 0);
  }

  public static void putInt(long address, int value) {
    MemorySegment.ofAddress(address)
        .reinterpret(ValueLayout.JAVA_INT.byteSize())
        .set(ValueLayout.JAVA_INT, 0, value);
  }

  public static long getLong(long address) {
    return MemorySegment.ofAddress(address)
            .reinterpret(ValueLayout.JAVA_LONG.byteSize())
            .get(ValueLayout.JAVA_LONG, 0);
  }

  public static void putLong(long address, long value) {
    MemorySegment.ofAddress(address)
        .reinterpret(ValueLayout.JAVA_LONG.byteSize())
        .set(ValueLayout.JAVA_LONG, 0, value);
  }

  public static void setMemory(long address, long bytes, byte value) {
    MemorySegment.ofAddress(address)
        .reinterpret(bytes)
        .fill(value);
  }

  public static int getInt(byte[] bytes, int index) {
    return MemorySegment.ofArray(bytes)
            .get(ValueLayout.JAVA_INT, index);
  }

  public static long getLong(byte[] bytes, int index) {
    return MemorySegment.ofArray(bytes)
            .get(ValueLayout.JAVA_LONG, index);
  }

  public static long allocateMemory(long bytes) {
    throw new UnsupportedOperationException("Use a java.lang.foreign.Arena instead");
  }

  public static void freeMemory(long address) {
      throw new UnsupportedOperationException("Use a java.lang.foreign.Arena instead");
  }
}
