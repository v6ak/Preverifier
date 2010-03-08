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
 *     Diego Sandin (Motorola)  - Fix errors after updating ASM library form 
 *                                version 2.2.2 to 3.0.0
 *     Vít Šesták				- ASM classloader problem fix
 *     							- preverifyStream added
 * 								- package renamed
 * 								- code cleaned
 */
package v6.java.preverifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import v6.enhancements.asm.ClassWriterEnhancement;
import v6.java.preverifier.results.PreverificationError;
import v6.java.preverifier.results.PreverificationResults;

/**
 * A class preverifier implementation
 * 
 * @author Craig Setera
 */
public class ClassPreverifier {

    /**
     * Flag used in ClassWriter when no default flags are required
     */
    private static final int INVALID_FLAG = -1;

    private IPreverificationPolicy preverificationPolicy;

    /**
     * Construct a new preverifier that uses the specified policy during
     * preverification processing.
     * 
     * @param preverificationPolicy
     */
    public ClassPreverifier(IPreverificationPolicy preverificationPolicy) {
        this.preverificationPolicy = preverificationPolicy;
    }

    /**
     * Preverify the specified class file.
     * 
     * @param classStream
     * @param classloader
     * @return
     * @throws IOException
     */
    public PreverificationResults preverify(InputStream classStream,
            URL[] classpath) throws IOException {
        ClassLoader classLoader = new URLClassLoader(classpath, Thread
                .currentThread().getContextClassLoader());
        return preverify(classStream, classLoader);
    }

    /**
     * Preverifies stream. Differences between this method and {@link #preverify(InputStream, ClassLoader)}:
     * <ul>
     * 	<li>It throws {@link PreverificationException} in case of preverification problem.
     * 	<li>Preverified class is returned as {@link InputStream}.
     * </ul>
     * @param classStream
     * @param classLoader
     * @return
     * @throws IOException
     * @throws PreverificationException
     */
    public InputStream preverifyStream(InputStream classStream, ClassLoader classLoader) throws IOException, PreverificationException{
    	final PreverificationResults results = preverify(classStream, classLoader);
    	if(results.isErrorResult()){
    		throw new PreverificationException(results);
    	}else{
    		return new ByteArrayInputStream(results.getPreverifiedClassBytes());
    	}
    }
    
    /**
     * Preverify the specified class file.
     * 
     * @param classStream
     * @param classloader
     * @return
     * @throws IOException
     */
    public PreverificationResults preverify(InputStream classStream,
            final ClassLoader classloader) throws IOException {
        if (classStream == null) {
            throw new IllegalArgumentException(
                    "Class byte stream must not be null");
        }

        PreverificationClassNode classNode = new PreverificationClassNode(
                preverificationPolicy, classloader);

        // Do the visitation
        ClassReader classReader = new ClassReader(classStream);

        /* Don't skip the debug information in the class*/
        classReader.accept(classNode, 0);
        classStream.close();

        // Collect the errors
        List<?> errorList = classNode.getErrorList();
        PreverificationError[] errorArray = errorList
                .toArray(new PreverificationError[errorList.size()]);

        // Generate the results object
        PreverificationResults results = null;
        if (errorArray.length > 0) {
            results = new PreverificationResults(null, null, errorArray);
        } else {
            // The preverification writer does the real work
            ClassWriter classWriter = ClassWriterEnhancement.createClassWriter(INVALID_FLAG, classloader);
            classNode.accept(classWriter);

            byte[] bytecode = classWriter.toByteArray();
            results = new PreverificationResults(classNode, bytecode,
                    errorArray);
        }

        return results;
    }
}
