package cgl.iotcloud.core.cmd;

import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.client.SensorClient;
import org.apache.commons.cli.*;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
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

    public void ls() {
        List<TSensor> sensors = client.getSensors();
        formatSensors(sensors);
    }

    public void ls(String site) {
        List<TSensor> sensors = client.getSensors(site);
        formatSensors(sensors);
    }

    private void formatSensors(List<TSensor> sensors) {
        System.out.format("----------------------------------------------------------------------------------%n");
        System.out.format("| Sensor Name                            | Sensor ID                             |%n");
        System.out.format("----------------------------------------------------------------------------------%n");
        for (TSensor sensor : sensors) {
            System.out.format(" %-40s| %-40s%n", sensor.getName(), sensor.getSensorId());
        }
        System.out.print("\n");
    }

    public void startSensor(String name) {
        client.startSensor(name);
    }

    public static void main(String[] args) {
        // get the first argument
        if (args.length == 0) {
            System.out.println("You should specify at least one argument");
            System.exit(1);
        }
        CommandClient client = new CommandClient();

        String command = args[0];
        String restArgs[] = new String[args.length - 1];
        System.arraycopy(args, 1, restArgs, 0, args.length - 1);

        if (command.equals("kill")) {
            Options options = new Options();
            options.addOption("id", true, "ID of sensor");


            CommandLineParser commandLineParser = new BasicParser();
            try {
                CommandLine cmd = commandLineParser.parse(options, restArgs);
                String name = cmd.getOptionValue("id");
                client.killSensor(name);
            } catch (ParseException e) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cmd", options);
            }
        } else if (command.equals("ls")) {
            client.ls();
        }
    }
}
