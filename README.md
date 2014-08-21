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

Running the examples
====================

To run the rabbitmq example

First start a RabbitMQ server on the local machine

After starting the master and site servers run the following command

./bin/iotcloud jar repository/sensors/iotcloud-examples-1.0-SNAPSHOT.jar cgl.iotcloud.examples.chat.RabbitMQSensor

IoTCloud Architecture - Sensor Discovery and Scalability
========================================================

Sensor Discovery
----------------

The sensor information is registered into the ZooKeeper. The information saved in the ZooKeeper is used by the Storm topology to discover the sensors. 

Sensor
------

Sensor has a name and set of communication channels. When a sensor is deployed, the running instance gets an instance id. This instance id is used for controlling the sensor after the deployment. Same sensor can be deployed multiple times and each of the instances will get an unique id. A communication channel connects the data to the publish subscribe messaging brokers. A sensor can have multiple such channels and each of these channels within a sensor has a unique name. 

When a sensor is deployed, its information is saved in ZooKeeper. The default structure of sensor information in ZooKeeper is 

/iot/sensors/[sensor_name]/[sensor_instance_id]/[channel_name]

The ZNode with the sensor instance id contains the information about the sensor like its status, metadata etc. The ZNodes with channel names contains the information about the channels. 

Messaging layer 
---------------

A communication channel belonging to a sensor can be a grouped channel or an individual channel. When a channel is grouped, a single queue is created for all such channels declared across the gateways by the sensors. If a channel is not grouped we create a unique queue for that channel. 

grouped channel queue name: gateway_id.sensor_name.queue_name
single_channel_queue_name: gateway_id.sensor_name.sensor_id.queue_name 

As the names suggests, we can only group channels belonging to the sensors of the same gateway. Also we can only group channels belonging to the same sensor.

Message Dispatching
-------------------

Every instance of a sensor deployed has a unique sensor id. This sensor id is used for dispatching the messages to correct processing bolts, queues and sensors. 

Sensor Discovery
When a Storm topology is created we can specify the information about the sensors to create the communication links. We use a yaml file to specify the spout and bolt configurations for a topology. Here is an example yaml file.

zk.servers: ["localhost:2181"]
zk.root: "/iot/sensors"
spouts:
    sentence_receive:
        broker: "rabbitmq"
        sensor: "wordcount"
        channel: "sentence"
        fields: ["sentence", "sensorID", "time"]
        properties:
          ackMode: "auto"
bolts:
    count_send:
        broker: "rabbitmq"
        sensor: "wordcount"
        channel: "count"
        fields: ["count", "sensorID", "time"]
        properties:
          ackMode: "auto"

This configuration specifies the sensors, channels and the broker we are using to create the spouts and bolts. The created spouts and bolts will listen to ZooKeeper nodes related to their configurations and will dynamically discover the sensor channels. 



