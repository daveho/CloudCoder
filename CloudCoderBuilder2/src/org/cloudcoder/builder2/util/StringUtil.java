package org.cloudcoder.builder2.util;

import java.util.Arrays;
import java.util.List;

/**
 * String utility methods.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class StringUtil {

	/**
	 * Merge an array of lines of text into a single string with
	 * each line separated by a newline.
	 * 
	 * @param list the array of lines
	 * @return the merged String
	 */
	public static String merge(String[] list) {
		return StringUtil.merge(Arrays.asList(list));
	}

	/**
	 * Merge a list of lines of text into a single string with
	 * each line separated by a newline.
	 * 
	 * @param list the list of lines
	 * @return the merged String
	 */
	public static String merge(List<String> list){
		return StringUtil.doMerge(list, "\n");
	}

	/**
	 * Merge an array of lines of text into a single string with
	 * each line separated by a space (to create a single result line).
	 * 
	 * @param list the array of lines
	 * @return the merged String
	 */
	public static String mergeOneLine(String[] list) {
		return StringUtil.mergeOneLine(Arrays.asList(list));
	}

	/**
	 * Merge a list of lines of text into a single string with
	 * each line separated by a space (to create a single result line).
	 * 
	 * @param list the list of lines
	 * @return the merged String
	 */
	public static String mergeOneLine(List<String> list) {
		return StringUtil.doMerge(list, " ");
	}

	/**
	 * Merge given strings by combining them using given separator.
	 * 
	 * @param list list of strings
	 * @param sep  separator
	 * @return merged result
	 */
	public static String doMerge(List<String> list, String sep) {
		StringBuilder builder=new StringBuilder();
		for (String s : list) {
			builder.append(s);
			builder.append(sep);
		}
		return builder.toString();
	}

	/**
	 * Count the number of lines of text in given string.
	 * 
	 * @param s the string
	 * @return number of lines
	 */
    public static int countLines(String s) {
        int count=0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i)=='\n') {
                count++;
            }
        }
        return count;
    }
}
