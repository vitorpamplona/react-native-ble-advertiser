import React, {Component} from 'react';

import {
  Alert,
  SafeAreaView,
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  FlatList,
  Platform,
} from 'react-native';

import {NativeEventEmitter, NativeModules} from 'react-native';

import update from 'immutability-helper';
import BLEAdvertiser from 'react-native-ble-advertiser';
import UUIDGenerator from 'react-native-uuid-generator';
import { requestMultiple, checkMultiple, PERMISSIONS, RESULTS } from 'react-native-permissions';
import {PermissionsAndroid} from 'react-native';

// Uses the Apple code to pick up iPhones
const APPLE_ID = 0x4c;
const MANUF_DATA = [1, 0];
// No scanner filters (finds all devices inc iPhone). Use UUID suffix to filter scans if using.
const SCAN_MANUF_DATA = Platform.OS === 'android' ? null : MANUF_DATA;
const UUID_SUFFIX = '00'
BLEAdvertiser.setCompanyId(APPLE_ID);

const requestPermissionsAndroid = () => {
  checkMultiple([PERMISSIONS.ANDROID.BLUETOOTH_ADVERTISE,
  PERMISSIONS.ANDROID.BLUETOOTH_CONNECT,
  PERMISSIONS.ANDROID.BLUETOOTH_SCAN,
  PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION]).then(res => {
    console.log('Advertise', res[PERMISSIONS.ANDROID.BLUETOOTH_ADVERTISE]);
    console.log('Connect', res[PERMISSIONS.ANDROID.BLUETOOTH_CONNECT]);
    console.log('Scan', res[PERMISSIONS.ANDROID.BLUETOOTH_SCAN]);
    console.log('Bluetooth Android 10', res[PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION]);
  })

  requestMultiple([PERMISSIONS.ANDROID.BLUETOOTH_ADVERTISE,
  PERMISSIONS.ANDROID.BLUETOOTH_CONNECT,
  PERMISSIONS.ANDROID.BLUETOOTH_SCAN,
  PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION]).then(res => {
    console.log('Advertise', res[PERMISSIONS.ANDROID.BLUETOOTH_ADVERTISE]);
    console.log('Connect', res[PERMISSIONS.ANDROID.BLUETOOTH_CONNECT]);
    console.log('Scan', res[PERMISSIONS.ANDROID.BLUETOOTH_SCAN]);
    console.log('Bluetooth Android 10', res[PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION]);
  })
}

export async function requestLocationPermission() {
  try {
    if (Platform.OS === 'android') {
      requestPermissionsAndroid();
    }

    const blueoothActive = await BLEAdvertiser.getAdapterState()
      .then((result) => {
        console.log('[Bluetooth]', 'Bluetooth Status', result);
        return result === 'STATE_ON';
      })
      .catch((error) => {
        console.log('[Bluetooth]', 'Bluetooth Not Enabled');
        return false;
      });

    if (!blueoothActive) {
      await Alert.alert(
        'Example requires bluetooth to be enabled',
        'Would you like to enable Bluetooth?',
        [
          {
            text: 'Yes',
            onPress: () => BLEAdvertiser.enableAdapter(),
          },
          {
            text: 'No',
            onPress: () => console.log('Do Not Enable Bluetooth Pressed'),
            style: 'cancel',
          },
        ],
      );
    }
  } catch (err) {
    console.warn(err);
  }
}

class Entry extends Component {
  constructor(props) {
    super(props);
    this.state = {
      uuid: '',
      isLogging: false,
      devicesFound: [],
    };
  }

  addDevice(_uuid, _name, _mac, _rssi, _date) {
    const index = this.state.devicesFound.findIndex(({uuid}) => uuid === _uuid);
    if (index < 0) {
      this.setState({
        devicesFound: update(this.state.devicesFound, {
          $push: [
            {
              uuid: _uuid,
              name: _name,
              mac: _mac,
              rssi: _rssi,
              start: _date,
              end: _date,
            },
          ],
        }),
      });
    } else {
      this.setState({
        devicesFound: update(this.state.devicesFound, {
          [index]: {
            end: {$set: _date},
            rssi: {$set: _rssi || this.state.devicesFound[index].rssi},
          },
        }),
      });
    }
  }

  componentDidMount() {
    requestLocationPermission();
    UUIDGenerator.getRandomUUID((newUid) => {
      this.setState({
        uuid: newUid.slice(0, -2) + UUID_SUFFIX,
      });
    });
  }

  componentWillUnmount() {
    if (this.state.isLogging) {
      this.stop();
    }
  }

  start() {
    console.log(this.state.uuid, 'Registering Listener');
    const eventEmitter = new NativeEventEmitter(NativeModules.BLEAdvertiser);

    this.onDeviceFound = eventEmitter.addListener('onDeviceFound', (event) => {
      //console.log('onDeviceFound', event);
      if (event.serviceUuids) {
        for (let i = 0; i < event.serviceUuids.length; i++) {
          if (event.serviceUuids[i] && event.serviceUuids[i].endsWith(UUID_SUFFIX)) {
            this.addDevice(
              event.serviceUuids[i],
              event.deviceName,
              event.deviceAddress,
              event.rssi,
              new Date(),
            );
          }
        }
      }
    });

    console.log(this.state.uuid, 'Starting Advertising');
    BLEAdvertiser.broadcast(this.state.uuid, MANUF_DATA, {
      advertiseMode: BLEAdvertiser.ADVERTISE_MODE_BALANCED,
      txPowerLevel: BLEAdvertiser.ADVERTISE_TX_POWER_MEDIUM,
      connectable: false,
      includeDeviceName: false,
      includeTxPowerLevel: false,
    })
      .then((sucess) => console.log(this.state.uuid, 'Adv Successful', sucess))
      .catch((error) => console.log(this.state.uuid, 'Adv Error', error));

    console.log(this.state.uuid, 'Starting Scanner');
    BLEAdvertiser.scan(SCAN_MANUF_DATA, {
      scanMode: BLEAdvertiser.SCAN_MODE_LOW_LATENCY,
    })
      .then((sucess) => console.log(this.state.uuid, 'Scan Successful', sucess))
      .catch((error) => console.log(this.state.uuid, 'Scan Error', error));

    this.setState({
      isLogging: true,
    });
  }

  stop() {
    console.log(this.state.uuid, 'Removing Listener');
    this.onDeviceFound.remove();
    delete this.onDeviceFound;

    console.log(this.state.uuid, 'Stopping Broadcast');
    BLEAdvertiser.stopBroadcast()
      .then((sucess) => console.log(this.state.uuid, 'Stop Broadcast Successful', sucess))
      .catch((error) => console.log(this.state.uuid, 'Stop Broadcast Error', error));

    console.log(this.state.uuid, 'Stopping Scanning');
    BLEAdvertiser.stopScan()
      .then((sucess) => console.log(this.state.uuid, 'Stop Scan Successful', sucess))
      .catch((error) => console.log(this.state.uuid, 'Stop Scan Error', error));

    this.setState({
      isLogging: false,
    });
  }

  short(str) {
    return (
      str.substring(0, 4) +
      ' ... ' +
      str.substring(str.length - 4, str.length)
    ).toUpperCase();
  }

  render() {
    return (
      <SafeAreaView>
        <View style={styles.body}>
          <View style={styles.sectionContainer}>
            <Text style={styles.sectionTitle}>BLE Advertiser Demo</Text>
            <Text style={styles.sectionDescription}>
              Broadcasting:{' '}
              <Text style={styles.highlight}>
                {this.short(this.state.uuid)}
              </Text>
            </Text>
          </View>

          <View style={styles.sectionContainer}>
            {this.state.isLogging ? (
              <TouchableOpacity
                onPress={() => this.stop()}
                style={styles.stopLoggingButtonTouchable}>
                <Text style={styles.stopLoggingButtonText}>Stop</Text>
              </TouchableOpacity>
            ) : (
              <TouchableOpacity
                onPress={() => this.start()}
                style={styles.startLoggingButtonTouchable}>
                <Text style={styles.startLoggingButtonText}>Start</Text>
              </TouchableOpacity>
            )}
          </View>

          <View style={styles.sectionContainerFlex}>
            <Text style={styles.sectionTitle}>Devices Around</Text>
            <FlatList
              data={this.state.devicesFound}
              renderItem={({item}) => (
                <Text style={styles.itemPastConnections}>
                  {this.short(item.uuid)} {item.mac} {item.rssi}
                </Text>
              )}
              keyExtractor={(item) => item.uuid}
            />
          </View>

          <View style={styles.sectionContainer}>
            <TouchableOpacity
              onPress={() => this.setState({devicesFound: []})}
              style={styles.startLoggingButtonTouchable}>
              <Text style={styles.startLoggingButtonText}>Clear Devices</Text>
            </TouchableOpacity>
          </View>
        </View>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  body: {
    height: '100%',
  },
  sectionContainerFlex: {
    flex: 1,
    marginTop: 12,
    marginBottom: 12,
    paddingHorizontal: 24,
  },
  sectionContainer: {
    flex: 0,
    marginTop: 12,
    marginBottom: 12,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    marginBottom: 8,
    fontWeight: '600',
    textAlign: 'center',
  },
  sectionDescription: {
    fontSize: 18,
    fontWeight: '400',
    textAlign: 'center',
  },
  highlight: {
    fontWeight: '700',
  },
  startLoggingButtonTouchable: {
    borderRadius: 12,
    backgroundColor: '#665eff',
    height: 52,
    alignSelf: 'center',
    width: 300,
    justifyContent: 'center',
  },
  startLoggingButtonText: {
    fontSize: 14,
    lineHeight: 19,
    letterSpacing: 0,
    textAlign: 'center',
    color: '#ffffff',
  },
  stopLoggingButtonTouchable: {
    borderRadius: 12,
    backgroundColor: '#fd4a4a',
    height: 52,
    alignSelf: 'center',
    width: 300,
    justifyContent: 'center',
  },
  stopLoggingButtonText: {
    fontSize: 14,
    lineHeight: 19,
    letterSpacing: 0,
    textAlign: 'center',
    color: '#ffffff',
  },
  listPastConnections: {
    width: '80%',
    height: 200,
  },
  itemPastConnections: {
    padding: 3,
    fontSize: 18,
    fontWeight: '400',
  },
});

export default Entry;
