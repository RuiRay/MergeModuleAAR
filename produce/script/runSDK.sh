#!/bin/bash

basedir=`cd $(dirname $0); pwd -P`

OriginProject=${basedir%/produce/script*}
PACKAGE_NAME="com.ruiray.sdk"
RES_PREFIX="rrs_"

# 项目相关库
MergeModuleName=("moduleA" "moduleB")
MergeModuleName[${#MergeModuleName[@]}]="moduleC"
# 使用绝对路径引入，加前缀 path>
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
cd ${basedir}
java -jar ./javaMerge.jar $OriginProject "$OriginProject/produce" $PACKAGE_NAME $RES_PREFIX "exo" ${MergeModuleName[*]}
checkResultCode "java MergeModule"

# 修复导入的包
java -jar ./javaMerge.jar "-fixPackage" "$OriginProject/produce" "import $PACKAGE_NAME.R;" "com.ruiray.mergea.ModuleA"
java -jar ./javaMerge.jar "-fixPackage" "$OriginProject/produce" "import $PACKAGE_NAME.BuildConfig;" "com.ruiray.mergea.ModuleA"
checkResultCode "java fix package"

cd $OriginProject

cp settings.gradle tempSettingsGradle
echo "include ':produce'" > settings.gradle

sh gradlew clean
checkResultCode "gradlew clean"

sh gradlew :produce:assembleRelease
checkResultCode "gradlew assembleRelease"

# reset
cd $OriginProject
mv tempSettingsGradle settings.gradle

# export file
mkdir -p $OriginProject/produce/product/

cd "./produce/build/outputs/aar/"
aarPath=$(ls . | grep '.aar' | sed -n '1p')
cp ${aarPath} $OriginProject/produce/product/

cd "$OriginProject/produce/build/outputs/mapping/release"
mappingPath=$(ls . | grep 'mapping' | sed -n '1p')
cp ${mappingPath} $OriginProject/produce/product/mapping_$(basename "${aarPath%.*}").txt

open $OriginProject/produce/product/

logi 'END... time:'$(($(date +%s)-start_time))"s"


