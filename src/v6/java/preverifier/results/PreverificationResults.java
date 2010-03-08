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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Results of the preverification.
 * 
 * @author Craig Setera
 */
public class PreverificationResults {
    private ClassNode preverifiedClassNode;
    private byte[] preverifiedClassBytes;
    private PreverificationError[] errors;

    /**
     * Construct a new preverication result.
     * 
     * @param preverifiedClassBytes
     * @param errors
     */
    public PreverificationResults(ClassNode preverifiedClassNode,
            byte[] preverifiedClassBytes, PreverificationError[] errors) {
        this.preverifiedClassNode = preverifiedClassNode;
        this.preverifiedClassBytes = preverifiedClassBytes;
        this.errors = errors;
    }

    /**
     * @return Returns the errors.
     */
    public PreverificationError[] getErrors() {
        return errors;
    }

    /**
     * @return Returns the preverifiedClassBytes.
     */
    public byte[] getPreverifiedClassBytes() {
        return preverifiedClassBytes;
    }

    /**
     * @return Returns the preverifiedClassNode.
     */
    public ClassNode getPreverifiedClassNode() {
        return preverifiedClassNode;
    }

    /**
     * Return the disassembled class output or <code>null</code> if there were
     * errors while doing the preverification.
     * 
     * @return
     */
    public String getDisassembledOutput() {
        String traceOutput = null;

        if (errors.length == 0) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            TraceClassVisitor traceVisitor = new TraceClassVisitor(printWriter);
            preverifiedClassNode.accept(traceVisitor);
            printWriter.close();
            traceOutput = stringWriter.toString();
        }

        return traceOutput;
    }

    /**
     * Return a boolean indicating whether the result was an error result.
     * 
     * @return
     */
    public boolean isErrorResult() {
        return errors.length > 0;
    }
}
