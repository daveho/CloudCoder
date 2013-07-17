package org.cloudcoder.app.loadtester;

/**
 * Load tester: creates {@link LoadTesterTask}s and runs them
 * in as many threads as necessary to achieve the desired degree
 * of concurrency.
 * 
 * @author David Hovemeyer
 */
public class LoadTester {
	
	private HostConfig hostConfig;
	private Mix mix;
	private int numThreads;
	private int repeatCount;

	/**
	 * Constructor.
	 */
	public LoadTester() {
		
	}
	
	/**
	 * Set the {@link HostConfig} specifying how to connect to the host webapp.
	 * 
	 * @param hostConfig the {@link HostConfig}
	 */
	public void setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
	}
	
	/**
	 * Set the {@link Mix} containing the {@link EditSequence}s that will be played.
	 * 
	 * @param mix the {@link Mix}
	 */
	public void setMix(Mix mix) {
		this.mix = mix;
	}
	
	/**
	 * Set how many concurrent threads should be used.
	 * 
	 * @param numThreads number of concurrent threads
	 */
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	/**
	 * Set how many times each thread should repeat playing its edit sequence.
	 * 
	 * @param repeatCount how many times each thread should repeat playing its edit sequence
	 */
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	/**
	 * Execute the tasks and wait for them to complete.
	 */
	public void execute() {
		// Create tasks
		LoadTesterTask[] tasks = new LoadTesterTask[numThreads];
		
		// EditSequences are assigned to tasks in round-robin order
		int seqIndex = 0;
		
		for (int i = 0; i < numThreads; i++) {
			tasks[i] = new LoadTesterTask();
			
			// We assume the test user accounts are "user1", "user2", etc.,
			// with passwords matching the usernames.
			String testUserName = "user" + (i+1);
			tasks[i].setUserName(testUserName);
			tasks[i].setPassword(testUserName);
			tasks[i].setHostConfig(hostConfig);
			tasks[i].setEditSequence(mix.get(seqIndex));
			tasks[i].setRepeatCount(repeatCount);
			
			seqIndex++;
			if (seqIndex >= mix.size()) {
				seqIndex = 0;
			}
		}
		
		// Create threads to execute the tasks, and start them
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(tasks[i]);
			threads[i].start();
		}
		
		// Wait for tasks to complete
		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.err.println("InterruptedException waiting for thread " + i);
			}
		}
		
		System.out.println("\nLoading testing complete");
	}
}
