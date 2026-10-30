#
#    Copyright 2026 Mishmash IO UK Ltd.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

FROM maven:3-eclipse-temurin-25-noble@sha256:51a751cf747bad5968979a66142d443e8d94854172c3891ae2496edec544c6b3 AS base-builder

ENV INSTALL_BASE=/opt/stacks
ENV INSTALL_COMMON_BASE=${INSTALL_BASE}/common
ENV INSTALL_JACKSON_BASE=${INSTALL_COMMON_BASE}/jackson
ENV INSTALL_LOGGING_BASE=${INSTALL_COMMON_BASE}/logging
ENV INSTALL_SECURITY_BASE=${INSTALL_BASE}/security
ENV INSTALL_SASL_BASE=${INSTALL_SECURITY_BASE}/sasl
ENV INSTALL_BOUNCY_CASTLE_BASE=${INSTALL_SECURITY_BASE}/bouncy-castle
ENV INSTALL_NETTY_BASE=${INSTALL_COMMON_BASE}/netty
ENV INSTALL_QUORUM_BASE=${INSTALL_BASE}/quorum
ENV INSTALL_QUORUM_CLIENT_BASE=${INSTALL_QUORUM_BASE}/client
ENV INSTALL_QUORUM_SERVER_BASE=${INSTALL_QUORUM_BASE}/server
ENV INSTALL_QUORUM_SERVER_ETC=${INSTALL_QUORUM_SERVER_BASE}/etc

RUN mkdir -p "${INSTALL_BASE}/bin"
RUN mkdir -p "${INSTALL_BASE}/libexec"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-base

RUN mkdir -p "${INSTALL_BASE}/libexec/shellprofile.d"

COPY scripts/bin "${INSTALL_BASE}/bin/"
COPY scripts/libexec "${INSTALL_BASE}/libexec"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-analytic-annotations

COPY analytic-annotations /tmp/analytic-annotations

RUN cd /tmp/analytic-annotations && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-guava

COPY guava /tmp/guava

RUN cd /tmp/guava && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-analytic-annotations "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-jackson

COPY json-jackson /tmp/json-jackson

RUN cd /tmp/json-jackson && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-gson

COPY json-gson /tmp/json-gson

RUN cd /tmp/json-gson && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-analytic-annotations "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-protobuf

COPY protobuf /tmp/protobuf

RUN cd /tmp/protobuf && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-analytic-annotations "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-yaml

COPY yaml-snakeyaml /tmp/yaml-snakeyaml

RUN cd /tmp/yaml-snakeyaml && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-jackson-yaml

COPY yaml-jackson /tmp/yaml-jackson

RUN cd /tmp/yaml-jackson && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-logging

COPY logging /tmp/logging

RUN cd /tmp/logging && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-slf4j

COPY logging-slf4j /tmp/logging-slf4j

RUN cd /tmp/logging-slf4j && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-bouncy-castle

COPY security-bouncy-castle /tmp/security-bouncy-castle

RUN cd /tmp/security-bouncy-castle && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-netty

COPY netty /tmp/netty

RUN cd /tmp/netty && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-sasl-oidc

COPY sasl-oidc /tmp/sasl-oidc

RUN cd /tmp/sasl-oidc && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-gson "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-opentelemetry-api

COPY opentelemetry-api /tmp/opentelemetry-api

RUN cd /tmp/opentelemetry-api && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-opentelemetry-agent

COPY opentelemetry-agent /tmp/opentelemetry-agent

RUN cd /tmp/opentelemetry-agent && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-jetty-base

COPY jetty-base /tmp/jetty-base

RUN cd /tmp/jetty-base && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-jetty-base AS stacks-jetty-client

COPY jetty-client /tmp/jetty-client

RUN cd /tmp/jetty-client && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-jetty-base AS stacks-jetty-server

COPY jetty-server /tmp/jetty-server

RUN cd /tmp/jetty-server && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-jetty-servlet

COPY jetty-servlet /tmp/jetty-servlet

RUN cd /tmp/jetty-servlet && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-vertx-core

COPY vertx-core /tmp/vertx-core

RUN cd /tmp/vertx-core && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-vertx-core AS stacks-vertx-common-grpc

COPY vertx-common-grpc /tmp/vertx-common-grpc

RUN cd /tmp/vertx-common-grpc && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-vertx-common-grpc AS stacks-vertx-server-grpc

COPY vertx-server-grpc /tmp/vertx-server-grpc

RUN cd /tmp/vertx-server-grpc && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-vertx-core AS stacks-vertx-common-web

COPY vertx-common-web /tmp/vertx-common-web

RUN cd /tmp/vertx-common-web && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM stacks-vertx-common-web AS stacks-vertx-server-web

COPY vertx-server-web /tmp/vertx-server-web

RUN cd /tmp/vertx-server-web && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-shellprofiles-container

COPY shellprofiles-container /tmp/shellprofiles-container

RUN cd /tmp/shellprofiles-container && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

#------------------------------------------------------------------------------

FROM base-builder AS stacks-quorum-client

COPY quorum-client /tmp/quorum-client

RUN cd /tmp/quorum-client && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-slf4j "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-netty "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-sasl-oidc "${INSTALL_BASE}/" "${INSTALL_BASE}/"
# needed mostly for self-signed certificates:
COPY --from=stacks-bouncy-castle "${INSTALL_BASE}/" "${INSTALL_BASE}/"

COPY --from=stacks-analytic-annotations "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM stacks-quorum-client AS stacks-quorum-server

COPY quorum-server /tmp/quorum-server

RUN cd /tmp/quorum-server && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

RUN chmod -R 755 "${INSTALL_QUORUM_SERVER_BASE}/bin/quorum.sh"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-quorum-server-controller

COPY quorum-controller /tmp/quorum-controller

RUN cd /tmp/quorum-controller && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-jetty-client "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-jetty-server "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM base-builder AS stacks-quorum-server-admin-rest

COPY quorum-admin-rest /tmp/quorum-admin-rest

RUN cd /tmp/quorum-admin-rest && mvn --offline -Dbase.path="${INSTALL_BASE}" clean initialize

COPY --from=stacks-jetty-server "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-jetty-servlet "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-jackson "${INSTALL_BASE}/" "${INSTALL_BASE}/"

#------------------------------------------------------------------------------

FROM eclipse-temurin:25-jre-noble@sha256:2ef7a81085e0502d7d4ff7e7c0257b6d4f46433f8ea6021180aa3c8a2b9ca3ec AS quorum-server

ARG INSTALL_BASE="/opt/stacks"

COPY --from=stacks-base "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-quorum-server "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-logging "${INSTALL_BASE}/" "${INSTALL_BASE}/"
# needed mostly for log4j2 yaml config:
COPY --from=stacks-jackson "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-yaml "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-jackson-yaml "${INSTALL_BASE}/" "${INSTALL_BASE}/"

COPY --from=stacks-opentelemetry-agent "${INSTALL_BASE}/" "${INSTALL_BASE}/"
COPY --from=stacks-shellprofiles-container "${INSTALL_BASE}/" "${INSTALL_BASE}/"

ENTRYPOINT ["/__cacert_entrypoint.sh", "/opt/stacks/quorum/server/bin/quorum.sh"]
CMD ["standalone"]
