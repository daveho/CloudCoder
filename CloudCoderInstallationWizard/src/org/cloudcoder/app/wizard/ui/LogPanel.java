package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
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

public class LogPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static PrintStream realOut;
	private static PrintStream realErr;
	
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
				if (!logPanelShown) {
					queuedText.add(line);
					return;
				}
				toAppend.addAll(queuedText);
				toAppend.add(line);
			}
			for (String text : toAppend) {
				appendText(text, color);
			}
		}
	}

	// Collect text and send it to a queue
	private class OutputSink extends OutputStream {
		private LinkedBlockingQueue<String> queue;
		private LogAppender appender; // only used to display error messages related to stdout/stderr diversion
		private CharsetDecoder decoder;
		private boolean closed;
		
		public OutputSink(LinkedBlockingQueue<String> queue, LogAppender appender) {
			this.queue = queue;
			this.appender = appender;
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
//			realOut.println("Sending " + text.length() + " chars", Color.GREEN);
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
//					realOut.println("Received " + buf.length() + " chars\n", Color.GREEN);
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
			return new OutputSink(queue, appender);
		}
	}
	
	// Static instance of LogPanel - this needs to be created extremely early in the
	// app initialization (in order to avoid the original System.out and System.err
	// being cached.)
	private static LogPanel instance;
	
	public static void createInstance() {
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
	private volatile boolean logPanelShown;
	private LogAppender stdoutAppender;
	private LogAppender stderrAppender;
	
	private LogPanel() {
		setLayout(new BorderLayout());
		
		this.textPane = new JTextPane();
		
		textPane.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		textPane.setBackground(Color.BLACK);
		textPane.setFont(new Font("monospaced", Font.PLAIN, 12));
		logPanelShown = false;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				logPanelShown = true;
				stdoutAppender.flush();
				stderrAppender.flush();
			}
		});
		JScrollPane scrollPane = new JScrollPane(textPane);

		add(scrollPane, BorderLayout.CENTER);
		
		stdoutAppender = new LogAppender(Color.GRAY);
		stderrAppender = new LogAppender(Color.RED);
		
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
		System.out.println("This is text printed to System.out");
		System.err.println("This is text printed to System.err");
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
}
