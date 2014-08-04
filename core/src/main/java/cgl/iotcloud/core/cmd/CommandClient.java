package cgl.iotcloud.core.cmd;

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

    public void killSensor(String name) {
        client.unDeploySensor(name);
    }

    public void startSensor(String name) {
        client.startSensor(name);
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("kill", false, "Stop a sensor");
        options.addOption("name", true, "Name of sensor");

        CommandClient client = new CommandClient();
        CommandLineParser commandLineParser = new BasicParser();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            if (cmd.hasOption("kill")) {
                String name = cmd.getOptionValue("name");

                client.killSensor(name);
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("cmd", options );
        }
    }
}
