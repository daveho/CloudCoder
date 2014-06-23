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

package org.cloudcoder.builder2.javasandbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * PrintStream that captures and buffers all output for however many
 * different threads may be printing to the given stream.
 * 
 * Common case is to pass an instance of this stream to System.setOut
 * right before starting a bunch of threads that print to System.out.
 * 
 * @author jspacco
 *
 */
public class ThreadedPrintStreamMonitor extends PrintStream
{
    /** Maps stdout and stderr for each thread */
    private Map<String, PrintStream> printStreamMap=new HashMap<String, PrintStream>();
    private Map<String, ByteArrayOutputStream> baosMap=
        new HashMap<String, ByteArrayOutputStream>();

    public ThreadedPrintStreamMonitor(PrintStream stream) {
        // XXX Is this a reasonable constructor?
        // I've intercepted just about every method call,
        // so nothing should actually be sent to super.
        // The other option was to have no params and do a
        // superconstructor call with null, but in that case calls
        // that leak up to super will crash.  This way we'll just
        // get random method calls on whatever stream (probably stdout and stderr)
        // the constructor is called on.
        super(stream);
    }
    
    public ThreadedPrintStreamMonitor() {
        super(new ByteArrayOutputStream());
    }
    
    private PrintStream getPrintStream() {
        return getPrintStream(Thread.currentThread());
    }
    
    private PrintStream getPrintStream(Thread t) {
        String name=t.getName();
        if (!baosMap.containsKey(name)) {
            baosMap.put(name, new ByteArrayOutputStream());
        }
        if (!printStreamMap.containsKey(name)) {
            printStreamMap.put(name, new PrintStream(baosMap.get(name)));
        }
        return printStreamMap.get(name);
    }
    
    /**
     * Given a thread, look up the backing ByteArrayOutputStream
     * that was used to buffer that thread's output, and return
     * it as a String.
     * 
     * @param t Thread
     * @return String containing that thread's buffered printing output.
     */
    public String getBufferedOutput(Thread t) {
        PrintStream ps=getPrintStream(t);
        ps.flush();
        ps.close();
        return baosMap.get(t.getName()).toString();
    }
    
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(java.lang.String)
     */
    @Override
    public void print(String s) {
        getPrintStream().print(s);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#write(int)
     */
    @Override
    public void write(int b) {
        getPrintStream().write(b);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        getPrintStream().write(buf,off,len);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(boolean)
     */
    @Override
    public void print(boolean b) {
        getPrintStream().print(b);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(char)
     */
    @Override
    public void print(char c) {
        getPrintStream().print(c);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(int)
     */
    @Override
    public void print(int i) {
        getPrintStream().print(i);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(long)
     */
    @Override
    public void print(long l) {
        getPrintStream().print(l);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(float)
     */
    @Override
    public void print(float f) {
        getPrintStream().print(f);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(double)
     */
    @Override
    public void print(double d) {
        getPrintStream().print(d);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(char[])
     */
    @Override
    public void print(char[] s) {
        getPrintStream().print(s);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#print(java.lang.Object)
     */
    @Override
    public void print(Object obj) {
        getPrintStream().print(obj);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println()
     */
    @Override
    public void println() {
        getPrintStream().println();
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(boolean)
     */
    @Override
    public void println(boolean x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(char)
     */
    @Override
    public void println(char x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(int)
     */
    @Override
    public void println(int x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(long)
     */
    @Override
    public void println(long x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(float)
     */
    @Override
    public void println(float x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(double)
     */
    @Override
    public void println(double x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(char[])
     */
    @Override
    public void println(char[] x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(java.lang.String)
     */
    @Override
    public void println(String x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#println(java.lang.Object)
     */
    @Override
    public void println(Object x) {
        getPrintStream().println(x);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream printf(String format, Object... args) {
        return getPrintStream().printf(format, args);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return getPrintStream().printf(l, format, args);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence)
     */
    @Override
    public PrintStream append(CharSequence csq) {
        return getPrintStream().append(csq);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
     */
    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return getPrintStream().append(csq, start, end);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#append(char)
     */
    @Override
    public PrintStream append(char c) {
        return getPrintStream().append(c);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#checkError()
     */
    @Override
    public boolean checkError() {
        return getPrintStream().checkError();
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#flush()
     */
    @Override
    public void flush() {
        getPrintStream().flush();
    }
    public void flush(Thread t) {
        getPrintStream(t).flush();
    }
    public void flushAll() {
        for (PrintStream ps : printStreamMap.values()) {
            ps.flush();
        }
    }
    public void closeAll() {
        for (PrintStream ps : printStreamMap.values()) {
            ps.close();
        }
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close() {
        getPrintStream().close();
    }
    public void close(Thread t) {
        getPrintStream(t).close();
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#format(java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream format(String format, Object... args) {
        return getPrintStream().format(format, args);
    }
    /* (non-Javadoc)
     * @see java.io.PrintStream#format(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return getPrintStream().format(l,format,args);
    }
    /* (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        getPrintStream().write(b);
    }
}
