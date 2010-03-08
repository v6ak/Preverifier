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

import org.objectweb.asm.tree.MethodNode;

/**
 * Implementation of the IMethodErrorInformation interface wrapped around a
 * method node.
 * 
 * @author Craig Setera
 */
public class MethodNodeErrorInformation implements IMethodErrorInformation {
    
    private IClassErrorInformation classErrorInformation;
    private MethodNode methodNode;

    /**
     * Construct a new instance of method node error information.
     * 
     * @param classErrorInformation
     * @param methodNode
     */
    public MethodNodeErrorInformation(
            IClassErrorInformation classErrorInformation, MethodNode methodNode) {
        super();

        this.classErrorInformation = classErrorInformation;
        this.methodNode = methodNode;
    }

    /**
     * @see v6.java.preverifier.results.IMethodErrorInformation#getName()
     */
    public String getName() {
        return methodNode.name;
    }

    /**
     * @see v6.java.preverifier.results.IMethodErrorInformation#getTypeDescription()
     */
    public String getTypeDescription() {
        return methodNode.desc;
    }

    /**
     * @see v6.java.preverifier.results.IMethodErrorInformation#getClassInformation()
     */
    public IClassErrorInformation getClassInformation() {
        return classErrorInformation;
    }
}
