#!/bin/bash

SCRIPT_DIR=`dirname $0`
PROJECT_HOME="${SCRIPT_DIR}/.."

TARGET_DIR="${PROJECT_HOME}/target"
LIB_DIR="${PROJECT_HOME}/lib"

if [[ -z "$JAVA_HOME" ]]; then
	export JAVA_HOME=/opt/java
fi

if [[ -z "$GROOVY_HOME" ]]; then
	GROOVY_HOME=/opt/groovy
fi

APP_CLASSPATH="."

if [[ -d $TARGET_DIR ]]; then
    for file in `/bin/ls ${TARGET_DIR}/*.jar`
    do
        APP_CLASSPATH=$APP_CLASSPATH:${file}
    done
fi


if [[ -d $LIB_DIR ]]; then
    for file in `/bin/ls ${LIB_DIR}/*.jar`
    do
        APP_CLASSPATH=$APP_CLASSPATH:${file}
    done
fi

export CLASSPATH=$APP_CLASSPATH

export JAVA_OPTS="-Duser.timezone=UTC -Djava.awt.headless=true"

#echo "APP_CLASSPATH: $APP_CLASSPATH"

$GROOVY_HOME/bin/groovy -cp $APP_CLASSPATH $1 "$@"
