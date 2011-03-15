/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package uk.ac.ox.comlab.oxpath.oxpathTreeWalker;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;

import uk.ac.ox.comlab.oxpath.BadDataException;

import static uk.ac.ox.comlab.oxpath.oxpathTreeWalker.OXPathType.OXPathTypes.*;

/**
 * Class for encoding OXPath return types, including nodesets, strings, numbers, and booleans
 * @author AndrewJSel
 *
 */
public class OXPathType {
	
	/**
	 * Null constructor
	 */
	public OXPathType() {
		this.type = NULL;
	}
	
	/**
	 * Constructor for nodelists
	 * @param in input nodelist
	 */
	public OXPathType(OXPathNodeList<OXPathDomNode> in) {
		this.set(in);
	}
	
	/**
	 * Constructor for strings
	 * @param in input String
	 */
	public OXPathType(String in) {
		this.set(in);
	}
	
	/**
	 * Constructor for number inputs
	 * @param in input int
	 */
	public OXPathType(double in) {
		this.set(in);
	}
	
	/**
	 * Constructor for booleans
	 * @param in input boolean
	 */
	public OXPathType(boolean in) {
		this.set(in);
	}

	/**
	 * Constructor for handling output from the getByXPath function from HtmlUnit
	 * @param byXPath input of List<?> from getByXPath
	 */
	public OXPathType(List<?> byXPath) {
		Object first = byXPath.get(0);
		if (first instanceof DomNode) {
			this.nodes.addList(byXPath);
			this.type=NODESET;
		}
		else if (first instanceof String) {
			this.string = (String) first;
			this.type = STRING;
		}
		else if (first instanceof Double) {
			this.number = (Double) first;
			this.type = NUMBER;
		}
		else if (first instanceof Boolean) {
			this.bool = (Boolean) first;
			this.type = BOOLEAN;
		}
		else {
			this.type = NULL;
		}
	}

	/**
	  * Constructor for handling output from the getByXPath function from HtmlUnit
	 * @param byXPath input of List<?> from getByXPath
	 * @param parent reference to parent node of current context
	 * @param last reference to parent node of current context
	 */
	public OXPathType(List<?> byXPath, int parent, int last) {
		if (byXPath.isEmpty()) this.type = NULL;
		else {
			Object first = byXPath.get(0);
			if (first instanceof DomNode) {
				this.nodes = new OXPathNodeList<OXPathDomNode>();
				for (Object n : byXPath) {
					this.nodes.add(new OXPathDomNode((DomNode)n,parent,last));
				}
				this.type=NODESET;
			}
			else if (first instanceof String) {
				this.string = (String) first;
				this.type = STRING;
			}
			else if (first instanceof Double) {
				this.number = (Double) first;
				this.type = NUMBER;
			}
			else if (first instanceof Boolean) {
				this.bool = (Boolean) first;
				this.type = BOOLEAN;
			}
			else {
				this.type = NULL;
			}
		}
	}

	/**
	 * Expression for setting state
	 * @param j input NodeList
	 */
	public OXPathType(OXPathDomNode j) {
		this.nodes = new OXPathNodeList<OXPathDomNode>();
		this.nodes.add(j);
		this.type = NODESET;
	}

	/**
	 * Expression for setting state
	 * @param in input NodeList
	 */
	public void set(OXPathNodeList<OXPathDomNode> in) {
		this.nodes = in;
		this.type = NODESET;
	}
	
	/**
	 * Expression for setting state
	 * @param in input String
	 */
	public void set(String in) {
		this.string = in;
		this.type = STRING;
	}
	
	/**
	 * Expression for setting state
	 * @param in input int
	 */
	public void set(Double in) {
		this.number = in;
		this.type = NUMBER;
	}
	
	/**
	 * Expression for setting state
	 * @param in input boolean
	 */
	public void set(boolean in) {
		this.bool = in;
		this.type = BOOLEAN;
	}
	
	/**
	 * Expression for setting null state	
	 */
	public void set(Object in) {
		this.type = NULL;
	}
	
	/**
	 * Expression that returns type of Object
	 * @return type of implicit parameter
	 */
	public OXPathTypes isType() {
		return this.type;
	}
	
	/**
	 * Casts object as <tt>OXPathNodeList</tt>
	 * @return object as <tt>OXPathNodeList</tt>
	 * @throws BadDataException
	 */
	public OXPathNodeList<OXPathDomNode> nodeList() throws BadDataException {
		if (this.type.equals(NODESET)) return this.nodes;
		else return new OXPathNodeList<OXPathDomNode>();
//		else throw new BadDataException("OXPathType exception - Can't cast " + this.type.toString() + " as " + NODESET.toString());
	}
	
	/**
	 * Casts object as <tt>String</tt>
	 * @return object as <tt>String</tt>
	 * @throws BadDataException
	 */
	public String string() throws BadDataException {
		if (this.type.equals(STRING)) return this.string;
		else if (this.type.equals(NODESET)) {
			if (this.nodes.isEmpty()) return "";
			else return (String) this.nodes.get(0).getNode().getFirstByXPath("string(.)");
		}
		else if (this.type.equals(BOOLEAN)) return (this.bool) ? "true" : "false";
		else if (this.type.equals(NUMBER)) return String.valueOf(this.number);
		else throw new BadDataException("OXPathType exception - Can't cast " + this.type.toString() + " as " + STRING.toString());
	}
	
	/**
	 * Casts object as <tt>double</tt>
	 * @return object as <tt>double</tt>
	 * @throws BadDataException
	 */
	public Double number() throws BadDataException {
		if (this.type.equals(NUMBER)) return this.number;
		else if (this.type.equals(STRING)) {
//			System.out.println(this.string);
			if (this.string().equals("false")) return 0.0;
			else if (this.string().equals("true")) return 1.0;
			else return Double.valueOf(this.string);
		}
		else if (this.type.equals(BOOLEAN)) return (this.bool) ? 1.0 : 0.0;
		else if (this.type.equals(NODESET)) return Double.valueOf((String) this.nodes.get(0).getNode().getFirstByXPath("string(.)"));
		else throw new BadDataException("OXPathType exception - Can't cast " + this.type.toString() + " as " + NUMBER.toString());
	}
	
	/**
	 * Casts object as <tt>boolean</tt>
	 * @return object as <tt>boolean</tt>
	 * @throws BadDataException
	 */
	public boolean booleanValue() throws BadDataException {
		if (this.type.equals(BOOLEAN)) return this.bool;
		else if (this.type.equals(NODESET)) return (this.nodes.getLength()>0)?true:false;
		else if (this.type.equals(STRING)) return (this.string.length() > 0);
		else if (this.type.equals(NUMBER)) return !this.number.equals(0);
		else throw new BadDataException("OXPathType exception - Can't cast " + this.type.toString() + " as " + NODESET.toString());
	}
	
	/**
	 * Not class-safe, but returns value based on instantiation of type in the object
	 * @return value of object
	 */
	public Object getValue() {
		switch (this.type) {
			case NODESET :
				return this.nodes;
			case STRING :
				return this.string;
			case NUMBER :
				return this.number;
			case BOOLEAN :
				return this.bool;
//			case NULL :
//				return null;
		}
		return null;
	}
	
	/**
	 * enumerated types of different types in OXPath
	 * @author AndrewJSel
	 *
	 */
	public enum OXPathTypes {
		NODESET("node-set"), STRING("string"), NUMBER("number"), BOOLEAN("boolean"), NULL("null");
		OXPathTypes(String in) {this.name=in;}
		public String toString() {return this.name;}
		private String name;
	}
	
	/**
	 * Instance field for data type
	 */
	private OXPathTypes type;
	
	/**
	 * Instance field for storing field, if used
	 */
	private OXPathNodeList<OXPathDomNode> nodes;
	
	/**
	 * Instance field for storing field, if used
	 */
	private String string;
	
	/**
	 * Instance field for storing field, if used
	 */
	private Double number;
	
	/**
	 * Instance field for storing field, if used
	 */
	private boolean bool;
}
