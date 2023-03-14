package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Parser can transform the input file content
 * into a readable format and allows the parsed data
 * to be iterated through
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 01-10-22
 */
public class Parser implements Iterable<String> {
	
	/** Space character */
	private static final char SPACE = ' '; 
	
	/** New line character */
	private static final char NEW_LINE = '\n';
	
	/** Comment begin character (Dromedary Camel, 2 Java chars) code point */
	private static final int COMMENT_BEGIN_CODE_POINT = 128042;
	
	/** Comment end character (Desert, 2 Java chars) code point */
	private static final int COMMENT_END_CODE_POINT = 127964;
	
	/**
	 * Readable data in format: "*_*_*...*_*_*"
	 * - * variable length substring (value)
	 * - _ space character (value separator)
	 */
	private final String parsedString;
	
	/** Length of the parsed data string */
	private final int parsedStringLength;
	
	/**
	 * Constructs a Parser object
	 * Parses the input file content into a readable format
	 * and stores it for later iteration through the data
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private Parser(String fileName) throws IOException {
		parsedString = getParsedString(fileName);
		parsedStringLength = parsedString.length();
	}
	
	/**
	 * Returns a Parser object representing parsed input file content
	 * 
	 * @param fileName File name.
	 * @return Parser Parser.
	 * @throws IOException If there is any problem regarding files.
	 */
	public static Parser parse(String fileName) throws IOException {
		return new Parser(fileName);
	}
	
	/**
	 * Loads input file content and transforms it
	 * into a readable format: "*_*_*...*_*_*"
	 * - * variable length substring (value)
	 * - _ space character (value separator)
	 * 
	 * @param fileName File name.
	 * @return String of values from the file in specified format
	 * @throws IOException If there is any problem regarding files.
	 */
	private String getParsedString(String fileName) throws IOException {
		String string;
		
		string = loadFileContent(fileName);
		string = eliminateComments(string);
		string = eliminateRedundantWhitespaces(string);
		
		return string;
	}
	
	/**
	 * Reads the input file into a string
	 * 
	 * @param fileName File name.
	 * @return Raw input file content.
	 * @throws IOException If there is any problem regarding files.
	 */
	private String loadFileContent(String fileName) throws IOException {
		StringBuilder builder;
		String line;
		
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
			builder = new StringBuilder();
			
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(NEW_LINE);
			}
			
			return builder.toString();
		}
	}
	
	/**
	 * Eliminates comments in the string
	 * Replaces the whole comment block with a space
	 * 
	 * @param string String
	 * @return String without comments, each comment block replaced with a space
	 */
	private String eliminateComments(String string) {
		int stringLength;
		StringBuilder builder;
		
		int nestedLevel; // times comment begin character was encountered
						 // and not resolved with particular end character
		int characterIndex;
		int codePoint;
		
		stringLength = string.length();
		builder = new StringBuilder();
		
		characterIndex = 0;
		nestedLevel = 0;
		while (characterIndex < stringLength) {
			codePoint = string.codePointAt(characterIndex);
			if (nestedLevel == 0) { // not inside comment
				if (codePoint == COMMENT_BEGIN_CODE_POINT) { // begin character encountered
					nestedLevel++;
					builder.append(SPACE); // replace the comment block with a space
					characterIndex += 2; // Camel character is represented by 2 Java chars
				}
				else { // not begin character
					builder.append(string.charAt(characterIndex));
					characterIndex++;
				}
			}
			else { // nestedLevel at least 1 = inside comment block
				if (codePoint == COMMENT_BEGIN_CODE_POINT) { // nested comment
					nestedLevel++;
					characterIndex += 2; // Camel character is represented by 2 Java chars
				}
				else if (codePoint == COMMENT_END_CODE_POINT) {
					nestedLevel--;
					characterIndex += 2; // Desert character is represented by 2 Java chars
				}
				else {
					characterIndex++; // do not append characters from inside of a comment block
				}
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Eliminates redundant whitespaces in the string
	 * Result has format: "*_*_*...*_*_*"
	 * - * variable length substring (value)
	 * - _ space character (value separator)
	 * 
	 * @param string String
	 * @return String without redundant whitespaces in specified format
	 */
	private String eliminateRedundantWhitespaces(String string) {
		int stringLength;
		StringBuilder builder;
		
		int characterIndex;
		char character;
		boolean lastCharacterIsWhitespace;
		
		stringLength = string.length();
		builder = new StringBuilder();
		
		lastCharacterIsWhitespace = false; // flag used for spaces between values
		characterIndex = 0;
		while (characterIndex < stringLength) { // eliminate whitespaces preceding any value
			if (Character.isWhitespace(string.charAt(characterIndex))) {
				characterIndex++; // skip
				continue;
			}
			break; // at characterIndex is not a whitespace
		}
		while (characterIndex < stringLength) { // continue
			character = string.charAt(characterIndex);
			
			// is character a whitespace? continue with another character
			if (Character.isWhitespace(character)) {
				lastCharacterIsWhitespace = true;
				characterIndex++;
				continue;
			}
			
			// character is not a whitespace
			// was previous character a whitespace? append a space before next character
			if (lastCharacterIsWhitespace) {
				builder.append(SPACE);
				lastCharacterIsWhitespace = false;
			}
			
			// append the character and continue with another
			builder.append(character);
			characterIndex++;
		}
		
		return builder.toString();
	}

	/**
	 * Returns an iterator of parsed data.
	 * @return Iterator, that allows to iterate
	 * through the parsed data one by one
	 */
	@Override
	public Iterator<String> iterator() {
		return new ValueIterator();
	}
	
	/**
	 * ValueIterator allows the parsed data to be iterated through one by one
	 * 
	 * @author Stanislav Kafara, Jakub Krizanovsky
	 * @version 1 01-10-22
	 */
	private class ValueIterator implements Iterator<String> {
		
		/** Character index for the parsed string */
		private int characterIndex;
		
		/**
		 * Constructs a new iterator
		 * Sets the character index to 0
		 */
		private ValueIterator() {
			characterIndex = 0;
		}

		/**
		 * Returns whether the is any more value.
		 * @return True, if there is any more value, False otherwise
		 */
		@Override
		public boolean hasNext() {
			return characterIndex < parsedStringLength;
		}

		/**
		 * Returns next value.
		 * @return Next value of the iterator
		 */
		@Override
		public String next() {
			int beginIndex;
			
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			beginIndex = characterIndex;
			while (characterIndex < parsedStringLength && // prevent StringIndexOutOfBoundsException
			       parsedString.charAt(characterIndex) != SPACE) {
				characterIndex++;
			}
			
			return parsedString.substring(beginIndex, characterIndex++);
		}
		
	}

}
