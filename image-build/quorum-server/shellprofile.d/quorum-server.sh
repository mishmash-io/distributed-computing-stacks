#!/usr/bin/env bash
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

hadoop_add_profile quorum-server

function _quorum-server_hadoop_classpath
{
    hadoop_add_classpath "@common.path@/lib/@dependencies.commons-io@" after
    hadoop_add_classpath "@common.path@/lib/@dependencies.snappy-java@" after
    hadoop_add_classpath "@quorum.server.path@/lib/@dependencies.quorum-server@" after
}

function _opentelemetry-agent_quorum_finalize
{
    if [ -z ${DISABLE_OPENTELEMETRY_AGENT+x} ]
    then
        # TODO: don't add it for cli commands
        hadoop_add_param HADOOP_OPTS "Dotel.javaagent.configuration-file" "-Dotel.javaagent.configuration-file=@quorum.server.path@/etc/opentelemetry.properties"
    fi
}
