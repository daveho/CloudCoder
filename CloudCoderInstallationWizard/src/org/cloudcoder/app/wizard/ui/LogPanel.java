package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.cloudcoder.app.wizard.exec.InstallationConstants;
import org.cloudcoder.app.wizard.exec.Util;

/**
 * Capture all output written to System.out and System.err
 * to a text pane and to a log file.
 */
public class LogPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static PrintStream realOut;
	public static PrintStream realErr;
	
	// Display log messages, queueing them if the LogPanel
	// is not visible yet.
	private class LogAppender {
		private Color color;
		private List<String> queuedText;
		private Object lock;
		
		public LogAppender(Color color) {
			this.color = color;
			this.queuedText = new ArrayList<String>();
			this.lock = new Object();
		}
		
		public void flush() {
			synchronized (lock) {
				for (String line : queuedText) {
					appendText(line, color);
				}
				queuedText.clear();
			}
		}
		
		public void append(String line) {
			List<String> toAppend = new ArrayList<String>();
			synchronized (lock) {
				if (!logPanelAdded) {
					queuedText.add(line);
					return;
				}
				toAppend.addAll(queuedText);
				toAppend.add(line);
			}
			for (String text : toAppend) {
				appendText(text, color); // log to text pane
				log.print(text); // log to file
			}
		}
	}

	// Collect text and send it to a queue
	private class OutputSink extends OutputStream {
		private LinkedBlockingQueue<String> queue;
		private CharsetDecoder decoder;
		private boolean closed;
		
		public OutputSink(LinkedBlockingQueue<String> queue) {
			this.queue = queue;
			this.decoder = Charset.forName("UTF-8").newDecoder();
			decoder.reset();
			this.closed = false;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte)b}, 0, 1);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (this.closed) {
				throw new IOException("OutputSink is closed");
			}
			ByteBuffer in = ByteBuffer.wrap(b, off, len);
//			realOut.println("Decoding " + len + " bytes of input\n");
//			realOut.println("remaining=" + in.remaining() + "\n");
			
			char[] outArr = new char[(int)(len * decoder.maxCharsPerByte())];
			CharBuffer out = CharBuffer.wrap(outArr);
			
			CoderResult cr;
			cr = decoder.decode(in, out, false);
			if (cr.isOverflow()) {
				realOut.println("Decoder overflow (while decoding), should not happen\n");
				return;
			}
			String text = new String(outArr, 0, out.position());
//			realOut.println("Sending " + text.length() + " chars");
			queue.offer(text);
		}
		
		@Override
		public void close() throws IOException {
			ByteBuffer in = ByteBuffer.wrap(new byte[0]);
			char[] outArr = new char[100]; // just in case there is unexpected data to be decoded
			CharBuffer out = CharBuffer.wrap(outArr);
			CoderResult res = decoder.decode(in, out, true);
			if (res.isOverflow()) {
				realOut.println("Decoder overflow (while closing), should not happen\n");
				return;
			}
			res = decoder.flush(out);
			if (res.isOverflow()) {
				realOut.println("Decoder overflow (while flushing), should not happen\n");
				return;
			}
			String text = new String(outArr, 0, out.position());
			if (text.length() > 0) {
				queue.offer(text);
			}
			this.closed = true;
		}
	}
	
	// Runnable for monitoring a queue (fed by an OutputSink)
	// to which output from System.out and System.err is arriving.
	// Append the output to the text pane and to the current
	// log file.
	private class OutputMonitor implements Runnable {
		private LogAppender appender;
		private LinkedBlockingQueue<String> queue;
		private StringBuilder buf;
		
		public OutputMonitor(LogAppender appender) {
			this.appender = appender;
			queue = new LinkedBlockingQueue<String>();
			buf = new StringBuilder();
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					String buf = queue.take();
//					realOut.println("Received " + buf.length() + " chars\n");
					this.buf.append(buf);
					process();
				}
			} catch (InterruptedException e) {
				realOut.println("OutputMonitor interrupted!");
			}
		}
		
		private void process() {
			while (true) {
				int nl = buf.indexOf("\n");
				if (nl < 0) {
					break;
				}
				String line = buf.substring(0, nl+1);
				appender.append(line);
				buf.delete(0, nl+1);
			}
		}
		
		public OutputStream getOutputSink() {
			return new OutputSink(queue);
		}
	}
	
	// Static instance of LogPanel - this needs to be created extremely early in the
	// app initialization (in order to avoid the original System.out and System.err
	// being cached.)
	private static LogPanel instance;
	
	public static void createInstance() throws IOException {
		if (instance != null) {
			throw new IllegalStateException();
		}
		instance = new LogPanel();
	}
	
	public static LogPanel getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance;
	}

	private JTextPane textPane;
	private volatile boolean logPanelAdded;
	private LogAppender stdoutAppender;
	private LogAppender stderrAppender;
	private File logFile;
	private PrintWriter log;
	
	private LogPanel() throws IOException {
		setLayout(new BorderLayout());
		
		this.textPane = new JTextPane();
		
		textPane.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		textPane.setBackground(Color.BLACK);
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		logPanelAdded = false;
		JScrollPane scrollPane = new JScrollPane(textPane);

		add(scrollPane, BorderLayout.CENTER);
		
		// Log to text pane
		stdoutAppender = new LogAppender(Color.GRAY);
		stderrAppender = new LogAppender(Color.RED);
		
		// Make sure data directory exists
		Util.createDataDir();

		// Also log to file
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		String logFileName = "installwizard-" + df.format(new Date()) + ".log";
		this.logFile = new File(InstallationConstants.DATA_DIR, logFileName);
		this.log = new PrintWriter(
				new OutputStreamWriter(
						new BufferedOutputStream(
							new FileOutputStream(logFile)
						),
						Charset.forName("UTF-8")
					)
				);
		
		startMonitors();
	}

	private void startMonitors() {
		try {
			// Save real streams
			realOut = System.out;
			realErr = System.err;
			
			// Redirect System.out and System.err
			System.setOut(new PrintStream(createOutputMonitor(stdoutAppender).getOutputSink(), true, "UTF-8"));
			System.setErr(new PrintStream(createOutputMonitor(stderrAppender).getOutputSink(), true, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// Should not happen
			throw new IllegalStateException("UTF-8 encoding not supported?");
		}
//		System.out.println("This is text printed to System.out");
//		System.err.println("This is text printed to System.err");
		System.out.println("Log file for this session is: " + logFile.getAbsolutePath());
	}

	private OutputMonitor createOutputMonitor(LogAppender appender) {
		OutputMonitor monitor = new OutputMonitor(appender);
		Thread t = new Thread(monitor);
		t.setDaemon(true);
		t.start();
		return monitor;
	}
	
	private void appendText(final String line, final Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// See: http://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea
				StyleContext sc = StyleContext.getDefaultStyleContext();
				AttributeSet attr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
				int len = textPane.getDocument().getLength();
				textPane.setCaretPosition(len);
				textPane.setCharacterAttributes(attr, false);
				textPane.replaceSelection(line);
			}
		});
	}

	/**
	 * This must be called to enable output to be displayed.
	 */
	public void enableOutput() {
		logPanelAdded = true;
		stdoutAppender.flush();
		stderrAppender.flush();
	}
	
	/**
	 * Flush the log file.
	 * This should be called once the installation has
	 * finished (successfully or unsuccessfully)
	 * to ensure that all output has made it to the log file.
	 */
	public void flushLog() {
		log.flush();
	}
	
	/**
	 * @return the log file
	 */
	public File getLogFile() {
		return logFile;
	}
}
