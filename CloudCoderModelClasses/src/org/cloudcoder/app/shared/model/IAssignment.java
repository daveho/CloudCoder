package org.cloudcoder.app.shared.model;

import java.util.Date;

public interface IAssignment
{

    /**
     * @return the whenAssigned
     */
    public abstract long getWhenAssigned();

    /**
     * Get "when assigned" as a java.util.Date.
     * 
     * @return "when assigned" as a java.util.Date
     */
    public abstract Date getWhenAssignedAsDate();

    /**
     * @return the whenDue
     */
    public abstract long getWhenDue();

    /**
     * Get "when due" as a java.util.Date.
     * 
     * @return "when due" as a java.util.Date.
     */
    public abstract Date getWhenDueAsDate();

    /**
     * @param whenAssigned the whenAssigned to set
     */
    public abstract void setWhenAssigned(long whenAssigned);

    /**
     * @param whenDue the whenDue to set
     */
    public abstract void setWhenDue(long whenDue);

}
