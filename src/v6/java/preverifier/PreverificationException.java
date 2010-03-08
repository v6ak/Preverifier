/**
 * Copyright (c) 2010 Vít Šesták
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Vít Šesták				- initial implementation
 */
package v6.java.preverifier;

import lombok.Getter;
import v6.java.preverifier.results.PreverificationResults;

/**
 * Signals that something is not correct in preverification process.
 * @author Vít Šesták AKA v6ak
 *
 */
@SuppressWarnings("serial")
public class PreverificationException extends Exception {

	@Getter
	private final PreverificationResults preverificationResults;
	
	/**
	 * @param preverificationResults results that contains preverification errors.
	 * @throws NullPointerException if preverificationResults is null
	 */
	PreverificationException(PreverificationResults preverificationResults) {
		super("Some errors are in preverification");
		if (preverificationResults == null) {
			throw new NullPointerException("preverificationResults must not be null");
		}
		this.preverificationResults = preverificationResults;
	}
	
}
