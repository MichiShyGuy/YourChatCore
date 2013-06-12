package de.deyovi.chat.core.services.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.deyovi.chat.core.services.TranslatorService;

public class ResourceTranslatorService implements TranslatorService {

	private final static Logger logger = LogManager.getLogger(ResourceTranslatorService.class);

	private final static Map<Locale, ResourceBundle> lang2resource = new HashMap<Locale, ResourceBundle>();
	
	private final static ResourceTranslatorService instance = new ResourceTranslatorService();
	
	private ResourceTranslatorService() {
		// hitdden
	}
	
	public static ResourceTranslatorService getInstance() {
		return instance;
	}
	
	private static ResourceBundle getResource(Locale locale) {
		ResourceBundle instance = lang2resource.get(locale);
		if (instance == null) {
			instance = createInstance(locale);
		}
		return instance;
	}
	
	private static synchronized ResourceBundle createInstance(Locale locale) {
		ResourceBundle instance = lang2resource.get(locale);
		if (instance == null) {
			instance = ResourceBundle.getBundle("MessageBundle", locale);
			lang2resource.put(locale, instance);
		}
		return instance;
	}

	private static List<String> parse(String input) {
		LinkedList<String> result = new LinkedList<String>();
		String main = null;
		StringTokenizer tokenizer = new StringTokenizer(input, "{");
		while (tokenizer.hasMoreTokens()) {
			String paramString = tokenizer.nextToken();
			if (main == null) {
				main = paramString;
			} else {
				paramString = paramString.substring(0, paramString.length() - 1);
				int ixOfequals = paramString.indexOf('=');
				if (ixOfequals == -1) {
					result.add(null);
				} else {
					String unescaped = StringEscapeUtils.unescapeHtml4(paramString.substring(ixOfequals + 1));
					result.add(unescaped);
				}
			}
		}
		// Keine Parameter da gewesen, dann vollen String als Kommando nutzen
		result.addFirst(main);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see de.yovi.chat.client.TranslatorService#translate(java.lang.String, java.lang.String)
	 */
	@Override
	public String translate(String rawMessage, Locale locale) {
		ResourceBundle messages = getResource(locale);
		// parse the input
		List<String> arguments = parse(rawMessage);
		// remove the first element (it's the message-id)
		String message = messages.getString(arguments.remove(0));
		// create a formatter with our message
		MessageFormat formatter = new MessageFormat(message, messages.getLocale());
		// and pass our arguments to the function
		String output = formatter.format(arguments.toArray());
		// let's restore NewLines, if some where in the Message-Pattern
		output = StringEscapeUtils.unescapeJava(output);
		if (logger.isDebugEnabled()) {
			logger.debug("translated '" + rawMessage + "' to '" + output + "'");
		}
		return output;
	}
	
}
