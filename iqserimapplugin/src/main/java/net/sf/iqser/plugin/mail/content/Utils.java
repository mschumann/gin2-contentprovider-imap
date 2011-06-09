package net.sf.iqser.plugin.mail.content;

import java.io.ByteArrayInputStream;

import org.apache.commons.lang.StringUtils;

import net.sf.iqser.plugin.file.parser.FileParser;
import net.sf.iqser.plugin.file.parser.FileParserException;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
/**
 * Utility class for MailServer content provider. 
 * It is used as a an adapter of the FileParser module version 1.2 . 
 * 
 * @author robert.baban
 *
 */
public class Utils {

	/**
	 * Parses an file using the given parser.
	 * 
	 * Cleans attributes according to version 1.4 GIN platform by removing empty or null attributes, 
	 * adding attribute type.
	 * 
	 * @param fileName the file name.
	 * @param parser the file parser.
	 * @param byteArray the binary content of the file.
	 * @return a {@link Content} object.
	 * @throws FileParserException if a parsing exception occurs.
	 */
	public static Content parseFileContent(String fileName, FileParser parser, byte[] byteArray) throws FileParserException{
				
		Content content = parser.getContent(fileName, new ByteArrayInputStream(byteArray));
		
		//remove null or empty attributes
		Content newContent = new Content();
		newContent.setContentUrl(content.getContentUrl());
		newContent.setFulltext(content.getFulltext());
		newContent.setIcon(content.getIcon());
		newContent.setModificationDate(content.getModificationDate());
		newContent.setProvider(content.getProvider());
		newContent.setType(content.getType());
		for (Attribute a : content.getAttributes()) {
			if (! attributeHasNullorEmptyValue(a)){
				
				if (a.getType() == -1){
					a.setType(Attribute.ATTRIBUTE_TYPE_TEXT);
				}
				
				newContent.addAttribute(a);
			}				
			
		}
		
		return newContent;
	}
	
	private static boolean attributeHasNullorEmptyValue(Attribute a){
		if (a.isMultiValue()){
			if (a.getValues()==null || a.getValues().isEmpty()) return true;
		}else{
			if (StringUtils.isEmpty(a.getValue())) return true;
		}
		return false;
	}
}
