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

HADOOP_SHELL_EXECNAME="quorum"
bin=$(cd -P -- "$(dirname -- "${BASH_SOURCE-$0}")" >/dev/null && pwd -P)

function quorum_shellprofiles_finalize
{
  local i

  for i in ${HADOOP_SHELL_PROFILES}
  do
    if declare -F _${i}_quorum_finalize >/dev/null ; then
       hadoop_debug "Profiles: ${i} finalize quorum"
       # shellcheck disable=SC2086
       _${i}_quorum_finalize
    fi
  done
}

function hadoop_usage
{
  hadoop_reset_usage

  hadoop_add_subcommand "classpath" server "prints the class path needed to get the quorum server jar and the required libraries"
  hadoop_add_subcommand "standalone" server "launches a standalone quorum"
  hadoop_add_subcommand "peer" server "launches a quorum peer"

  hadoop_generate_usage "${HADOOP_SHELL_EXECNAME}" false "COMMAND"
}

function quorumcmd_case
{
  subcmd=$1
  shift

  case ${subcmd} in
    standalone)
      HADOOP_SUBCMD_SUPPORTDAEMONIZATION=false
      HADOOP_CLASSNAME=org.apache.zookeeper.server.ZooKeeperServerMain
      if [ $# -eq 0 ]
      then
        hadoop_debug "Using default configuration file"
        HADOOP_SUBCMD_ARGS=("@quorum.server.path@/etc/zoo.cfg")
      fi
      ;;
    peer)
      HADOOP_SUBCMD_SUPPORTDAEMONIZATION=false
      HADOOP_CLASSNAME=org.apache.zookeeper.server.quorum.QuorumPeerMain
      if [ $# -eq 0 ]
      then
        hadoop_debug "Using default configuration file"
        HADOOP_SUBCMD_ARGS=("@quorum.server.path@/etc/zoo.cfg")
      fi
      ;;
    classpath)
      hadoop_do_classpath_subcommand HADOOP_CLASSNAME "$@"
      ;;
    *)
      HADOOP_CLASSNAME="${subcmd}"
      if ! hadoop_validate_classname "${HADOOP_CLASSNAME}"; then
        hadoop_exit_with_usage 1
      fi
      ;;
  esac
}

if [ ! -z ${DEBUG_SHELL_SCRIPT+x} ]
then
  HADOOP_SHELL_SCRIPT_DEBUG="true"
fi

#JAVA_HOME=/usr/lib/jvm/java-25

# hadoop-functions.sh, hadoop_basic_init
HADOOP_COMMON_HOME=@base.path@
HADOOP_HDFS_HOME=@base.path@
HADOOP_YARN_HOME=@base.path@
HADOOP_MAPRED_HOME=@base.path@

# for shell profiles
HADOOP_LIBEXEC_DIR=@base.path@/libexec
HADOOP_NEW_CONFIG=true
# Override some functions (in hadoop-functions) with quorum-specific versions
HADOOP_CONF_DIR=@quorum.server.path@/conf

. ${bin}/quorum-config.sh

quorum_shellprofiles_finalize

hadoop_add_param HADOOP_OPTS Dlog4j.configurationFile "-Dlog4j.configurationFile=@quorum.server.path@/etc/log4j2.yaml"

MYNAME=$(hadoop_abs "${BASH_SOURCE-$0}")

if [[ $# = 0 ]]; then
  hadoop_exit_with_usage 1
fi

HADOOP_SUBCMD=$1
shift

if hadoop_need_reexec "${HADOOP_SHELL_EXECNAME}" "${HADOOP_SUBCMD}"; then
  hadoop_uservar_su "${HADOOP_SHELL_EXECNAME}" "${HADOOP_SUBCMD}" \
    "${MYNAME}" \
    "--reexec" \
    "${HADOOP_USER_PARAMS[@]}"
  exit $?
fi

hadoop_verify_user_perm "${HADOOP_SHELL_EXECNAME}" "${HADOOP_SUBCMD}"

HADOOP_SUBCMD_ARGS=("$@")

if declare -f quorum_command_"${HADOOP_SUBCMD}" >/dev/null 2>&1; then
  hadoop_debug "Calling dynamically: quorum_command_${HADOOP_SUBCMD} ${HADOOP_SUBCMD_ARGS[*]}"
  "quorum_command_${HADOOP_SUBCMD}" "${HADOOP_SUBCMD_ARGS[@]}"
else
  quorumcmd_case "${HADOOP_SUBCMD}" "${HADOOP_SUBCMD_ARGS[@]}"
fi

hadoop_add_client_opts

hadoop_subcommand_opts "${HADOOP_SHELL_EXECNAME}" "${HADOOP_SUBCMD}"

# everything is in globals at this point, so call the generic handler
hadoop_generic_java_subcmd_handler
