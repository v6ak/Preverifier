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
 *     Vít Šesták				- package renamed, code cleaning
 */
package v6.java.preverifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import v6.java.preverifier.results.PreverificationResults;

/**
 * A preverifier implementation for pre-verifying an archive file.
 * 
 * @author Craig Setera
 */
public class ArchivePreverifier {
    private ClassPreverifier classPreverifier;
    private IArchivePreverificationListener listener;

    /**
     * Create a new archive preverifier with the specified policy and listener.
     * 
     * @param preverificationPolicy
     * @param listener
     */
    public ArchivePreverifier(IPreverificationPolicy preverificationPolicy,
            IArchivePreverificationListener listener) {
        super();

        classPreverifier = new ClassPreverifier(preverificationPolicy);
        this.listener = (listener == null) ? new NullArchivePreverificationListener()
                : listener;
    }

    /**
     * Preverify the specified archive file using the specified classpath.
     * 
     * @param classStream
     * @param classpath
     * @return
     * @throws IOException
     */
    public void preverify(File archive, File outputFile, URL[] classpath)
            throws IOException {
        if ((archive == null) || (!archive.exists())) {
            throw new IllegalArgumentException(
                    "Archive must not be null or does not exist");
        }

        ClassLoader classLoader = new URLClassLoader(classpath, Thread
                .currentThread().getContextClassLoader());

        ZipFile zipArchive = new ZipFile(archive, ZipFile.OPEN_READ);
        ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(
                outputFile));

        try {
            // Start me up...
            listener.fileBegin(zipArchive);

            Enumeration<? extends ZipEntry> entries =  zipArchive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                handleEntry(entry, zipArchive, zipStream, classLoader);
            }

            // All done...
            listener.fileEnd(zipArchive);
        } finally {
            if (zipStream != null)
                zipStream.close();
            if (zipArchive != null)
                zipArchive.close();
        }
    }

    /**
     * Copy the non-content information about the ZipEntry.
     * 
     * @param entry
     * @return
     */
    private ZipEntry copyEntry(ZipEntry entry) {
        ZipEntry entryCopy = new ZipEntry(entry.getName());
        entryCopy.setComment(entry.getComment());
        entryCopy.setSize(entry.getSize());
        entryCopy.setTime(entry.getTime());

        return entryCopy;
    }

    /**
     * Copy the specified ZipEntry from the source to the destination.
     * 
     * @param entry
     * @param zipArchive
     * @param zipStream
     * @throws IOException
     */
    private void copyEntryAndContents(ZipEntry entry, ZipFile zipArchive,
            ZipOutputStream zipStream) throws IOException {
        InputStream is = zipArchive.getInputStream(entry);
        ZipEntry entryCopy = copyEntry(entry);
        zipStream.putNextEntry(entryCopy);
        copyInputToOutput(is, zipStream);
    }

    /**
     * Copy the contents of the input stream into the specified output stream.
     * 
     * @param is
     * @param os
     * @throws IOException
     */
    private void copyInputToOutput(InputStream is, OutputStream os)
            throws IOException {
        int bytesRead = 0;
        byte[] buffer = new byte[1024];

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Handle the specified entry in the incoming zip file.
     * 
     * @param entry
     * @param zipArchive
     * @param zipStream
     * @param classpath
     * @throws IOException
     */
    private void handleEntry(ZipEntry entry, ZipFile zipArchive,
            ZipOutputStream zipStream, ClassLoader classLoader)
            throws IOException {
        if (!entry.isDirectory()) {
            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                preverifyEntry(entry, zipArchive, zipStream, classLoader);
            } else {
                copyEntryAndContents(entry, zipArchive, zipStream);
            }
        }
    }

    /**
     * Preverify the specified class entry in the zip file.
     * 
     * @param entry
     * @param zipArchive
     * @param zipStream
     * @param classpath
     * @throws IOException
     */
    private boolean preverifyEntry(ZipEntry entry, ZipFile zipArchive,
            ZipOutputStream zipStream, ClassLoader classLoader)
            throws IOException {
        boolean keepGoing = listener.classBegin(zipArchive, entry);

        if (keepGoing) {
            InputStream is = zipArchive.getInputStream(entry);
            PreverificationResults results = classPreverifier.preverify(is,
                    classLoader);

            if (!results.isErrorResult()) {
                ZipEntry entryCopy = copyEntry(entry);
                zipStream.putNextEntry(entryCopy);
                zipStream.write(results.getPreverifiedClassBytes());
            }

            keepGoing = listener.classEnd(zipArchive, entry, results);
        }

        return keepGoing;
    }
}
