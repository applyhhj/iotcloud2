package cgl.iotcloud.core.cmd;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.client.SensorClient;
import org.apache.commons.cli.*;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;

public class CommandClient {
    // read the configuration file
    private Map conf = Utils.readConfig();

    private SensorClient client;

    public CommandClient() {
        try {
            client = new SensorClient(conf);
        } catch (TTransportException e) {
            System.out.println("Error occurred while trying to connect to sensor master");
        }
    }

    public void killSensor(String name, String group) {
        client.stopSensor(new SensorId(name, group));
    }

    public void startSensor(String name, String group) {
        client.startSensor(new SensorId(name, group));
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("kill", false, "Stop a sensor");
        options.addOption("name", true, "IP");
        options.addOption("group", true, "Receive Queue name");

        CommandClient client = new CommandClient();
        CommandLineParser commandLineParser = new BasicParser();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            if (cmd.hasOption("kill")) {
                String name = cmd.getOptionValue("name");
                String group = cmd.getOptionValue("group");

                client.killSensor(name, group);
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("cmd", options );
        }
    }
}
