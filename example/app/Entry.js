import React, {Component } from 'react';

import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
} from 'react-native';

import AndroidBLEAdvertiserModule from 'react-native-ble-advertiser'

import {
  Header,
  Colors
} from 'react-native/Libraries/NewAppScreen';


class Entry extends Component {
    constructor(props) {
        super(props);
        this.state = {
            uuid:'d4e45cb4-6b6d-4b9e-8b87-876d2a1729ee'
        }
    }

    componentDidMount(){
      console.log("Starting Advertising with", this.state.uuid);
      AndroidBLEAdvertiserModule.setCompanyId(0xE2);
      AndroidBLEAdvertiserModule.broadcastPacket(this.state.uuid, [1,2])
      .then((sucess) => {
          console.log("Sucessful", sucess);
      }).catch(error => console.log(error));
    }

    render() {
      return (
          <>
            <StatusBar barStyle="dark-content" />
            <SafeAreaView>
              <ScrollView
                contentInsetAdjustmentBehavior="automatic"
                style={styles.scrollView}>
                <Header />
                {global.HermesInternal == null ? null : (
                  <View style={styles.engine}>
                    <Text style={styles.footer}>Engine: Hermes</Text>
                  </View>
                )}
                <View style={styles.body}>
                  <View style={styles.sectionContainer}>
                    <Text style={styles.sectionTitle}>Broadcasting Demo</Text>
                    <Text style={styles.sectionDescription}>
                      App is Broadcasting <Text style={styles.highlight}>{ this.state.uuid }</Text>
                    </Text>
                  </View>
                  
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
});

export default Entry;
