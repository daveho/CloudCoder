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
//			appendText("Decoding " + len + " bytes of input\n", Color.GREEN);
//			appendText("remaining=" + in.remaining() + "\n", Color.GREEN);
			
			char[] outArr = new char[(int)(len * decoder.maxCharsPerByte())];
			CharBuffer out = CharBuffer.wrap(outArr);
			
			CoderResult cr;
			cr = decoder.decode(in, out, false);
			if (cr.isOverflow()) {
				appendText("Decoder overflow (while decoding), should not happen\n", Color.WHITE);
				return;
			}
			String text = new String(outArr, 0, out.position());
//			appendText("Sending " + text.length() + " chars", Color.GREEN);
			queue.offer(text);
		}
		
		@Override
		public void close() throws IOException {
			ByteBuffer in = ByteBuffer.wrap(new byte[0]);
			char[] outArr = new char[100]; // just in case there is unexpected data to be decoded
			CharBuffer out = CharBuffer.wrap(outArr);
			CoderResult res = decoder.decode(in, out, true);
			if (res.isOverflow()) {
				appendText("Decoder overflow (while closing), should not happen\n", Color.WHITE);
				return;
			}
			res = decoder.flush(out);
			if (res.isOverflow()) {
				appendText("Decoder overflow (while flushing), should not happen\n", Color.WHITE);
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
		private Color color;
		private LinkedBlockingQueue<String> queue;
		private StringBuilder buf;
		
		public OutputMonitor(Color color) {
			this.color = color;
			queue = new LinkedBlockingQueue<String>();
			buf = new StringBuilder();
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					String buf = queue.take();
//					appendText("Received " + buf.length() + " chars\n", Color.GREEN);
					this.buf.append(buf);
					process();
				}
			} catch (InterruptedException e) {
				appendText("OutputMonitor interrupted!", Color.WHITE);
			}
		}
		
		private void process() {
			while (true) {
				int nl = buf.indexOf("\n");
				if (nl < 0) {
					break;
				}
				String line = buf.substring(0, nl+1);
				appendText(line, color);
				buf.delete(0, nl+1);
			}
		}
		
		public OutputStream getOutputSink() {
			return new OutputSink(queue);
		}
	}

	private JTextPane textPane;
	private boolean monitorsStarted;
	
	public LogPanel() {
		setLayout(new BorderLayout());
		
		this.textPane = new JTextPane();
		
		textPane.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		textPane.setBackground(Color.BLACK);
		textPane.setFont(new Font("Courier New", Font.PLAIN, 12));
		monitorsStarted = false;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				startMonitors();
			}
		});
		JScrollPane scrollPane = new JScrollPane(textPane);

		add(scrollPane, BorderLayout.CENTER);
	}
	
	private void startMonitors() {
		if (!this.monitorsStarted) {
			this.monitorsStarted = true;
			try {
				System.setOut(new PrintStream(createOutputMonitor(Color.LIGHT_GRAY).getOutputSink(), true, "UTF-8"));
				System.setErr(new PrintStream(createOutputMonitor(Color.RED).getOutputSink(), true, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// Should not happen
				throw new IllegalStateException("UTF-8 encoding not supported?");
			}
			System.out.println("This is text printed to System.out");
			System.err.println("This is text printed to System.err");
		}
	}

	private OutputMonitor createOutputMonitor(Color textColor) {
		OutputMonitor monitor = new OutputMonitor(textColor);
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
