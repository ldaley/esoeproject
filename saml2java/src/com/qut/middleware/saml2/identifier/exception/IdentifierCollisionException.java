/* 
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 20/10/2006
 * 
 * Purpose: Thrown to indicate that there has been a duplicate identifier in the identifier cache. 
 */
package com.qut.middleware.saml2.identifier.exception;

/** Thrown to indicate that there has been a collision between identifiers. */
public class IdentifierCollisionException extends Exception
{
	private static final long serialVersionUID = -7989180409581930266L;
	
	/**
	 * Thrown when there has been a collision between identifiers.
	 * 
	 * @param message Human readable message indicating why this exception was thrown
	 * @param cause Any exception which caused this exception to be thrown, may be null
	 */
	public IdentifierCollisionException(String message, Exception cause)
	{
		super(message, cause);
	}
	
	/**
	 * Thrown when there has been a collision between identifiers.
	 * 
	 * @param message Human readable message indicating why this exception was thrown
	 */
	public IdentifierCollisionException(String message)
	{
		super(message);
	}
}
