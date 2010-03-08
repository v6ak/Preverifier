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
 *     Vít Šesták				- package renamed
 */
package v6.java.preverifier.results;

/**
 * Error type enumeration for errors that are found during preverification.
 * 
 * @author Craig Setera
 */
public final class PreverificationErrorType {
    private static final String[] STRINGS = new String[] { "No error",
            "Native code not allowed", "Floating point not allowed",
            "Finalizers not allowed", "Missing class definition",
            "Unknown error", };

    /** No error code */
    public static final int NO_ERROR_CODE = 0;

    /** No error type */
    public static final PreverificationErrorType NO_ERROR = new PreverificationErrorType(
            NO_ERROR_CODE);

    /** Native methods not allowed code */
    public static final int NATIVE_CODE = 1;

    /** Native methods not allowed */
    public static final PreverificationErrorType NATIVE = new PreverificationErrorType(
            NATIVE_CODE);

    /** Floating point not allowed in CLDC 1.0 code */
    public static final int FLOATING_POINT_CODE = 2;

    /** Floating point not allowed in CLDC 1.0 */
    public static final PreverificationErrorType FLOATING_POINT = new PreverificationErrorType(
            FLOATING_POINT_CODE);

    /** Finalizers not allowed code */
    public static final int FINALIZERS_CODE = 3;

    /** Finalizers not allowed */
    public static final PreverificationErrorType FINALIZERS = new PreverificationErrorType(
            FINALIZERS_CODE);

    /** Missing type definition code */
    public static final int MISSING_TYPE_CODE = 4;

    /** Missing type definition */
    public static final PreverificationErrorType MISSING_TYPE = new PreverificationErrorType(
            MISSING_TYPE_CODE);

    /** Unknown error type code */
    public static final int UNKNOWN_ERROR_CODE = 5;

    /** Unknown error type */
    public static final PreverificationErrorType UNKNOWN_ERROR = new PreverificationErrorType(
            UNKNOWN_ERROR_CODE);

    private int errorCode;

    /**
     * Private constructor.
     * 
     * @param code
     */
    private PreverificationErrorType(int code) {
        errorCode = code;
    }

    /**
     * Get the code of th error that occurred.
     * 
     * @return
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return STRINGS[errorCode];
    }
}
