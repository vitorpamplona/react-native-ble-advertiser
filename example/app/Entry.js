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

import AndroidBLEAdvertiserModule from 'react-native-ble-advertiser'
import { NativeEventEmitter, NativeModules } from 'react-native';

import {
  Header,
  Colors
} from 'react-native/Libraries/NewAppScreen';

import UUIDGenerator from 'react-native-uuid-generator';
import { PermissionsAndroid } from 'react-native';

export async function requestLocationPermission() {
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        'title': 'Example App',
        'message': 'Example App access to your location '
      }
    )
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      console.log("You can use the location")
    } else {
      console.log("location permission denied")
    }
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

    componentDidMount(){
      requestLocationPermission();
      
      AndroidBLEAdvertiserModule.setCompanyId(0xFF); // PrivateKit
      
      UUIDGenerator.getRandomUUID((newUid) => {
        this.setState({
          uuid: newUid
        });
      });

      const eventEmitter = new NativeEventEmitter(NativeModules.AndroidBLEAdvertiserModule);
      eventEmitter.addListener('onDeviceFound', (event) => {
        const currentDevs = [...this.state.devicesFound];
        for(let i=0; i< event.serviceUuids.length; i++){
            if(currentDevs.indexOf(event.serviceUuids[i]) === -1) { // notice that there is a parenthesis after `id`.
               currentDevs.push(event.serviceUuids[i]);
            }
        }
        this.setState({
            devicesFound: currentDevs
        });
        console.log(event) // "someValue"
      });
    }

    start() {
      console.log(this.state.uuid, "Starting Advertising");
      AndroidBLEAdvertiserModule.broadcast(this.state.uuid, [12,23,56])
      .then((sucess) => {
        console.log(this.state.uuid, "Adv Sucessful", sucess);
      }).catch(error => {
        console.log(this.state.uuid, "Adv Error", error); 
      });
      
      console.log(this.state.uuid, "Starting Scanner");
      AndroidBLEAdvertiserModule.scan([12,23,56], {})
      .then((sucess) => {
        console.log(this.state.uuid, "Scan Sucessful", sucess);
      }).catch(error => {
        console.log(this.state.uuid, "Scan Error", error); 
      });

      this.setState({
        isLogging: true,
      });
    }

    stop(){
      console.log(this.state.uuid, "Stopping Broadcast");
      AndroidBLEAdvertiserModule.stopBroadcast()
        .then((sucess) => {
          console.log(this.state.uuid, "Stop Scan Sucessful For", sucess);
        }).catch(error => {
          console.log(this.state.uuid, "Stop Scan Error for", error); 
        });

      this.setState({
        isLogging: false,
      });

      console.log(this.state.uuid, "Stopping Scanning");
      AndroidBLEAdvertiserModule.stopScan()
        .then((sucess) => {
          console.log(this.state.uuid, "Stop Scan Sucessful For", sucess);
        }).catch(error => {
          console.log(this.state.uuid, "Stop Scan Error for", error); 
        });

      this.setState({
        isLogging: false,
      });
    }

    onClearArray = () => {
      this.setState({ devicesFound: [] });
    };

    render() {
      return (
          <>
            <StatusBar barStyle="dark-content" />
            <SafeAreaView>
              <ScrollView
                contentInsetAdjustmentBehavior="automatic"
                style={styles.scrollView}>
                <View style={styles.body}>
                  <View style={styles.sectionContainer}>
                    <Text style={styles.sectionTitle}>Broadcasting Demo</Text>
                    <Text style={styles.sectionDescription}>App is Broadcasting</Text>
                    <Text style={styles.sectionDescription}><Text style={styles.highlight}>{ this.state.uuid }</Text></Text>
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
                  <View style={styles.sectionContainer}>
                    <Text style={styles.sectionTitle}>Devices Found</Text>
                    <FlatList
                        data={ this.state.devicesFound }
                        renderItem={({item}) => <Text style={styles.itemPastConnections}>{item}</Text>}
                        />
                  </View>

                   <TouchableOpacity
                      onPress={this.onClearArray}
                      style={styles.startLoggingButtonTouchable}>
                      <Text style={styles.startLoggingButtonText}>
                        Clear Devices
                      </Text>
                    </TouchableOpacity>
                </View>
              </ScrollView>
            </SafeAreaView>
          </>
        );
    }
}

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  engine: {
    position: 'absolute',
    right: 0,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
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
    fontFamily: 'OpenSans-Bold',
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
    fontFamily: 'OpenSans-Bold',
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
      padding: 3
  },
});

export default Entry;
