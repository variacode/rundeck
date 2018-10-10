#!/bin/bash
#/ trigger local ci test run


. rd_versions.sh


set -euo pipefail
IFS=$'\n\t'
readonly ARGS=("$@")
DOCKER_DIR=$PWD/test/docker

usage() {
      grep '^#/' <"$0" | cut -c4- # prints the #/ lines above as usage info
}
die(){
    echo >&2 "$@" ; exit 2
}

check_args(){
	if [ ${#ARGS[@]} -gt 0 ] ; then
    	DOCKER_DIR=$1
	fi
}
copy_jar(){
	local FARGS=("$@")
	local DIR=${FARGS[0]}
	local -a VERS=( $( rd_get_version ) )
	local JAR=rundeck-${VERS[0]}-${VERS[2]}.war
    local buildJar=$PWD/rundeckapp/build/libs/$JAR
	test -f $buildJar || die "Jar file not found $buildJar"
	mkdir -p $DIR
	cp $buildJar $DIR/rundeck-launcher.war
	echo $DIR/$JAR
}
run_tests(){
	local FARGS=("$@")
	local DIR=${FARGS[0]}

	cd $DIR
	bash $DIR/test-ssl.sh
}
run_docker_test(){
	local FARGS=("$@")
	local DIR=${FARGS[0]}
	local launcherJar=$( copy_jar $DIR ) || die "Failed to copy jar"
	run_tests $DIR
}


main() {
    check_args
    run_docker_test  $DOCKER_DIR
}
main

