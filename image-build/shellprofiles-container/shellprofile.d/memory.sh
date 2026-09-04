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

hadoop_add_profile container-memory

function _container-memory_hadoop_finalize
{
    # cgroup2 version:
    max_memory=$(cat /sys/fs/cgroup/memory.max 2> /dev/null || echo "max")

    hadoop_debug "Detected value '$max_memory' for maximum allowed memory"
    if [ "$max_memory" != "max" ]
    then
        # use 90% of max memory, round down to KB
        hadoop_add_param HADOOP_OPTS Xmx "-Xmx$((max_memory*9/10/1024))k"
    else
        # just use a default value
        hadoop_add_param HADOOP_OPTS Xmx "-Xmx2g"
    fi
}
