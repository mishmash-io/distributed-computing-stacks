# OpenTelemetry Java Agent stack

This stack enables the [OpenTelemetry Java agent](https://opentelemetry.io/docs/zero-code/java/agent/) for zero-code instrumentation.

## Usage

Use this stack as per the [general instructions for using stack images.](../#readme)

For example, in a multi-stage container build copy the contents of the `/opt/stacks` directory from this stack image into your final image:

```dockerfile
FROM mishmashio/stacks-opentelemetry-agent AS stack

...

FROM my-base-image

...

COPY --from=stack /opt/stacks /opt/stacks

...

```

## Image versions

Versions of this stack image correspond to the releases of the OpenTelemetry Java agent.

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

## Configuration

The OpenTelemetry Java agent can be configured via environment variables or with a
properties file. Follow [its instructions here.](https://opentelemetry.io/docs/zero-code/java/agent/configuration/)

For an example - take a look at the [Quorum server stack,](../quorum-server/) it
sets a system property to configure the Java agent with a configuration file.

---

## About the distributed computing stacks

For a broader view on the distributed computing stacks start at the [main documentation here.](../../#readme) Also [see this document](../#readme) specifically
about the stacks images.
