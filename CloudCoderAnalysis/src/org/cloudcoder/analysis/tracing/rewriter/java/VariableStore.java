package org.cloudcoder.analysis.tracing.rewriter.java;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import experimental.IVariableStore;

public class VariableStore implements IVariableStore
{
    /*
     * TODO: handle previous and next links
     *  (either here or in CodePath
     * 
     * TODO: handle start/end loop links 
     *  (either here or in CodePath)
     * 
     * TODO: Include text of line of code, in canonicalized form
     * 
     * TODO: how to handle null statements like closing braces?
     */
    
    /** 
     * Maps variable names to their values (as Strings).
     */
    protected Map<String, String> varmap=new LinkedHashMap<String,String>();
    /**
     * String representation of the current line.
     * For example:
     * 
     * "1" for line 1
     * "4-2" for line 4, 2nd loop iteration
     * "5-3-1" for line 5, 3rd loop iteration of the outer loop,
     *      first loop iteration of the inner loop
     */
    protected String lineNum;
    protected String code;
    
    public String getLineNum() {
        return lineNum;
    }
    
    public VariableStore(String lineNum) {
        this.lineNum=lineNum;
    }
    
    public String toString() {
        StringBuffer buf=new StringBuffer();
        buf.append(getLineNum()+"\n");
        for (Entry<String,String> entry : varmap.entrySet()) {
            buf.append("\t"+entry.getKey()+" => "+entry.getValue()+"\n");
        }
        return buf.toString();
    }
    
    static String toString(Object value) {
        if (value instanceof Integer ||
                value instanceof String ||
                value instanceof Float ||
                value instanceof Double ||
                value instanceof Character ||
                value instanceof Boolean || 
                value instanceof Short ||
                value instanceof Byte ||
                value instanceof Long)
        {
            return value.toString();
        }
        if (value instanceof int[]) {
            int[] arr=(int[])value;
            StringBuffer buf=new StringBuffer();
            buf.append("[");
            for (int i=0; i<arr.length-1; i++) {
                buf.append(arr[i]+", ");
            }
            buf.append(arr[arr.length-1]+"]");
            return buf.toString();
        }
        if (value instanceof boolean[]) {
            boolean[] arr=(boolean[])value;
            StringBuffer buf=new StringBuffer();
            buf.append("[");
            for (int i=0; i<arr.length-1; i++) {
                buf.append(arr[i]+", ");
            }
            buf.append(arr[arr.length-1]+"]");
            return buf.toString();
        }
        if (value instanceof String[]) {
            String[] arr=(String[])value;
            StringBuffer buf=new StringBuffer();
            buf.append("[");
            for (int i=0; i<arr.length-1; i++) {
                buf.append("\""+arr[i]+"\", ");
            }
            buf.append("\""+arr[arr.length-1]+"\"]");
            return buf.toString();
        }
        throw new IllegalArgumentException("Can only trace primitives, " +
                "String, String[], int[], boolean[];\n"+
                "Cannot trace: "+value.getClass());
    }
    
    public void put(String varname, Object value) {
        if (!varmap.containsKey(varname)){
            varmap.put(varname, toString(value));
        } else {
            varmap.put(varname, toString(value));
        }
    }

    public static void main(String[] args)
    {
        //int[] arr=new int[] {1,2,3,4};
        //Object[] arr2=(Object[])arr;
        toString(5);
    }
    
    /*
     * Store a Map<String,Object> for:
     * 
     * locals
     * params
     * instance vars
     * static vars
     * something like the loop counter, which is a special 
     *      var bound to the loop that starts at a particular
     *      line of code
     * 
     * Vars can be:
     * undef (haven't reached that point in the code yet)
     * null (legitimately null)
     * whatever value they actually have
     * 
     * We insert update statements after every statement of code.
     * This should assign new values to any variable that 
     *      has a new value.
     * Easiest to use .equals() to determine what has changed,
     *      although faster to check each statement for possible
     *      things that have changed.
     * For primitives, just store the value for every line.
     * For reference types, probably want to store deltas
     *      from an initial value.  Not sure how to serialize
     *      reference types.  Can figure out an easy way to 
     *      serialize arrays, and Strings are easy.
     * 
     */

    

}
