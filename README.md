# OpenLDAT
OpenLDAT is a Free and Open Source solution to measure and analyze display latency metrics, similar to Nvidia LDAT.

This is a fork that is used under license but not affiliated with the original whose homepage is https://openldat.fdossena.com

# Usage
The wiki contains concrete, practical usage showcases.

>[!TIP]
>Please see the wiki: https://github.com/moults31/OpenLDAT/wiki


# Quickstart
## Software
This repo provides a docker image that bundles all dependencies for both the firmware and the app software.

### Starting a docker container
In your OpenLDAT clone:
```
$ cd Docker
$ docker compose run openldat
```

Expected output:
```
Success! Found /openldat
openldat-dev ●
```

### Firmware
This section assumes you are using the arduino firmware in this repo that targets the Elite C board with atmega32u4 MCU.

#### Build
```
openldat-dev ● Scripts/fw-build.sh
```

#### Flash
Quickly short RST to GND on the Elite C board two times within a ~1 second window using tweezers or similar to activate DFU mode. Then:

```
openldat-dev ● sudo -E ./Scripts/fw-flash.sh
```

Expect output:
```
Flash script invoking dfu-programmer for Elite C.
Note: This script REQUIRES sudo for accessing the device!
Validating...
8076 bytes used (28.17%)
```

### App Software
#### Build
```
openldat-dev ● Scripts/app-build.sh
```

Expect output:
```
BUILD SUCCESSFUL
Total time: 2 seconds
```

#### Run
```
openldat-dev ● Scripts/app-run.sh
```

Expect to see a GUI program similar to https://github.com/moults31/OpenLDAT/wiki#result-in-openldat-java-app

## Hardware
To build the physical OpenLDAT device, refer to the [Fritzing Model](Device/Hardware/OpenLDAT_Model1.fzz) provided by the original OpenLDAT creator.