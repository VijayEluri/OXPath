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

//import java.util.List;
//import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import uk.ac.ox.comlab.oxpath.scriptParser.SimpleNode;

import Examples.impl.OutputProviderImpl;
import Examples.intf.OutputProvider;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class used to represent GroundedOXPathExpression objects
 * 
 * @author AndrewJSel
 * 
 */
public class GroundedOXPathExpression extends OXPathExpression {

	/**
	 * Creates a Grounded OXPathExpression from an UnGroundedOXPathExpression and an instantiated path
	 * 
	 * @param xp
	 *            the UnGroundedOXPathExpression that will be the source of this new OXPathExpression
	 * @param path
	 *            instantiated path
	 */
	public GroundedOXPathExpression(UnGroundedOXPathExpression xp, String path) {
		super();
		// database has been loaded, no longer needed
		this.setDatabaseUsed(false);
		this.setDatabaseName("");

		// path is determined from instantiated path
		this.setPathName(path);

		// other variables are transferred over, but any mutable objects are broken down until (immutable) String or boolean copies are made
		this.setURLName(xp.getURLName());
		this.setNextUsed(xp.getNextUsed());
		this.addNextLinkNames(xp.getNextLinkNames());
		for (String key : xp.getScrapedInfoKeys()) {
			this.addScrapedInfo(key, xp.getScrapedInfoPath(key));
		}
	}

	/**
	 * Method that executes the grounded query described by the OXPath path and target host encoded within the implicit parameter
	 * 
	 * @return a WebPage object representing the resulting page, for use with SAXON engine via XQueryScraper
	 * @throws FileNotFoundException
	 * @throws IOException
	 *             potentially thrown on webClient.getPage(X) or WebPageImpl.createWebPage(X) calls
	 * @throws XPathExpressionException
	 *             can be thrown if host delivers malformed webpage
	 * @throws BadDataException
	 *             in case of malformed navigation path expression
	 */
	public HtmlPage getResultFromWebQuery() throws IOException, XPathExpressionException, BadDataException {
		// use the HTMLUnit WebClient to access the host of interest, run nav path to fill out web form and submit, printing result page to file
		// TEST
		// webClient.setThrowExceptionOnScriptError(false);
		webClient.setThrowExceptionOnFailingStatusCode(false);
		// Get the first page of the query
		final HtmlPage page = webClient.getPage(this.getURLName()); // possible IOException
		// Use XPathHelper class to return the resulting htmlpage that comes after submitting the query
		XPathHelper helper = new XPathHelper();
		HtmlPage resultPage = helper.getQueryResult(page, this.getPathName());
		attributes.putAll(helper.getXQueryAttributes());
		attributes.put("webFormURL", this.getURLName());// add domain name to attribute by default
		return resultPage;// leave WebClient open
		/*
		 * legacy code from when scraping and next link navigation were handled by SAXON String resultAsString = resultPage.asXml(); // System.out.println(resultAsString); webClient.closeAllWindows(); //create new webpage (in SAXON implementation) for use with XQueryScraper, powered by the SAXON engine //return WebPageImpl.createWebPage(new ByteArrayInputStream(resultAsString.getBytes())); //possible IOException
		 */

	}

	/**
	 * Method for returning the results of a XQueryScrape from a results page (or pages)
	 * 
	 * @param page
	 *            the first page containing results
	 * @return results, formatted as specified in the XQ portion of the implicit parameter
	 * @throws Exception
	 *             raised in creation of web page
	 * @throws IOException
	 *             in case of missing HTML page
	 */
	public OutputProvider scrapeByXQuery(HtmlPage page) throws IOException, Exception {
		/*
		 * construct XQuery Factory; construct a temporary holder for the scraping paths (w/o the next link) to pass to the query constructorso that the GroundedOXPath object remains immutable
		 */
		Map<String, SimpleNode> tempPaths = new HashMap<String, SimpleNode>();
		ArrayList<String> tempNexts = new ArrayList<String>();
		tempNexts.addAll(this.getNextLinkNames());// to keep the objects immutable
		for (String key : this.getScrapedInfoKeys()) {// acquire all the paths needed for the scraping query
			if (!this.getNextUsed() || !tempNexts.contains(key)) {
				tempPaths.put(key, this.getScrapedInfoPath(key));
			}
		}
		//TODO: Fix
//		ScraperXQueryFactory scraperQuery = new ScraperXQueryFactory(attributes.keySet(), tempPaths);// build the scraping query
		// populate a new list with the paths for the ArrayList names
		ArrayList<String> tempNextPaths = new ArrayList<String>();
		for (String name : tempNexts) {
			if (!this.getNextLinkNames().contains(name))
				throw new BadDataException("Referencing " + name + " without providing a path.");
			// System.out.println(name);
			//TODO: Fix
//			tempNextPaths.add(this.getScrapedInfoPath(name));
		}
		NextNavigator nextNodePaths = new NextNavigator(page, tempNextPaths);
		String output = "";
		// scrap the current page
		// output += XQueryHelper.scrapWebPage(XQueryHelper.convertHtmlPageToWebPage(page),scraperQuery,this.attributes);
		// scrap the progress next pages
		// if (this.getNextUsed()) {
		//TODO: Fix
//		output += XQueryHelper.navigateNextAndScrapWebPage(page, scraperQuery, this.attributes, nextNodePaths);
		// }

		OutputProvider out = new OutputProviderImpl(output);
		return out;

		// Legacy code from when SAXON queries handled next link
		// if (this.getNextUsed()) {//scrap the webpage by calling the appropriate helper function
		// String nextLinkPath = this.getScrapedInfoPath(this.getNextLink());
		// if (nextLinkPath==null) {
		// throw new BadDataException("Next link path specification not found as required by token following while in <XQ while >!");
		// }
		// NextXQueryFactory nextLink = new NextXQueryFactory(this.getNextLink(),nextLinkPath);
		// output += XQueryHelper.scrapWebPage(page, scraperQuery, nextLink, this.attributes, this.getURLName());
		// }
		// else {
		// output += XQueryHelper.scrapWebPage(page,scraperQuery, this.attributes);
		// }

	}

	/**
	 * method for setting navigation paths
	 * 
	 * @param aPathName
	 *            navigation path
	 */
	public void setPathName(String aPathName) {
		this.pathName = aPathName;
	}

	/**
	 * method for retrieving navigation paths
	 * 
	 * @return navigational paths through URL
	 */
	public String getPathName() {
		return this.pathName;
	}

	/**
	 * method for closing a GroundedOXPath object so as to cleanly close any open web pages associated with the object
	 */
	public void close() {
		this.webClient.closeAllWindows();
	}

	/**
	 * overridden method used for converting an UnGroundedOXPathExpression object to a String object with state info for testing purposes
	 * 
	 * @return String object with state info
	 */
	@Override
	public String toString() {
		String out = " [path ==> " + this.getPathName() + "]\n";
		return this.toString(out);
	}

	/**
	 * instance field for navigation path of grounded OXPathExpression (grounded), stored as a String for use with standard XPath class
	 */
	private String pathName;

	/**
	 * private field storing attribute info for Query result attributes
	 */
	Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * WebClient object associated with the grounded OXPression for navigating a query and scraping the underlying result page(s)
	 */
	final WebClient webClient = new WebClient();
}
