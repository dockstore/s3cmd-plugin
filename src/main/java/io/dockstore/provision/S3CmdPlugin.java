/*
 *    Copyright 2016 OICR
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.dockstore.provision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

import static io.dockstore.provision.S3CmdPluginHelper.getChunkSize;
import static io.dockstore.provision.S3CmdPluginHelper.nextLinesRequireCarriageReturn;

/**
 * @author gluu
 */
public class S3CmdPlugin extends Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(S3CmdPlugin.class);

    public S3CmdPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            System.out.println(StringUtils.upperCase("S3CmdPlugin development mode"));
        }
    }

    @Override
    public void stop() {
        System.out.println("S3CmdPlugin.stop()");
    }

    @Extension
    public static class S3CmdProvision implements ProvisionInterface {

        private static final String CLIENT_LOCATION = "client";
        private static final String CONFIG_FILE_LOCATION = "config-file-location";
        private static final String VERBOSITY = "verbosity";
        private static final String DEFAULT_CLIENT = "/usr/bin/s3cmd";
        private static final String DEFAULT_CONFIGURATION = System.getProperty("user.home") + "/.s3cfg";
        private static final String DEFAULT_VERBOSITY = "normal";
        private VerbosityEnum verbosity;
        private String client;
        private String configLocation;
        private Map<String, String> config;

        // Similar to https://github.com/qos-ch/slf4j/blob/0b1e6d38cfabd4b7ed335aec1aa6b2ae0c770f08/slf4j-simple/src/main/java/org/slf4j/simple/SimpleLoggerConfiguration.java#L145
        public void setVerbosity(String verbosity) {
            try {
                switch (verbosity.toLowerCase()) {
                case "minimal":
                    this.verbosity = VerbosityEnum.MINIMAL;
                    break;
                case "normal":
                    this.verbosity = VerbosityEnum.NORMAL;
                    break;
                default:
                    LOG.error("Unknown verbosity setting");
                    this.verbosity = VerbosityEnum.NORMAL;
                }
            } catch (NumberFormatException e) {
                this.verbosity = VerbosityEnum.NORMAL;
            }
        }

        void setClient(String client) {
            this.client = client;
        }

        void setConfigLocation(String configLocation) {
            this.configLocation = configLocation;
        }

        public void setConfiguration(Map<String, String> map) {
            this.config = map;
        }

        public Set<String> schemesHandled() {
            return new HashSet<>(Lists.newArrayList("s3cmd"));
        }

        /**
         * Downloads the file from the remote source path and places at the local destination
         *
         * @param sourcePath  The scheme for s3cmd (ex. s3cmd://bucket/dir/object)
         * @param destination The destination where the file is supposed to be (includes filename)
         * @return Whether download was successful or not
         */
        public boolean downloadFrom(String sourcePath, Path destination) {
            setConfigAndClient();
            // ambiguous how to reference s3cmd files, rip off these kinds of headers
            sourcePath = sourcePath.replaceFirst("s3cmd", "s3");
            String command = client + " -c " + configLocation + " get " + sourcePath + " " + destination + " --force";
            int exitCode = executeConsoleCommand(command, true);
            return checkExitCode(exitCode);
        }

        // This function checks the exit code and decides what to return
        // See https://github.com/s3tools/s3cmd/blob/master/S3/ExitCodes.py for exit code description
        private boolean checkExitCode(int exitCode) {
            switch (exitCode) {
            case 0: {
                return true;
            }
            case 65:
            case 71:
            case 74:
            case 75: {
                return false;
            }
            default: {
                throw new RuntimeException("Process exited with exit code" + exitCode);
            }
            }
        }

        /**
         * This sets the s3cmd client and s3 config file based on the dockstore config file and defaults
         */
        private void setConfigAndClient() {
            if (config == null) {
                LOG.error("You are missing a dockstore config file");
            } else {
                setConfigLocation(config.getOrDefault(CONFIG_FILE_LOCATION, DEFAULT_CONFIGURATION));
                setClient(config.getOrDefault(CLIENT_LOCATION, DEFAULT_CLIENT));
                setVerbosity(config.getOrDefault(VERBOSITY, DEFAULT_VERBOSITY));
            }
        }

        /**
         * Uploads the local source file and places at the remote destination
         *
         * @param destPath   The remote destination (ex. s3cmd://bucket/dir/object)
         * @param sourceFile The local source file (ex. file.txt)
         * @param metadata   Metadata: currently not used
         * @return Returns true on successful upload, false otherwise
         */
        public boolean uploadTo(String destPath, Path sourceFile, Optional<String> metadata) {
            setConfigAndClient();
            long sizeInBytes;
            try {
                sizeInBytes = Files.size(sourceFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String recursive = "";

            // If the destination end with a slash, source could be either a file or a directory
            // It can technically be either, but we're recursively putting the source file as if it's a directory because there's no side effect
            // If the destination does not end with a slash, source must be a file
            if (destPath.endsWith("/")) {
                recursive = "-r ";
            }

            String modifiedChunkSize = getChunkSize(sizeInBytes);
            destPath = destPath.replace("s3cmd://", "s3://");
            String trimmedPath = destPath.replace("s3://", "");
            List<String> splitPathList = Lists.newArrayList(trimmedPath.split("/"));
            String bucketName = splitPathList.remove(0);
            String fullBucketName = "s3://" + bucketName;
            if (checkBucket(fullBucketName)) {
                LOG.info("Bucket exists");
            } else {
                if (!createBucket(fullBucketName)) {
                    // New s3cmd apparently returns an error if bucket exists
                    LOG.info("Could not create bucket");
                }
            }
            String command = client + " -c " + configLocation + " put " + recursive + sourceFile.toString().replace(" ", "%32") + " " + destPath
                    + modifiedChunkSize;
            int exitCode = executeConsoleCommand(command, true);
            return checkExitCode(exitCode);
        }

        /**
         * Check if the bucket exists
         *
         * @param bucket The actual bucket name (ex. s3://bucket)
         * @return True if bucket exists, false if bucket doesn't exist
         */
        private boolean checkBucket(String bucket) {
            String command = client + " -c " + configLocation + " info " + bucket;
            int exitCode = executeConsoleCommand(command, true);
            return exitCode == 0;
        }

        /**
         * Creates the bucket
         *
         * @param bucket The name of the bucket that needs to be created
         * @return True if bucket successfully created, false if it wasn't successfully created
         */
        private boolean createBucket(String bucket) {
            String command = client + " -c " + configLocation + " mb " + bucket;
            int exitCode = executeConsoleCommand(command, verbosity.getLevel() >= VerbosityEnum.NORMAL.getLevel());
            return exitCode == 0;
        }

        /**
         * Executes the string command given
         *
         * @param command The command to execute
         * @return True if command was successfully execute without error, false otherwise.
         */
        private int executeConsoleCommand(String command, boolean printStdout) {
            // Show command in dockstore --debug mode
            LOG.debug("Executing command: " + command);
            String[] split = command.split(" ");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].replace("%32", " ");
            }
            ProcessBuilder builder = new ProcessBuilder(split);
            builder.redirectErrorStream(true);
            final Process p;
            try {
                p = builder.start();
                final Thread ioThread = new Thread(() -> {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        boolean carriage = false;
                        while ((line = reader.readLine()) != null) {
                            if (printStdout) {
                                // The line prior to the carriage return will start with "download" or "upload", must isolate from others
                                if (carriage == false) {
                                    // 's3cmd info' will only display in dockstore --debug mode
                                    if (command.contains("info")) {
                                        LOG.debug(line);
                                    } else {
                                        System.out.println(line);
                                    }
                                    carriage = nextLinesRequireCarriageReturn(line);
                                } else {
                                    System.out.print("\r" + line);
                                }
                            }
                        }
                        // For some reason file provisioning download does not output a newline, but upload does
                        if (command.contains("get")) {
                            System.out.println();
                        }
                        reader.close();
                    } catch (IOException e) {
                        LOG.error("Could not read input stream from process. " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
                ioThread.start();
                try {
                    return p.waitFor();
                } catch (InterruptedException e) {
                    LOG.error("Process interrupted. " + e.getMessage());
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                LOG.error("Could not execute command: " + command);

                throw new RuntimeException(e);
            }
        }
    }
}

