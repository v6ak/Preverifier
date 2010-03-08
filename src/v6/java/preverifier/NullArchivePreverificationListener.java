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
package v6.java.preverifier;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import v6.java.preverifier.results.PreverificationResults;

/**
 * Null implementation of the IArchivePreverificationListener implementation.
 * 
 * @author Craig Setera
 */
public class NullArchivePreverificationListener implements
        IArchivePreverificationListener {
    /**
     * @see v6.java.preverifier.IArchivePreverificationListener#fileBegin(java.util.zip.ZipFile)
     */
    public void fileBegin(ZipFile archive) {
    }

    /**
     * @see v6.java.preverifier.IArchivePreverificationListener#classBegin(java.util.zip.ZipFile,
     *      java.util.zip.ZipEntry)
     */
    public boolean classBegin(ZipFile archive, ZipEntry classEntry) {
        return true;
    }

    /**
     * @see v6.java.preverifier.IArchivePreverificationListener#classEnd(java.util.zip.ZipFile,
     *      java.util.zip.ZipEntry,
     *      v6.java.preverifier.results.PreverificationResults)
     */
    public boolean classEnd(ZipFile archive, ZipEntry classEntry,
            PreverificationResults results) {
        return true;
    }

    /**
     * @see v6.java.preverifier.IArchivePreverificationListener#fileEnd(java.util.zip.ZipFile)
     */
    public void fileEnd(ZipFile archive) {
    }
}
