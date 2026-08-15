# Java Foreign Functions & Memory for POSIX libc

Use this module to build OS-specific optimizations that need to do `syscalls.`

At [mishmash io](https://mishmash.io) we use it to implement **short-circuit, zero-copy I/O.**

> [!TIP]
>
> To learn more about **shared memory,** **short-circuiting,** **zero-copy** and how
> these can increase the performance of a system [read our blog post on how we implemented this library](https://mishmash.io/blog/shared_memory_short_circuit_java_lang_foreign_jextract)
>

