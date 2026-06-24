#!/bin/bash -eu
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

# build project
mvn clean install -DskipTests -Dmaven.javadoc.skip=true

# build fuzzers
cd fuzzers && \
    mvn clean package -DskipTests -Dmaven.javadoc.skip=true \
        dependency:build-classpath -Dmdep.outputFile=target/build-classpath.txt

cp target/classes/* $OUT/

class_path=`cat target/build-classpath.txt`

for fuzzer in src/main/java/*Fuzzer.java
do
    base_name=$(basename -s .java $fuzzer)

    echo "Createing fuzz script for $base_name"

    cat > "$OUT/$base_name" << EOF
#!/bin/sh
this_dir=\$(dirname "\$0")
\$this_dir/jazzer_driver --agent_path=\$this_dir/jazzer_agent_deploy.jar \
    --cp="\$this_dir:$class_path" \
    --target_class=$base_name \
    --jvm_args="-Xmx2048m" \
    \$@
EOF

    chmod +x "$OUT/$base_name"
done
