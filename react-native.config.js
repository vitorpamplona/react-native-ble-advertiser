module.exports = {
  dependencies: {
    'react-native-ble-advertiser': {
        platforms: {
            android: {
                "packageImportPath": "import com.vitorpamplona.bleadvertiser;",
                "packageInstance": "new BLEAdvertiserPackage()"
            }
        }
    }
  }
};