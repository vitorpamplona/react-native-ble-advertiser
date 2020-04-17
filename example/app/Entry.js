import React, {Component } from 'react';

import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  Button,
  StatusBar,
  TouchableOpacity,
  FlatList,
} from 'react-native';

import Moment from 'moment';

import { Alert, Platform } from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';
import BLEAdvertiser from 'react-native-ble-advertiser'
import update from 'immutability-helper';

import {
  Header,
  Colors
} from 'react-native/Libraries/NewAppScreen';

import UUIDGenerator from 'react-native-uuid-generator';
import { PermissionsAndroid } from 'react-native';

export async function requestLocationPermission() {
  try {
    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          'title': 'BLE Avertiser Example App',
          'message': 'Example App access to your location '
        }
      )
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log("You can use the location")
      } else {
        console.log("location permission denied")
      }
    }

    const blueoothActive = await BLEAdvertiser.getAdapterState().then(result => {
      console.log('[Bluetooth]', "isBTActive", result)
      return result === "STATE_ON";
    }).catch(error => { 
      console.log('[Bluetooth]', "BT Not Enabled")
      return false;
    });

    if (!blueoothActive) {
      await Alert.alert(
        'Private Kit requires bluetooth to be enabled',
        'Would you like to enable Bluetooth?',
        [
          {
            text: 'Yes',
            onPress: () => BLEAdvertiser.enableAdapter(),
          },
          {
            text: 'No',
            onPress: () => console.log('No Pressed'),
            style: 'cancel',
          },
        ],
      )
    }

    console.log("BT Active?", blueoothActive);
  } catch (err) {
    console.warn(err)
  }
}

class Entry extends Component {
    constructor(props) {
        super(props);
        this.state = {
            uuid:'',
            devicesFound:[]
        }
    }

    isValidUUID(uuid) {
      if (!uuid)return false;
      return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(uuid);
    }

    addDevice(_uuid, _name, _rssi, _date) {
      let index = -1;
      for(let i=0; i< this.state.devicesFound.length; i++){
        if (this.state.devicesFound[i].uuid == _uuid) {
          index = i;
        }
      }
      if (index<0) {
        let dev = {uuid:_uuid, name:_name, rssi:_rssi, start:_date, end:_date};
        this.setState({
          devicesFound: update(this.state.devicesFound, 
            {$push: [dev]}
          )
        });
      } else {
        //let dev = this.state.devicesFound[index];
        //const newList = this.state.devicesFound.splice(index, 1);
        const itemIndex = index;
        this.setState({
          devicesFound: update(this.state.devicesFound, 
            {[itemIndex]: {end: {$set: _date}, rssi: {$set: _rssi || this.state.devicesFound[itemIndex].rssi }}}
          )
        });
      }
    }

    componentDidMount(){
      requestLocationPermission();
      
      console.log("BLE Advertiser", BLEAdvertiser);
      BLEAdvertiser.setCompanyId(0x4C); 
    
      UUIDGenerator.getRandomUUID((newUid) => {
        this.setState({
          uuid: newUid
        });
      });

      const eventEmitter = Platform.select({
        ios: new NativeEventEmitter(NativeModules.BLEAdvertiser),
        android: new NativeEventEmitter(NativeModules.BLEAdvertiser),
      });

      eventEmitter.addListener('onDeviceFound', (event) => {
        //console.log('onDeviceFound', event);
        if (event.serviceUuids) {
          for(let i=0; i< event.serviceUuids.length; i++){
            if (this.isValidUUID(event.serviceUuids[i]))
              this.addDevice(event.serviceUuids[i], event.deviceName, event.rssi, new Date())   
          }
        }
      });
    }

    start() {
      console.log(this.state.uuid, "Starting Advertising");
      BLEAdvertiser.broadcast(this.state.uuid, [12,23,56], {
        advertiseMode: BLEAdvertiser.ADVERTISE_MODE_LOW_POWER, 
        txPowerLevel: BLEAdvertiser.ADVERTISE_TX_POWER_ULTRA_LOW, 
        connectable: false, 
        includeDeviceName: false, includeTxPowerLevel: false
      })
      .then((sucess) => {
        console.log(this.state.uuid, "Adv Successful", sucess);
      }).catch(error => {
        console.log(this.state.uuid, "Adv Error", error); 
      });
      
      console.log(this.state.uuid, "Starting Scanner");
      BLEAdvertiser.scan([12,23,56], {scanMode: BLEAdvertiser.SCAN_MODE_LOW_POWER})
      .then((sucess) => {
        console.log(this.state.uuid, "Scan Successful", sucess);
      }).catch(error => {
        console.log(this.state.uuid, "Scan Error", error); 
      });

      this.setState({
        isLogging: true,
      });
    }

    stop(){
      console.log(this.state.uuid, "Stopping Broadcast");
      BLEAdvertiser.stopBroadcast()
        .then((sucess) => {
          console.log(this.state.uuid, "Stop Broadcast Successful", sucess);
        }).catch(error => {
          console.log(this.state.uuid, "Stop Broadcast Error", error); 
        });

      console.log(this.state.uuid, "Stopping Scanning");
      BLEAdvertiser.stopScan()
        .then((sucess) => {
          console.log(this.state.uuid, "Stop Scan Successful", sucess);
        }).catch(error => {
          console.log(this.state.uuid, "Stop Scan Error", error); 
        });

      this.setState({
        isLogging: false,
      });
    }

    onClearArray = () => {
      this.setState({ devicesFound: [] });
    };

    short(str) {
      return str.substring(0, 4) + " ... " + str.substring(str.length-4, str.length); 
    }

    dateDiffSecs(start, end) {
      return Math.floor((end.getTime() - start.getTime())/1000);
    }

    dateStr(dt) {
      return Moment(dt).format('H:mm');
    }

    render() {
      return (
        <SafeAreaView>
          <View style={styles.body}>
            <View style={styles.sectionContainer}>
              <Text style={styles.sectionTitle}>BLE Advertiser Demo</Text>
              <Text style={styles.sectionDescription}>Broadcasting: <Text style={styles.highlight}>{ this.short(this.state.uuid) }</Text></Text>
            </View>

            <View style={styles.sectionContainer}>
              {this.state.isLogging ? (
              <TouchableOpacity
                onPress={() => this.stop()}
                style={styles.stopLoggingButtonTouchable}>
                <Text style={styles.stopLoggingButtonText}>
                  Stop
                </Text>
              </TouchableOpacity>
                ) : (
              <TouchableOpacity
                onPress={() => this.start()}
                style={styles.startLoggingButtonTouchable}>
                <Text style={styles.startLoggingButtonText}>
                  Start
                </Text>
              </TouchableOpacity>
              )}
            </View>

            <View style={styles.sectionContainerFlex}>
              <Text style={styles.sectionTitle}>Devices Around</Text>
              <FlatList
                  data={ this.state.devicesFound }
                  renderItem={({item}) => <Text style={styles.itemPastConnections}>{this.dateStr(item.start)} ({this.dateDiffSecs(item.start, item.end)}s): {this.short(item.uuid)} {item.rssi} {item.name}</Text>}
                  keyExtractor={item => item.uuid}
                  />
            </View>

            <View style={styles.sectionContainer}>
              <TouchableOpacity
                onPress={this.onClearArray}
                style={styles.startLoggingButtonTouchable}>
                <Text style={styles.startLoggingButtonText}>
                  Clear Devices
                </Text>
              </TouchableOpacity>
            </View>

          </View>
        </SafeAreaView>
      );
    }
}

const styles = StyleSheet.create({
  body: {
    backgroundColor: Colors.white,
    height: "100%",
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
    color: Colors.black,
    textAlign: 'center'
  },
  sectionDescription: {
    fontSize: 18,
    fontWeight: '400',
    textAlign: 'center',
    color: Colors.dark,
  },
  highlight: {
    fontWeight: '700',
  },
  footer: {
    color: Colors.dark,
    fontSize: 12,
    fontWeight: '600',
    padding: 4,
    paddingRight: 12,
    textAlign: 'right',
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
      width: "80%",
      height: 200
  },
  itemPastConnections: {
      padding: 3,
      fontSize: 18,
      fontWeight: '400',
  },
});

export default Entry;
