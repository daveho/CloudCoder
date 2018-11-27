// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.progsnap2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.dataanalysis.Util;

public class Export {
    private Properties config;
    private MainTableWriter mainTableWriter;

    public Export() {
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public void setMainTableWriter(MainTableWriter mainTableWriter) {
        this.mainTableWriter = mainTableWriter;
    }

    public void execute() throws IOException {
        Util.connectToDatabase(config);
        IDatabase db = Database.getInstance();

        // Dummy writes
        mainTableWriter.writeEvent(new Event(EventType.SessionStart, 0, 0, 0, TOOL_INSTANCES));
        mainTableWriter.writeEvent(new Event(EventType.SessionEnd, 0, 0, 0, TOOL_INSTANCES));
    }

    // Mostly a copy-paste of the original exporter's main() method
    public static void main(String[] args) throws IOException {
        Export exporter = new Export();

        @SuppressWarnings("resource")
        Scanner keyboard = new Scanner(System.in);

        boolean interactiveConfig = false;
        String specFile = null;
        Properties config = new Properties();

        for (String arg : args) {
            if (arg.equals("--interactiveConfig")) {
                // Configure interactively rather than using embedded cloudcoder.properties
                interactiveConfig = true;
            } else if (arg.startsWith("--spec=")) {
                // A "spec" file is properties defining what data should be pulled,
                // what the dataset metadata are, etc. It can also override
                // cloudcoder.properties configuration values, if desirned (e.g.,
                // database access credentials.) The idea is to allow repeatable
                // non-interactive exports.
                specFile = arg.substring("--spec=".length());
            } else if (arg.startsWith("-D")) {
                // Set an individual config property
                String keyVal = arg.substring("-D".length());
                int eq = keyVal.indexOf('=');
                if (eq < 0) {
                    throw new IllegalArgumentException("Invalid key/value pair: " + keyVal);
                }
                config.setProperty(keyVal.substring(0, eq), keyVal.substring(eq + 1));
            } else {
                throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }
        Util.configureLogging();
        if (interactiveConfig) {
            Util.readDatabaseProperties(keyboard, config);
        } else {
            try {
                Util.loadEmbeddedConfig(config, Export.class.getClassLoader());
            } catch (IllegalStateException e) {
                // Attempt to load from cloudcoder.properties in same directory
                Util.loadFileConfig(config, new File("cloudcoder.properties"));
                System.out.println("Read cloudcoder.properties in same directory");
            }
        }

        // If a specfile was specified, layer its properties on top of
        // whatever config properties we found.
        if (specFile != null) {
            Properties spec = new Properties();
            try (FileReader fr = new FileReader(specFile)) {
                spec.load(fr);
            }

            Properties effectiveSpec = new Properties();
            effectiveSpec.putAll(config);
            effectiveSpec.putAll(spec);

            config = effectiveSpec;
        }

        askIfMissing(config, "dest", "Progsnap2 output directory: ", keyboard);
        askIfMissing(config, "separationSeconds", "Session separation in seconds: ", keyboard);

        exporter.setConfig(config);

        File destDir = new File(config.getProperty("dest"));
        MainTableWriter mainTableWriter = new MainTableWriter(destDir);
        exporter.setMainTableWriter(mainTableWriter);

        // Do the export
        try {
            exporter.execute();
        } finally {
            IOUtils.closeQuietly(mainTableWriter);
        }
    }

    private static void askIfMissing(Properties config, String propName, String prompt, Scanner keyboard) {
        if (!config.containsKey(propName)) {
            config.setProperty(propName, Util.ask(keyboard, prompt));
        }
    }

    // TODO: Include version number and language student is using
    private static String[] TOOL_INSTANCES = { "CloudCoder" };
}