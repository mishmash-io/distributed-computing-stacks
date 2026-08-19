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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Method;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.mishmash.foreign.posix.linux.x86_64.libc.ErrnoH.__errno_location;
import static io.mishmash.foreign.posix.linux.x86_64.libc.FcntlH.O_CREAT;
import static io.mishmash.foreign.posix.linux.x86_64.libc.FcntlH.O_RDWR;
import static io.mishmash.foreign.posix.linux.x86_64.libc.UnistdH.close;
import static io.mishmash.foreign.posix.linux.x86_64.libc.UnistdH.ftruncate;

import io.mishmash.foreign.posix.linux.x86_64.libc.sys.cmsghdr;
import io.mishmash.foreign.posix.linux.x86_64.libc.sys.iovec;
import io.mishmash.foreign.posix.linux.x86_64.libc.sys.msghdr;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.mmap;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.shm_open;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.shm_unlink;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.MAP_SHARED;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.PROT_READ;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.MManH.PROT_WRITE;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.SocketH.sendmsg;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.SocketH.SCM_RIGHTS;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.SocketH.SOL_SOCKET;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.StatH.S_IRUSR;
import static io.mishmash.foreign.posix.linux.x86_64.libc.sys.StatH.S_IWUSR;

public class Server {

    public static void main(String[] args) throws Exception {
        // Make sure we're running on linux
        if (!"Linux".equals(System.getProperty("os.name"))) {
            throw new RuntimeException("Must be run on Linux");
        }

        // Create an Arena to allocate from:
        Arena arena = Arena.ofAuto();

        // Allocate a null-terminated string holding the name of
        // the shared object:
        MemorySegment sharedObjectName = arena
                .allocateFrom("/my_shared_memory_object");
        // create the shared memory object and get a file descriptor:
        int shmFD = shm_open(sharedObjectName,
                O_CREAT() | O_RDWR(),
                S_IRUSR() | S_IWUSR());
        if (shmFD == -1) {
            // an error occurred
            throwOnErrno("shm_open");
        }

        /*
         * Immediately remove the shared object to prevent leakage
         * in case this process crashes.
         *
         * The shared object will continue to exist for as long as
         * someone is holding its file descriptor.
         */
        if (shm_unlink(sharedObjectName) == -1) {
            // operation failed
            throwOnErrno("shm_unlink");
        }

        // allocate some memory in the shared object:
        long size = 1024 * 1024; // allocate one megabyte
        if (ftruncate(shmFD, size) == -1) {
            // operation failed
            throwOnErrno("ftruncate");
        }

        // Map the newly allocated shared memory in our address space:
        MemorySegment sharedMemory = mmap(
                MemorySegment.ofAddress(0),
                size,
                PROT_READ() | PROT_WRITE(),
                MAP_SHARED(),
                shmFD,
                0);
        if (sharedMemory.address() == -1) {
            // operation failed
            throwOnErrno("mmap");
        }

        // Now, set a few bytes in the shared memory:
        sharedMemory.set(ValueLayout.JAVA_BYTE, 0, (byte) 1);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 1, (byte) 2);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 2, (byte) 3);
        sharedMemory.set(ValueLayout.JAVA_BYTE, 3, (byte) 4);

        // create a Unix socket in the current directory
        Path socketPath = Path.of(".").resolve("test_shared_memory.socket");
        Files.deleteIfExists(socketPath); // remove if already exists
        UnixDomainSocketAddress sa = UnixDomainSocketAddress.of(socketPath);
        ServerSocketChannel serverChannel = ServerSocketChannel.open(
                StandardProtocolFamily.UNIX);
        // bind the socket:
        serverChannel.bind(sa);

        SocketChannel acceptedChannel = null;

        System.out.println("Shared memory allocated, waiting for client...");
        // wait until a client connects:
        acceptedChannel = serverChannel.accept();

        /*
         * A client is connected now, send them the shared memory object
         * file descriptor
         */

        // first, we'll need the socket's FD for the sendmsg() call:
        Method getFDValMethod = acceptedChannel.getClass()
                .getDeclaredMethod("getFDVal");
        getFDValMethod.setAccessible(true);
        int clientSocketFD = (Integer) getFDValMethod
                .invoke(acceptedChannel);

        /*
         * Build the control message (cmsg) to send to the client
         */

        // allocate some dummy data, which is necessary:
        MemorySegment dummyData = arena.allocateFrom("A");
        // create an iovec struct for our data
        MemorySegment iov = iovec.allocate(arena);
        // populate the iovec:
        iovec.iov_base(iov, dummyData);
        iovec.iov_len(iov, dummyData.byteSize());

        /*
         * allocate the control message header PLUS space for one integer -
         * the file descriptor:
         */
        MemorySegment controlMsg = arena.allocate(
                cmsghdr.sizeof()
                + ValueLayout.JAVA_INT.byteSize());
        // populate the control message now
        cmsghdr.cmsg_level(controlMsg, SOL_SOCKET());
        cmsghdr.cmsg_type(controlMsg, SCM_RIGHTS());
        cmsghdr.cmsg_len(controlMsg,
                cmsghdr.sizeof()
                + ValueLayout.JAVA_INT.byteSize());
        // set the file descriptor just after the end of the cmsghdr:
        controlMsg.set(ValueLayout.JAVA_INT, cmsghdr.sizeof(), shmFD);

        /*
         * Allocate the actual message to send, it combines the
         * dummy data and the control message
         */

        MemorySegment msgHdr = msghdr.allocate(arena);
        // populate the necessary fields:
        msghdr.msg_name(msgHdr, MemorySegment.ofAddress(0));
        msghdr.msg_namelen(msgHdr, 0);
        msghdr.msg_iov(msgHdr, iov);
        msghdr.msg_iovlen(msgHdr, 1);
        msghdr.msg_control(msgHdr, controlMsg);
        msghdr.msg_controllen(msgHdr,
                cmsghdr.sizeof()
                + ValueLayout.JAVA_INT.byteSize());
        msghdr.msg_flags(msgHdr, 0);

        // Send the message to the client:
        long bytesSent = sendmsg(clientSocketFD, msgHdr, 0);
        if (bytesSent == -1) {
            throwOnErrno("sendmsg");
        }

        System.out.println("""
                Sent shared memory object file descriptor to client,
                waiting for it to quit...
                """);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        while ((acceptedChannel.read(buf)) != -1) {
            // ignore all received data, we're just waiting for the client
            // to close the connection
            buf = buf.clear();
        }

        // client's gone, let's check the shared memory:
        System.out.println(String.format(
                "Client terminated, shared memory is: [%d, %d, %d, %d]",
                sharedMemory.get(ValueLayout.JAVA_BYTE, 0),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 1),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 2),
                sharedMemory.get(ValueLayout.JAVA_BYTE, 3)));

        // close the Unix socket:
        serverChannel.close();

        // close the shared memory object FD:
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
