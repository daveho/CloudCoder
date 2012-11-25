// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.ccompiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.builder2.process.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compile a C/C++ program.
 * FIXME: currently is hard-coded to use gcc.  Would be nice to support other compilers.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class Compiler {
    private static final Logger logger=LoggerFactory.getLogger(Compiler.class);

	public static final String DEFAULT_COMPILER_EXE = "gcc";

    private String compilerExe;
    private String progName;
    private File workDir;
    private String code;
    private String statusMessage;
    private List<String> compilerOutput;

    /**
     * Constructor.
     * 
     * @param code     the C/C++ program to compile
     * @param workDir  the working directory where compilation should take place
     * @param progName the name to be given to the resulting executable
     */
    public Compiler(String code, File workDir, String progName) {
    	this.compilerExe = DEFAULT_COMPILER_EXE;
        this.progName = progName;
        this.workDir = workDir;
        this.code = code;
        this.statusMessage = "";
        this.compilerOutput = new LinkedList<String>();
    }
    
    /**
     * Set the name of the compiler executable (e.g., "gcc").
     * 
	 * @param compilerExe the compiler executable to set
	 */
	public void setCompilerExe(String compilerExe) {
		this.compilerExe = compilerExe;
	}

	/**
	 * Attempt to compile the program.
	 * 
	 * @return true if the compilation was successful, false if not
	 */
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
    
    /**
     * Get {@link CompilerDiagnostic}s resulting from attempting
     * (successfully or unsuccessfully) to compile the program.
     * 
     * @return the list of {@link CompilerDiagnostic}s
     */
    public CompilerDiagnostic[] getCompilerDiagnosticList() {
        //TODO: Limit to only errors for the functions we're interested in
    	ArrayList<CompilerDiagnostic> result = new ArrayList<CompilerDiagnostic>();
        for (String s : compilerOutput) {
            CompilerDiagnostic d = CompilerDiagnosticUtil.diagnosticFromGcc(s);
            if (d != null) {
            	result.add(d);
            }
        }
        return result.toArray(new CompilerDiagnostic[result.size()]);
    }        

    private String[] getCompileCmd() {
        return new String[]{
                this.compilerExe,
                "-Wall", // ALWAYS use -Wall
                "-o",
                getExeFileName(),
                getSourceFileName(),
        };
    }

    private String getSourceFileName() {
        return progName + ".c";
    }

    private String getExeFileName() {
        return progName;
    }

    private boolean runCommand(File tempDir, String[] cmd) {
        ProcessRunner runner = new ProcessRunner();
        if (!runner.runSynchronous(tempDir, cmd)) {
            statusMessage = runner.getStatusMessage();
            return false;
        }

        compilerOutput.addAll(runner.getStderrAsList());
        if (runner.getExitCode() != 0) {
            statusMessage = cmd[0] + " exited with non-zero exit code " + runner.getExitCode();
            return false;
        }

        return true;
    }

    /**
     * Get a status message summarizing the result of the compilation attempt.
     * 
     * @return the status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Get raw lines of compiler output.
     * 
     * @return raw lines of compiler output
     */
    public List<String> getCompilerOutput() {
        return Collections.unmodifiableList(compilerOutput);
    }

    public void setProgramName(String progname) {
        this.progName = progname;
    }
}
