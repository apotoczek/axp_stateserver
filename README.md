# State Server

a simple http server in Java to determine US State location of supplied lat/lng input

state geometries supplied in states.json (simplified for example purposes)

map of simplified states: [tondogroup.com/gmap.php](http://tondogroup.com/gmap.php) (or see screenshot below)

```
/**
* The StateServer HTTPServer calculates which US state(s)
* contain the supplied longitude/latitude point input.
* Simplified state boundaries are supplied in states.json
* and sometimes overlap due to the simplified geometry.
* (some states are missing altogether)
*
* Example map of states.json geometries:
* http://tondogroup.com/gmap.php
*
* Tested on:
* $ java -version
* java version "1.8.0_31"
* Java(TM) SE Runtime Environment (build 1.8.0_31-b13)
* Java HotSpot(TM) 64-Bit Server VM (build 25.31-b07, mixed mode)
*
* Compile:
* $ javac -cp .:\* StateServer.java
*
* Run:
* $ java -cp .:\* StateServer
*
* Example input/output (single state):
* $ curl -d "longitude=-78.953149&latitude=39.137085" http://localhost:8081
* [West Virginia]
*
* Example input/output (example of overlap):
* $ curl -d "longitude=-78.353149&latitude=39.437085" http://localhost:8081
* [West Virginia, Maryland, Virginia]
*/
```

![](http://tondogroup.com/gmap.png)