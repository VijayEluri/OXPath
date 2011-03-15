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
package uk.ac.ox.comlab.oxpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class used to facilitate the navigation of OXPath additional axes
 * @author AndrewJSel
 *
 */
public class AdditionalAxisNavigator {

	/**
	 * Constructor that builds an AdditionalAxisNavigator object out of an additional axis token of a navigational path
	 * @param token additional axis token
	 */
	public AdditionalAxisNavigator(String token) throws BadDataException{
		//utilize pattern matching
		AddAxisTokenPattern tPattern = null;
		for (AddAxisTokenPattern pat : AddAxisTokenPattern.values()) {
			if (Pattern.matches(pat.getValue(), token)) {
				tPattern = pat;
//				System.out.println(tPattern);
			}
		}
		if (tPattern==null) throw new BadDataException("Malformed Additional Axis token in the Navigational Path!");
		
		/*eliminate all predicate attributes; 
		 * not great programming by creating more string, but this feature was added later*/
		while (token.contains(BEGINATTPRED)) {//pattern matching above assures that the following constraints hold
			int beginAttPred = token.indexOf(BEGINATTPRED);
			int equalAttPred = token.indexOf(EQUALATTPRED,beginAttPred);
			int endAttPred = token.indexOf(ENDATTPRED,equalAttPred);
			//+1 and -1 to consume the ' quote in the predicates
			this.attributePredicates.put(token.substring(beginAttPred+BEGINATTPRED.length(),equalAttPred),token.substring(equalAttPred+EQUALATTPRED.length()+1,endAttPred-1));
			if (endAttPred>=token.length()-1) {//if the predicate is at the end of the 
				token = token.substring(0,beginAttPred);
			}
			else {
				token = token.substring(0,beginAttPred) + token.substring(endAttPred+ENDATTPRED.length());
			}
		}
		
		//get specific additional axis, pattern matching assures only one can be present because of :: characters
		for (AdditionalAxes a : AdditionalAxes.values()) {
			if (token.contains(a.getValue())) {
				axis = a;
			}
		}
		if (axis==null) {
			throw new BadDataException("Malformed Additional Axis token in the Navigational Path!");
		}
		
		//get node type
		if (tPattern==AddAxisTokenPattern.P4) {
			nodeType = token.substring(token.indexOf(AXISID)+AXISID.length());
		}
		else {
			int endNodeType = -1;
			if ((tPattern==AddAxisTokenPattern.P1)||(tPattern==AddAxisTokenPattern.P2)) {
				endNodeType = token.indexOf(')');
			}
			else if (tPattern==AddAxisTokenPattern.P3) {
				endNodeType = token.indexOf('[');
			}
			nodeType = token.substring((token.indexOf(AXISID)+AXISID.length()), endNodeType);
		}
		
		//get offset
		if ((tPattern==AddAxisTokenPattern.P1)||(tPattern==AddAxisTokenPattern.P3)) {
			this.offset = Integer.parseInt(token.substring(token.indexOf("[")+ "[".length(), token.indexOf(']')));
		}
		else {
			this.offset = 1;//XPath counting starts at 1, so this is the first result (no offset)
		}
	}
	
	/**
	 * method that returns the implicit parameter's axis type
	 * @return AdditionalAxes value corresponding to axis type
	 */
	public AdditionalAxes getAxisType() {
		return this.axis;
	}
	
	/**
	 * method that returns the implicit parameter's node type
	 * @return node type as a String
	 */
	public String getNodeType() {
		return this.nodeType;
	}
	
	/**
	 * method that returns the offset axis navigation (determined by axis predicate)
	 * @return offset as int primitive
	 */
	public int getOffset() {
		return this.offset;
	}
	
	/**
	 * method for returning the keys of the attribute predicates mapping
	 * @return set of keys for the attributes stored in the predicates of the additional axis
	 */
	public Set<String> getPredicateAttributeKeys() {
		return new HashSet<String>(this.attributePredicates.keySet());//creates new object so state info isn't mutable
	}
	
	/**
	 * method for returning the value of an attribute
	 * @param key the name of the attribute to return the value of
	 * @return the value of the attribute
	 */
	public String getPredicateAttributeValue(String key) {
		return this.attributePredicates.get(key);
	}
	
	/**
	 * constant for the axis identifier
	 */
	public final static String AXISID = "::";
	
	/**
	 * constant for additional axis wildcard (same as standard XPath axis)
	 */
	public final static String WILDCARD = "*";
	
	/**
	 * constant for starting an attribute predicate
	 */
	public final static String BEGINATTPRED = "[@";
	
	/**
	 * constant for tokenizing in an attribute predicate
	 */
	public final static String EQUALATTPRED = "=";
	
	/**
	 * constant for ending an attribute predicate
	 */
	public final static String ENDATTPRED = "]";
	
	//instance fields for additional axis navigations, the axis type 
	private AdditionalAxes axis;
	private String nodeType;
	private int offset;
	private Map<String,String> attributePredicates = new HashMap<String,String>();
	
	/**
	 * utilize enum for possible patterns of AdditionalAxisTokens
	 */
	public enum AddAxisTokenPattern {
		P1("/\\(\\w+-\\w+::(\\w+|\\*)\\)(\\[@\\w+='\\w+'\\])*\\[[0-9]+\\]"),
		P2("/\\(\\w+-\\w+::(\\w+|\\*)\\)(\\[@\\w+='\\w+'\\])*"),
		P3("/\\w+-\\w+::(\\w+|\\*)(\\[@\\w+='\\w+'\\])*\\[[0-9]+\\]"),
		P4("/\\w+-\\w+::(\\w+|\\*)(\\[@\\w+='\\w+'\\])*");
	
		/**
		 * constructor for TokenPattern values
		 * @param aValue String value of pattern
		 */
		private AddAxisTokenPattern(String aValue) {
			this.value = aValue;
		}
		
		/**
		 * method that returns the pattern value of the enum object
		 * @return pattern value to the corresponding enum object (as a String)
		 */
		public String getValue() {
			return this.value;
		}
		
		//instance field storing pattern value
		private String value;
	}

}
