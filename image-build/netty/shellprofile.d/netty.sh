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

hadoop_add_profile netty

function _netty_hadoop_classpath
{
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-common@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-resolver@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-buffer@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-transport@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-transport-native-unix-common@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-codec-base@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-handler@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-transport-classes-epoll@" after
    hadoop_add_classpath "@netty.path@/lib/@dependencies.netty-transport-native-epoll@" after
}

function _netty_quorum_finalize
{
    hadoop_add_param HADOOP_OPTS Dzookeeper.serverCnxnFactory "-Dzookeeper.serverCnxnFactory=org.apache.zookeeper.server.NettyServerCnxnFactory"
}
