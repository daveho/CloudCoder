package org.cloudcoder.builder2.javamethod;

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Bytecode;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;

/**
 * Execute {@link ProblemType#JAVA_METHOD} tests.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ExecuteJavaMethodTestsBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		// Get array of Bytecode objects representing compiled classes
		Bytecode[] bytecodeList = submission.getArtifact(Bytecode[].class);
		if (bytecodeList == null) {
			throw new InternalBuilderException(this.getClass(), "No Bytecode list");
		}
		
		// Find the bytecode for the Tester class
		Bytecode tester = null;
		for (Bytecode b : bytecodeList) {
			if (b.getClassName().equals("Tester")) {
				tester = b;
				break;
			}
		}
		if (tester == null) {
			throw new InternalBuilderException(this.getClass(), "Could not find Tester bytecode");
		}
		
		// TODO: get classes for compiled bytecode - should use prior classloading step?
		
		/*
        // create a list of tasks to be executed
        List<IsolatedTask<TestResult>> tasks=new ArrayList<IsolatedTask<TestResult>>();
        final Class<?> testerCls=compiler.getClass("Tester");
        for (final TestCase t : testCaseList) {
            tasks.add(new IsolatedTaskRunner(testerCls, t));
        }

        KillableTaskManager<TestResult> pool=new KillableTaskManager<TestResult>(
                tasks, 
                TIMEOUT_LIMIT,
                new KillableTaskManager.TimeoutHandler<TestResult>() {
                    @Override
                    public TestResult handleTimeout() {
                        return new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
                                "Took too long!  Check for infinite loops, or recursion without a proper base case");
                    }
                });

        // run each task in a separate thread
        pool.run();

        // merge outcomes with their buffered inputs for stdout/stderr
        List<TestResult> outcomes=TesterUtils.getStdoutStderr(pool);
        SubmissionResult result=new SubmissionResult(new CompilationResult(CompilationOutcome.SUCCESS));
        result.setTestResults(outcomes.toArray(new TestResult[outcomes.size()]));
        logger.info("Sending back to server "+result.getTestResults().length+" results");
        */
	}

}
