/**
 * Copyright (c) 2003,2008 Craig Setera and others.
 * Copyright (c) 2010 Vít Šesták
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Craig Setera (EclipseME) - Initial implementation
 *     Diego Sandin (Motorola)  - Refactoring package name to follow eclipse 
 *                                standards
 *     Vít Šesták				- package renamed, toString() added
 */
package v6.java.preverifier.results;

import lombok.ToString;

/**
 * The description of an error that occurred during preverification.
 * 
 * @author Craig Setera
 */
@ToString
public class PreverificationError {
    // The error information
    private PreverificationErrorType errorType;
    private PreverificationErrorLocation errorLocation;
    private String detail;

    /**
     * Construct a new error.
     * 
     * @param type
     * @param location
     * @param detailMessage
     */
    public PreverificationError(PreverificationErrorType type,
            PreverificationErrorLocation location, String detailMessage) {
        super();

        this.detail = detailMessage;
        this.errorLocation = location;
        this.errorType = type;
    }

    /**
     * Returns the detailed message concerning this error or <code>null</code>
     * if no detail message has been specified.
     * 
     * @return Returns the detail.
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Returns the location where the error occurred.
     * 
     * @return Returns the error location.
     */
    public PreverificationErrorLocation getLocation() {
        return errorLocation;
    }

    /**
     * Returns the type of the error.
     * 
     * @return Returns the error type.
     */
    public PreverificationErrorType getType() {
        return errorType;
    }
}
