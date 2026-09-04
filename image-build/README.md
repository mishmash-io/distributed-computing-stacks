# Images for the distributed computing stacks

## List

- `stacks-jetty-base`
- `stacks-jetty-client`
- `stacks-jetty-server`
- `stacks-jetty-servlet`
- `stacks-gson`
- `stacks-yaml`
- `stacks-jackson`
- `stacks-jackson-yaml`
- `stacks-logging`
- `stacks-slf4j`
- `stacks-netty`
- `stacks-opentelemetry-agent`
- `stacks-sasl-oidc`
- `stacks-bouncy-castle`

- `shellprofiles-container`

- `stacks-quorum-client`
- `stacks-quorum-server`
- `stacks-quorum-admin-rest`
- `stacks-quorum-controller`


Ready to use images:

- `quorum-server`

## Using stack images

```dockerfile
FROM eclipse-temurin:25-jre-noble:latest

...
```

## Building images

## Adding new stacks

1. In a maven pom - copy the necessary dependencies to the given path (typically under `/opt/stacks`)
2. Install a shell script in `libexec/shellprofile.d` and in it add the
necessary entries to the classpath, add additional JVM args, etc.
3. Add to the build files - `Dockerfile`, `build-container-images.sh`, etc

[Apache Hadoop Unix Shell Guide](https://hadoop.apache.org/docs/r3.5.0/hadoop-project-dist/hadoop-common/UnixShellGuide.html)
