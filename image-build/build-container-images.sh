#!/bin/bash
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

stages="stacks-jackson stacks-gson stacks-yaml stacks-jackson-yaml \
    stacks-logging stacks-slf4j stacks-bouncy-castle stacks-netty \
    stacks-sasl-oidc stacks-opentelemetry-agent stacks-jetty-base \
    stacks-jetty-client stacks-jetty-server stacks-jetty-servlet \
    stacks-quorum-client stacks-quorum-server quorum-server \
    stacks-quorum-server-controller stacks-quorum-server-admin-rest \
    "

for stage in $stages
do
    echo "Building stage: $stage"
    podman build --target "$stage" \
        --tag "docker.io/mishmashio/$stage:latest" \
        --tag "docker.io/mishmashio/$stage:1.1.2" \
        -v=$HOME/.m2:/root/.m2:rw,Z .
done
