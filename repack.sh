#! /bin/sh 
npm ci

PACKAGE_VERSION=$(cat package.json \
  | grep version \
  | head -1 \
  | awk -F: '{ print $2 }' \
  | sed 's/[",]//g' \
  | tr -d '[[:space:]]')

NPMFILE="../react-native-ble-advertiser-$PACKAGE_VERSION.tgz"

echo Packing $PACKAGE_VERSION into $NPMFILE

npm pack

echo Installing new Lib on Example app

cd example
npm i $NPMFILE
cd ios
pod install
cd ..

if [ -n "$1" ]  
then
    echo Running on "$1"
    case "$1" in
        "android") npx react-native run-android
        ;;
        "ios") npx react-native run-ios --device "$2"
        ;;
    esac
fi

cd ..
