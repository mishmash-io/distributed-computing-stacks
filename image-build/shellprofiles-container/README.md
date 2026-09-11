# Utility scripts for containerized environments

Contains various extra utility scripts for when running stacks in containerized environments.

## Scripts

- `shellprofile.d/memory.sh`
  
  Sets the `Xmx` maximum Java heap based on the container memory limits,
  or to a default value of 2G if container memory is not limited.

  Uses Linux cgroups2 `/sys/fs/cgroup/memory.max` file to determine the container's memory limit. Sets `Xmx` to 90% of the limit to allow some headroom inside the container.

## Usage

Use this stack as per the [general instructions for using stack images.](../#readme)

For example, in a multi-stage container build copy the contents of the `/opt/stacks` directory from this stack image into your final image:

```dockerfile
FROM mishmashio/stacks-shellprofiles-container AS stack

...

FROM my-base-image

...

COPY --from=stack /opt/stacks /opt/stacks

...

```

## Image versions

> [!WARNING]
>
> Container image tags are not immutable. That is, one tag may point to different
> images (and content) over time.
>
> We strongly recommend using using concrete hashes AND some dependency tracking
> mechanism that will notify you when we update the images.
>
> Doing both is the only way to be sure you are consistently deploying CVE-free
> software.

---

## About the distributed computing stacks

For a broader view on the distributed computing stacks start at the [main documentation here.](../../#readme) Also [see this document](../#readme) specifically
about the stacks images.
