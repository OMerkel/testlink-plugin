/* 
 * The MIT License
 * 
 * Copyright (c) 2010 Bruno P. Kinoshita <http://www.kinoshita.eti.br>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.testlink.result;

import hudson.Util;
import hudson.model.BuildListener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import br.eti.kinoshita.testlinkjavaapi.model.CustomField;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;

/**
 * Seeks for Test Results.
 *
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 2.2
 */
public abstract class TestResultSeeker<T>
implements Serializable
{

	private static final long serialVersionUID = 6476036912489515690L;
	
	protected final String includePattern;
	protected final TestCase[] automatedTestCases;
	protected final String keyCustomFieldName;
	protected final BuildListener listener;
	
	/**
	 * Default constructor.
	 * 
	 * @param includePattern Include pattern.
	 * @param report TestLink report.
	 * @param keyCustomFieldName Name of the Key Custom Field.
	 * @param listener Hudson Build listener.
	 */
	public TestResultSeeker( 
			String includePattern, 
		TestCase[] automatedTestCases, 
		String keyCustomFieldName, 
		BuildListener listener)
	{
		super();
		
		this.includePattern = includePattern;
		this.automatedTestCases = automatedTestCases;
		this.keyCustomFieldName = keyCustomFieldName;
		this.listener = listener;
	}
	
	/**
	 * Seeks for Test Results in a directory. It tries to match the 
	 * includePattern with files in this directory.
	 * 
	 * @param directory Directory to look for test results
	 * @throws TestResultSeekerException
	 */
	public abstract Map<Integer, TestCaseWrapper<T>> seek( 
			File directory )
	throws TestResultSeekerException;
	
	/**
	 * Retrieves the file content encoded in Base64.
	 * 
	 * @param file file to read the content.
	 * @return file content encoded in Base64.
	 * @throws IOException 
	 */
	protected String getBase64FileContent( File file ) 
	throws IOException
	{
		byte[] fileData = FileUtils.readFileToByteArray(file);
		return Base64.encodeBase64String( fileData );
	}
	
	/**
	 * Splits a String by comma and gets an array of Strings.
	 */
	protected String[] split( String input )
	{
		if ( StringUtils.isBlank( input ) )
		{
			return new String[0];
		}
		
		StringTokenizer tokenizer = new StringTokenizer( input, ",");
		
		String[] values = new String[ tokenizer.countTokens() ];
		
		for( int i = 0 ; tokenizer.hasMoreTokens() ; i++ )
		{
			values[i] = tokenizer.nextToken().trim();
		}
		
		return values;		
	}
	
	/**
	 * Scans a directory for files matching the includes pattern.
	 * 
	 * @param directory the directory to scan.
	 * @param includes the includes pattern.
	 * @param listener Hudson Build listener.
	 * @return array of strings of paths for files that match the includes pattern in the directory.
	 * @throws IOException
	 */
	protected String[] scan( 
		final File directory, 
		final String includes, 
		final BuildListener listener 
	) 
	throws IOException
	{
		
		String[] fileNames = new String[0];
		
		if ( StringUtils.isNotBlank( includes ) )
		{
			FileSet fs = null;
			
			try
			{
				fs = Util.createFileSet( directory, includes );
				
				DirectoryScanner ds = fs.getDirectoryScanner();
				fileNames = ds.getIncludedFiles();
			}
			catch ( BuildException e ) 
			{
				e.printStackTrace( listener.getLogger() );
				throw new IOException( e );
			}
		}
		
		return fileNames;
		
	}
	
	/**
	 * Gets the key custom field out of a list using the key custom field name.
	 */
	protected CustomField getKeyCustomField( List<CustomField> customFields )
	{
		CustomField customField = null;
		 
		for ( CustomField cf : customFields )
		{
			boolean isKeyCustomField = cf.getName().equals(keyCustomFieldName);
			
			if ( isKeyCustomField )
			{
				customField = cf;
				break;
			}
			
		}
		return customField;
	}
	
}
