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

hadoop_add_profile log4j2

function _log4j2_hadoop_classpath
{
    hadoop_add_classpath "@logging.path@/lib/@dependencies.log4j-api@" after
    hadoop_add_classpath "@logging.path@/lib/@dependencies.log4j-core@" after
    hadoop_add_classpath "@logging.path@/lib/@dependencies.log4j-jul@" after
}

function _log4j2_hadoop_finalize
{
    if [ ! -z ${DEBUG_LOG4J+x} ]
    then
        # if DEBUG_LOG4J is set (to anything, including empty string), enable log4j2 debugging
        hadoop_add_param HADOOP_OPTS Dlog4j2.debug "-Dlog4j2.debug=true"
    fi

    hadoop_add_param HADOOP_OPTS Djava.util.logging.manager "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
}
