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

hadoop_add_profile analytic-annotations

function _analytic-annotations_hadoop_classpath
{
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.spotbugs-annotations@" after
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.jsr305@" after
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.audience-annotations@" after
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.error_prone_annotations@" after
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.j2objc_annotations@" after
    hadoop_add_classpath "@analytic-annotations.path@/lib/@dependencies.jspecify@" after
}
