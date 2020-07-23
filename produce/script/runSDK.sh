#!/bin/bash

basedir=`cd $(dirname $0); pwd -P`


function logi(){
	echo "##############################################################################"
	echo "##"
	tempLogiArr=$*
	for item in ${tempLogiArr[@]}
    do
        echo "##   "${item}
    done
	echo "##"
	echo "##############################################################################"
}

function die(){
	RED='\033[1;31m'
	NC='\033[0m' # No Color
	echo "${RED}##############################################################################"
	echo "##"
	echo "##   "$*
	echo "##"
	echo "##############################################################################${NC}"
    exit 1
}

function checkResultCode(){
	return_code=$?
	if [[ $return_code != 0 ]]; then
		die "$*       code=$return_code"
	fi
}

############################################################################################################
## 合成代码到单个工程，并生成 AAR，最后打包测试Demo
############################################################################################################


start_time=$(date +%s)

logi "merge module: "${MergeModuleName[@]}

# 调用 java，执行合成代码功能
java -jar $basedir/MergeModule_jar/MergeModule.jar $OriginProject "$GeneProject/main" "exo" ${MergeModuleName[*]}
checkResultCode "java MergeModule"

cd $GeneProject

sh gradlew clean
checkResultCode "gradlew clean"

sh gradlew assembleRelease
checkResultCode "gradlew assembleRelease"

# sh gradlew :produce:assembleSdk_dushulangRelease

logi 'END... time:'$(($(date +%s)-start_time))"s"


