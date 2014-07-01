IoTCloud is a distributed software to bring Sensor Data to a cloud environment for processing.

How to install
==============

Go to the source directory and use maven to build the project

mvn clean install

Then go to the distribution directory and type

mvn clean install

This will build the zip distribution in the target directory. Unzip the zip file and you are ready to run IOTCloud.

How to Run
==========

There are two services we need to run in-order to bring IOTCloud

1. Master
2. Site

To start the site go to the iotcloud distribution directory and type

./bin/iotcloud master

Then open a new terminal, go to iotcloud distribution directory and type the following to start a site.

./bin/iotcloud site

How to deploy a sensor
======================

The sensor has to be built as a jar including all the dependencies. You need to copy the sensor jar in to

<iotcloud_home>/repository/sensors

then use the command

./bin/iotcloud jar {jar_file_location} {main_class_for_sensor} {arguments_for_the sensor}