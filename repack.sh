npm pack
cd example
npm i ../react-native-ble-advertiser-0.0.10.tgz

if [ $1 = "android" ]
then
    npx react-native run-android
else
    cd ios
    pod install
    cd ..
    npx react-native run-ios
fi

cd ..