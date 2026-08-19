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

package test;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Method;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import io.mishmash.foreign.posix.linux.x86_64.libc.sys.cmsghdr;
import io.mishmash.foreign.posix.linux.x86_64.libc.sys.iovec;
import io.mishmash.foreign.posix.linux.x86_64.libc.sys.msghdr;

import static io.mishmash.foreign.posix.linux.x86_64.libc.ErrnoH.__errno_location;
import static io.mishmash.foreign.posix.linux.x86_64.libc.UnistdH.close;

import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.mmap;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.MAP_SHARED;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.PROT_READ;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.PROT_WRITE;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.SocketH.recvmsg;

public class Client {

    public static void main(String[] args) throws Exception {
        // Make sure we're running on linux
        if (!"Linux".equals(System.getProperty("os.name"))) {
            throw new RuntimeException("Must be run on Linux");
        }

        // Create an Arena to allocate from:
        Arena arena = Arena.ofAuto();

        // connect to an Unix socket located in the current directory
        Path socketPath = Path.of(".").resolve("test_shared_memory.socket");
        UnixDomainSocketAddress sa = UnixDomainSocketAddress.of(socketPath);
        SocketChannel clientChannel = SocketChannel.open(StandardProtocolFamily.UNIX);
        if (!clientChannel.connect(sa)) {
            throw new IOException("Failed to connect!");
        }

        /*
         * Prepare to receive the shared memory object from the server,
         * via a control message (cmsg)
         */

        // First, obtain the file descriptor of the socket, we'll need it
        // for the recvmsg() call:
        Method getFDValMethod = clientChannel.getClass()
                .getDeclaredMethod("getFDVal");
        getFDValMethod.setAccessible(true);
        int clientSocketFD = (Integer) getFDValMethod
                .invoke(clientChannel);

        // allocate enough space to receive dummy data:
        MemorySegment dummyData = arena.allocate(16);
        MemorySegment iov = iovec.allocate(arena);
        iovec.iov_base(iov, dummyData);
        iovec.iov_len(iov, dummyData.byteSize());

        // allocate a buffer to receive a cmsghdr and some FDs,
        // (16 cmsghdr structs should be plenty):
        MemorySegment controlMsg = arena.allocate(cmsghdr.sizeof() * 16);

        // allocate a buffer for the actual message:
        MemorySegment msgHdr = msghdr.allocate(arena);
        // set pointers to the buffers:
        msghdr.msg_name(msgHdr, MemorySegment.ofAddress(0));
        msghdr.msg_namelen(msgHdr, 0);
        msghdr.msg_iov(msgHdr, iov);
        msghdr.msg_iovlen(msgHdr, 1);
        msghdr.msg_control(msgHdr, controlMsg);
        msghdr.msg_controllen(msgHdr, controlMsg.byteSize());
        msghdr.msg_flags(msgHdr, 0);

        System.out.println("Waiting to receive shared memory object...");

        // receive the message:
        long bytesReceived;
        while ((bytesReceived = recvmsg(clientSocketFD, msgHdr, 0)) <= 0) {
            if (bytesReceived == -1) {
                throwOnErrno("recvmsg");
            }

            Thread.sleep(500);
        }

        // now, read the control message, linux populated it for us
        // get the file descriptor which is just after the cmsghdr struct:
        int shmFD = controlMsg.get(ValueLayout.JAVA_INT, cmsghdr.sizeof());

        System.out.println("Got the file descriptor");

        // Map 1 MB of shared memory in local address space:
        long size = 1024 * 1024; // one megabyte
        MemorySegment sharedMemory = mmap(
                MemorySegment.ofAddress(0),
                size,
                PROT_READ() | PROT_WRITE(),
                MAP_SHARED(),
                shmFD,
                0);
        if (sharedMemory.address() == -1) {
            throwOnErrno("mmap");
        }

        System.out.println(String.format(
            "Got shared memory from server: [%d, %d, %d, %d], modifying...",
                sharedMemory.get(ValueLayout.JAVA_BYTE, 0),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 1),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 2),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 3)));

        // Now, lets modify a few bytes in the shared memory:
        sharedMemory.set(ValueLayout.JAVA_BYTE, 0, (byte) 5);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 1, (byte) 6);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 2, (byte) 7);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 3, (byte) 8);

        System.out.println("Done, closing...");

        clientChannel.close();
        if (close(shmFD) == -1) {
            throwOnErrno("close");
        }
    }

    private static void throwOnErrno(String syscall) throws Exception {
        // get a pointer to the actual errno:
        MemorySegment errnoPtr = __errno_location();
        // get the actual errno:
        int errno = errnoPtr.get(ValueLayout.JAVA_INT, 0);

        throw new RuntimeException(syscall + "() failed with errno " + errno);
    }
}
