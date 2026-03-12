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

/** A dummy MemoryUtil used for compilation only. Use the MemoryUtil implementation of your chosen AllocationManager */
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
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void copyMemory(long srcAddress, long destAddress, long bytes) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void copyToMemory(byte[] src, int srcIndex, long destAddress, long bytes) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void copyFromMemory(long srcAddress, byte[] dest, int destIndex, long bytes) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static byte getByte(long address) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void putByte(long address, byte value) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static short getShort(long address) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void putShort(long address, short value) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static int getInt(long address) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void putInt(long address, int value) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static long getLong(long address) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void putLong(long address, long value) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void setMemory(long address, long bytes, byte value) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static int getInt(byte[] bytes, int index) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static long getLong(byte[] bytes, int index) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static long allocateMemory(long bytes) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }

  public static void freeMemory(long address) {
    throw new UnsupportedOperationException("Deploy a memory util class with your allocation manager");
  }
}
