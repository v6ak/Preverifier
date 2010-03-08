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
 * An interface that clients may provide when pre-verifying archive files to
 * receive callbacks concerning preverification processing.
 * 
 * @author Craig Setera
 */
public interface IArchivePreverificationListener {
    
    /**
     * Preverification is beginning on the specified archive file.
     * 
     * @param archive
     */
    void fileBegin(ZipFile archive);

    /**
     * Preverification is beginning on the specified entry in the specified
     * archive file. The listener must return a boolean indicating whether to
     * continue with the preverification processing.
     * 
     * @param archive
     * @param classEntry
     * @return
     */
    boolean classBegin(ZipFile archive, ZipEntry classEntry);

    /**
     * Preverification is ending on the specified entry in the specified archive
     * file. The listener must return a boolean indicating whether to continue
     * with the preverification processing.
     * 
     * @param archive
     * @param classEntry
     * @return
     */
    boolean classEnd(ZipFile archive, ZipEntry classEntry,
            PreverificationResults results);

    /**
     * Preverification is ending on the specified archive file.
     * 
     * @param archive
     */
    void fileEnd(ZipFile archive);
}
