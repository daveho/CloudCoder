/*
 * Web C programming environment
 * Copyright (c) 2010-2011, David H. Hovemeyer <dhovemey@ycp.edu>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudcoder.submitsvc.oop.builder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently, this class is only for compiling a single C file
 * for the Teensy++.
 */
public class Compiler {
    private static final String COMPILE_BASE_DIR = "compile";
    private static final Logger logger=LoggerFactory.getLogger(Compiler.class);

    private String progName;
    private File workDir;
    private String code;
    private String statusMessage;
    private List<String> compilerOutput;
    private String outputFileName;

    public Compiler(String code, File workDir, String progName) {
        this.progName = progName;
        this.workDir = workDir;
        if (workDir.exists()) {
            logger.warn("work directory already exists!  Going to compile in there without looking");
            //TODO: Crash compiler?
        }
        this.code = code;
        this.statusMessage = "";
        this.compilerOutput = new LinkedList<String>();
    }

    public boolean compile() {
        // copy source program into a .c file in the temporary directory
        File sourceFile = new File(workDir, getSourceFileName());
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(sourceFile));
            IOUtils.write(code, out);
        } catch (IOException e) {
            logger.error("Could not create source file", e);
            statusMessage = "Could not create source file: " + e.getMessage();
        } finally {
            IOUtils.closeQuietly(out);
        }

        if (!runCommand(workDir, getCompileCmd())) {
            return false;
        }

        // success!
        statusMessage = "Compilation succeeded";
        return true;
    }

    private String[] getCompileCmd() {
        return new String[]{
                //"avr-gcc",
                "gcc",
                "-o",
                getExeFileName(),
                //				"-mmcu=at90usb1286",
                //				"-gdwarf-2",
                //				"-Os",
                //				"-funsigned-char",
                //				"-funsigned-bitfields",
                //				"-fpack-struct",
                //				"-fshort-enums",
                //				"-DF_CPU=16000000UL",
                getSourceFileName(),
        };
    }

    private String getSourceFileName() {
        return progName + ".c";
    }

    private String getExeFileName() {
        //return progName + ".elf";
        return progName;
    }

    

    private boolean runCommand(File tempDir, String[] cmd) {
        ProcessRunner runner = new ProcessRunner();
        if (!runner.runSynchronous(tempDir, cmd)) {
            statusMessage = runner.getStatusMessage();
            return false;
        }

        compilerOutput.addAll(runner.getStderr());
        if (runner.getExitCode() != 0) {
            statusMessage = cmd[0] + " exited with non-zero exit code " + runner.getExitCode();
            return false;
        }

        return true;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public List<String> getCompilerOutput() {
        return Collections.unmodifiableList(compilerOutput);
    }

    public void setProgramName(String progname) {
        this.progName = progname;
    }

    public static File makeTempDir(String baseDir) {

        int attempts = 1;
        File tempDir = null;

        while (tempDir == null && attempts < 10) {
            try {
                // start by creating a temporary file
                File tempFile = File.createTempFile("cmp", "", new File(baseDir));

                // temporary file created successfully - delete it and make an identically-named
                // directory
                if (tempFile.delete() && tempFile.mkdir()) {
                    // success!
                    tempDir = tempFile;
                }
            } catch (IOException e) {
                logger.warn("Unable to delete temp file and create directory");
            }
            attempts++;
        }
        return tempDir;
    }
}
