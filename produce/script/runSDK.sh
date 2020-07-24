#!/bin/bash

basedir=`cd $(dirname $0); pwd -P`

OriginProject=${basedir%produce/script*}
PACKAGE_NAME="com.ruiray.sdk"
RES_PREFIX="rrs_"

# 项目相关库
MergeModuleName=("moduleA" "moduleB")
MergeModuleName[${#MergeModuleName[@]}]="moduleC"
MergeModuleName[${#MergeModuleName[@]}]="path>/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR/moduleD"


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

logi "config: " $OriginProject ${MergeModuleName[@]}

# 调用 java，执行合成代码功能
java -jar $basedir/javaMerge.jar $OriginProject "$OriginProject/produce" $PACKAGE_NAME $RES_PREFIX "exo" ${MergeModuleName[*]}
checkResultCode "java MergeModule"

cd $OriginProject

sh gradlew clean
checkResultCode "gradlew clean"

sh gradlew :produce:assembleRelease
checkResultCode "gradlew assembleRelease"

logi 'END... time:'$(($(date +%s)-start_time))"s"


