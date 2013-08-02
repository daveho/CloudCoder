// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.loadtester;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.SubmissionResult;

/**
 * Singleton class to report load tester activity to the terminal
 * using colors and text characters.  Assumes that the terminal
 * is capable of displaying 256 colors.  Also keeps track of some
 * useful stats such as the number of exceptions (recoverable and
 * unrecoverable).
 * 
 * @author David Hovemeyer
 */
public class LoadTesterActivityReporter {
	private static final LoadTesterActivityReporter theInstance = new LoadTesterActivityReporter();
	
	private static class Item {
		public final int taskNumber;
		public Item(int taskNumber) {
			this.taskNumber = taskNumber;
		}
	}
	
	private static class FullTextItem extends Item {
		public FullTextItem(int taskNumber) {
			super(taskNumber);
		}
	}
	
	private static class SendItem extends Item {
		public final int numChanges;
		public SendItem(int taskNumber, int numChanges) {
			super(taskNumber);
			this.numChanges = numChanges;
		}
	}
	
	private static class SubmissionResultItem extends Item {
		public final boolean success;
		public SubmissionResultItem(int taskNumber, boolean success) {
			super(taskNumber);
			this.success = success;
		}
	}
	
	private static class RecoverableExceptionItem extends Item {
		public RecoverableExceptionItem(int taskNumber) {
			super(taskNumber);
		}
	}
	
	private class MonitorTask implements Runnable {
		private static final String FULL_TEXT = "\u2191"; // up arrow
		private static final String UNSUCCESSFUL_SUBMISSION = "\u2717"; // ballot X
		private static final String SUCCESSFUL_SUBMISSION = "\u2713";   // check mark
//		private static final String UNSUCCESSFUL_SUBMISSION = "\u2639"; // frowney
//		private static final String SUCCESSFUL_SUBMISSION = "\u263A";   // smiley
		private static final String RECOVERABLE_EXCEPTION = "\u2620"; // skull and crossbones

		@Override
		public void run() {
			boolean done = false;
			while (!done && !Thread.interrupted()) {
				try {
					Item item = activityQueue.take();
					if (item instanceof FullTextItem) {
						onFullText(item.taskNumber);
					} else if (item instanceof SendItem) {
						onSend(item.taskNumber, ((SendItem)item).numChanges);
					} else if (item instanceof SubmissionResultItem) {
						onSubmissionResult(item.taskNumber, ((SubmissionResultItem)item).success);
					} else if (item instanceof RecoverableExceptionItem) {
						onRecoverableException(item.taskNumber);
					}
				} catch (InterruptedException e) {
					done = true;
				}
			}
		}

		private void onFullText(int taskNumber) {
			out(taskNumber, FULL_TEXT);
		}

		private void onSend(int taskNumber, int numChanges) {
			char[] a = new char[numChanges];
			Arrays.fill(a, '.');
			out(taskNumber, new String(a));
		}

		private void onSubmissionResult(int taskNumber, boolean success) {
			out(taskNumber, success ? SUCCESSFUL_SUBMISSION : UNSUCCESSFUL_SUBMISSION);
		}

		private void onRecoverableException(int taskNumber) {
			out(taskNumber, RECOVERABLE_EXCEPTION);
		}

		private void out(int taskNumber, String str) {
			setColor(taskNumber);
			System.out.print(str);
			System.out.flush();
			unsetColor();
		}

		private void setColor(int taskNumber) {
			// Set one of the 6x6x6 color cube colors based on the current LoadTesterTask's
			// sequence number (avoiding color 16, which renders as black)
			int color = 17 + taskNumber%(6*6*6 - 1);
			System.out.printf("%c[38;5;%dm", 27, color);
			System.out.flush();
		}

		private void unsetColor() {
			// Restore to default terminal color
			System.out.printf("%c[0m", 27);
			System.out.flush();
		}
	}
	
	private ICallback<Change[]> ON_SEND_CALLBACK = new ICallback<Change[]>() {
		@Override
		public void call(Change[] value) {
			int taskNumber = LoadTesterTask.getCurrent().getSequenceNumber();
			if (value.length == 1 && value[0].getType() == ChangeType.FULL_TEXT) {
				activityQueue.add(new FullTextItem(taskNumber));
			} else {
				SendItem item = new SendItem(taskNumber, value.length);
				activityQueue.add(item);
			}
		}
	};
	
	private ICallback<SubmissionResult> ON_SUBMISISON_RESULT_CALLBACK = new ICallback<SubmissionResult>() {
		public void call(SubmissionResult value) {
			int taskNumber = LoadTesterTask.getCurrent().getSequenceNumber();
			boolean success = value.getCompilationResult().getOutcome() == CompilationOutcome.SUCCESS
					&& value.getNumTestsPassed() >= value.getNumTestsAttempted();
			SubmissionResultItem item = new SubmissionResultItem(taskNumber, success);
			activityQueue.add(item);
		}
	};
	
	private final LinkedBlockingQueue<Item> activityQueue = new LinkedBlockingQueue<Item>();
	
	private AtomicBoolean started = new AtomicBoolean();
	
	private AtomicInteger recoverableExceptionCount;
	private AtomicInteger unrecoverableExceptionCount;
	private StatsCollector statsCollector;
	
	private LoadTesterActivityReporter() {
		recoverableExceptionCount = new AtomicInteger(0);
		unrecoverableExceptionCount = new AtomicInteger(0);
		statsCollector = new StatsCollector();
	}
	
	/**
	 * @return the recoverable exception count
	 */
	public int getRecoverableExceptionCount() {
		return recoverableExceptionCount.get();
	}
	
	/**
	 * @return the unrecoverable exception count
	 */
	public int getUnrecoverableExceptionCount() {
		return unrecoverableExceptionCount.get();
	}
	
	/**
	 * @return the {@link StatsCollector}
	 */
	public StatsCollector getStatsCollector() {
		return statsCollector;
	}
	
	/**
	 * @return the singleton instance of {@link LoadTesterActivityReporter}
	 */
	public static LoadTesterActivityReporter getInstance() {
		return theInstance;
	}
	
	/**
	 * Start the reporter's monitor thread.
	 * This may be called any number of times safely: only the first
	 * call will start the monitor thread.
	 */
	public void start() {
		if (started.compareAndSet(false, true)) {
			Thread t = new Thread(new MonitorTask());
			t.setDaemon(true);
			t.start();
		}
	}
	
	/**
	 * @return the on send callback that should be used for all {@link LoadTesterTask}s
	 */
	public ICallback<Change[]> getOnSendCallback() {
		return ON_SEND_CALLBACK;
	}
	
	/**
	 * @return the on submission result callback that should be used for all {@link LoadTesterTask}s
	 */
	public ICallback<SubmissionResult> getOnSubmissionResultCallback() {
		return ON_SUBMISISON_RESULT_CALLBACK;
	}

	/**
	 * Report the occurrence of a recoverable exception.
	 * @param ex the exception
	 */
	public void reportRecoverableException(Exception ex) {
		activityQueue.add(new RecoverableExceptionItem(LoadTesterTask.getCurrent().getSequenceNumber()));
		recoverableExceptionCount.incrementAndGet();
	}
	
	/**
	 * Report the occurrence of an unrecoverable exception.
	 * @param ex the exception
	 */
	public void reportUnrecoverableException(Exception ex) {
		unrecoverableExceptionCount.incrementAndGet();
	}
}
