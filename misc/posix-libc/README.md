# Java Foreign Functions & Memory for POSIX libc

Use this module to build OS-specific optimizations that need to do `syscalls.`

At [mishmash io](https://mishmash.io) we use it to implement **short-circuit, zero-copy I/O.**

> [!TIP]
>
> To learn more about **shared memory,** **short-circuiting,** **zero-copy** and how
> these can increase the performance of a system [read our blog post on how we implemented this library](https://mishmash.io/blog/shared_memory_short_circuit_java_lang_foreign_jextract)
>

## Example code

See [example code in this folder.](examples/) It contains [a server](examples/src/main/java/test/Server.java) that allocates a shared memory segment, opens a UNIX-domain socket and waits for a client to connect. Then it shares the 
memory segment with the connected client. When the client disconnects the
server prints some bytes from the shared memory, just to confirm that the
memory was indeed shared.

There's also [a client](examples/src/main/java/test/Client.java) that opens
a connection to the server, gets a shared memory segment from it and modifies
the first bytes of that shared memory, as a way of verifying that both client
and server use the same physical memory.

To build and run the examples you'll need `Apache Maven` and `Java 25:`

```sh
$ cd examples/
$ mvn package
```

Once examples are successfully built, you can run the server:

```sh
$ cd examples/
$ mvn -P server exec:exec

# which will print something like and wait for you:
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------------< io.mishmash.test:test-ffm >----------------------
[INFO] Building Java Foreign Functions and Memory API examples by mishmash io 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.6.3:exec (default-cli) @ test-ffm ---
Shared memory allocated, waiting for client...

```

When you get the `waiting for client...` message ***open another terminal*** and run the client:

```sh
$ cd examples/
$ mvn -P client exec:exec

# which will print something like:
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------------< io.mishmash.test:test-ffm >----------------------
[INFO] Building Java Foreign Functions and Memory API examples by mishmash io 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.6.3:exec (default-cli) @ test-ffm ---
Waiting to receive shared memory object...
Got the file descriptor
Got shared memory from server: [1, 2, 3, 4], modifying...
Done, closing...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.441 s
[INFO] Finished at: 2026-08-19T13:49:22+03:00
[INFO] ------------------------------------------------------------------------
```

> [!NOTE]
>
> The client prints short messages on `stdout` as it operates:
> ```
> Waiting to receive shared memory object...
> Got the file descriptor
> Got shared memory from server: [1, 2, 3, 4], modifying...
> Done, closing...
> ```

Now go back to the terminal where you ran the server and you'll see that
it has now terminated too, printing some messages:

```sh
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------------< io.mishmash.test:test-ffm >----------------------
[INFO] Building Java Foreign Functions and Memory API examples by mishmash io 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.6.3:exec (default-cli) @ test-ffm ---
Shared memory allocated, waiting for client...
Sent shared memory object file descriptor to client,
waiting for it to quit...

Client terminated, shared memory is: [5, 6, 7, 8]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  14.473 s
[INFO] Finished at: 2026-08-19T13:49:22+03:00
[INFO] ------------------------------------------------------------------------
```
