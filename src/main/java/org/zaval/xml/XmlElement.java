/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
 * Copyright (C) 2019 Christoph Obexer <cobexer@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * (version 2) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.zaval.xml;

/*
 * This code based upon NanoXML 2.2 sources
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XmlElement {
	private final Map<String, String> attributes = new HashMap<>();
	private final List<XmlElement> children = new ArrayList<>();
	private String name;
	private String contents;
	private final Map<String, char[]> entities;

	private final boolean ignoreCase;
	private final boolean ignoreWhitespace;
	private char charReadTooMuch;
	private Reader reader;
	private int parserLineNr;

	public XmlElement() {
		this(new HashMap<>(), false, true, true);
	}

	private XmlElement(Map<String, char[]> entities, boolean skipLeadingWhitespace, boolean fillBasicConversionTable, boolean ignoreCase) {
		this.ignoreWhitespace = skipLeadingWhitespace;
		this.ignoreCase = ignoreCase;
		this.name = null;
		this.contents = "";
		this.entities = entities;
		if (fillBasicConversionTable) {
			this.entities.put("amp", new char[] { '&' });
			this.entities.put("quot", new char[] { '"' });
			this.entities.put("apos", new char[] { '\'' });
			this.entities.put("lt", new char[] { '<' });
			this.entities.put("gt", new char[] { '>' });
		}
	}

	private void addChild(XmlElement child) {
		this.children.add(child);
	}

	private void setAttribute(String name, Object value) {
		if (this.ignoreCase) {
			name = name.toLowerCase();
		}
		this.attributes.put(name, value.toString());
	}

	public List<XmlElement> children() {
		return this.children;
	}

	public String getContent() {
		return this.contents;
	}

	public Object getAttribute(String name) {
		if (this.ignoreCase) {
			name = name.toLowerCase();
		}
		return this.attributes.get(name);
	}

	public String getName() {
		return this.name;
	}

	public void parse(Reader reader) throws IOException, XmlParseException {
		this.charReadTooMuch = '\0';
		this.reader = reader;
		this.parserLineNr = 1;

		for (;;) {
			char ch = this.scanWhitespace();

			if (ch != '<') {
				throw this.expectedInput("<");
			}

			ch = this.readChar();

			if ((ch == '!') || (ch == '?')) {
				this.skipSpecialTag(0);
			}
			else {
				this.unreadChar(ch);
				this.scanElement(this);
				return;
			}
		}
	}

	private XmlElement createAnotherElement() {
		return new XmlElement(this.entities, this.ignoreWhitespace, false, this.ignoreCase);
	}

	@Override
	public String toString() {
		try {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				this.write(new PrintStream(out));
				out.flush();
				return new String(out.toByteArray(), 0);
			}
		}
		catch (IOException e) {
			// Java exception handling suxx
			return super.toString();
		}
	}

	private void write(PrintStream writer) {
		if (this.name == null) {
			this.writeEncoded(writer, this.contents);
			return;
		}
		writer.print('<');
		writer.print(this.name);
		if (!this.attributes.isEmpty()) {
			for (Map.Entry<String, String> stringStringEntry : this.attributes.entrySet()) {
				writer.print(' ');
				String value = stringStringEntry.getValue();
				writer.print(stringStringEntry.getKey());
				writer.print('=');
				writer.write('"');
				this.writeEncoded(writer, value);
				writer.write('"');
			}
		}
		if ((this.contents != null) && (!this.contents.isEmpty())) {
			writer.print('>');
			this.writeEncoded(writer, this.contents);
			writer.print('<');
			writer.print('/');
			writer.print(this.name);
			writer.write('>');
		}
		else {
			if (this.children.isEmpty()) {
				writer.print('/');
			}
			else {
				writer.print('>');
				for (XmlElement child : this.children()) {
					child.write(writer);
				}
				writer.print('<');
				writer.print('/');
				writer.print(this.name);
			}
			writer.print('>');
		}
	}

	private void writeEncoded(PrintStream writer, String str) {
		for (int i = 0; i < str.length(); i += 1) {
			char ch = str.charAt(i);
			switch (ch) {
				case '<':
					writer.write('&');
					writer.write('l');
					writer.write('t');
					writer.write(';');
					break;
				case '>':
					writer.write('&');
					writer.write('g');
					writer.write('t');
					writer.write(';');
					break;
				case '&':
					writer.write('&');
					writer.write('a');
					writer.write('m');
					writer.write('p');
					writer.write(';');
					break;
				case '"':
					writer.write('&');
					writer.write('q');
					writer.write('u');
					writer.write('o');
					writer.write('t');
					writer.write(';');
					break;
				case '\'':
					writer.write('&');
					writer.write('a');
					writer.write('p');
					writer.write('o');
					writer.write('s');
					writer.write(';');
					break;
				default:
					if ((ch < 32) || (ch > 126)) {
						writer.write('&');
						writer.write('#');
						writer.write('x');
						writer.print(Integer.toString(ch, 16));
						writer.write(';');
					}
					else {
						writer.write(ch);
					}
			}
		}
	}

	private void scanIdentifier(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (((ch < 'A') || (ch > 'Z'))
				&& ((ch < 'a') || (ch > 'z'))
				&& ((ch < '0') || (ch > '9'))
				&& (ch != '_')
				&& (ch != '.')
				&& (ch != ':')
				&& (ch != '-')
				&& (ch <= '\u007E')) {
				this.unreadChar(ch);
				return;
			}
			result.append(ch);
		}
	}

	private char scanWhitespace() throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					break;
				default:
					return ch;
			}
		}
	}

	private char scanWhitespace(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
				case ' ':
				case '\t':
				case '\n':
					result.append(ch);
				case '\r':
					break;
				default:
					return ch;
			}
		}
	}

	private void scanString(StringBuffer string) throws IOException {
		char delimiter = this.readChar();
		if ((delimiter != '\'') && (delimiter != '"')) {
			throw this.expectedInput("' or \"");
		}
		for (;;) {
			char ch = this.readChar();
			if (ch == delimiter) {
				return;
			}
			else if (ch == '&') {
				this.resolveEntity(string);
			}
			else {
				string.append(ch);
			}
		}
	}

	private void scanPCData(StringBuffer data) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (ch == '<') {
				ch = this.readChar();
				if (ch == '!') {
					this.checkCDATA(data);
				}
				else {
					this.unreadChar(ch);
					return;
				}
			}
			else if (ch == '&') {
				this.resolveEntity(data);
			}
			else {
				data.append(ch);
			}
		}
	}

	private boolean checkCDATA(StringBuffer buf) throws IOException {
		char ch = this.readChar();
		if (ch != '[') {
			this.unreadChar(ch);
			this.skipSpecialTag(0);
			return false;
		}
		else if (!this.checkLiteral("CDATA[")) {
			this.skipSpecialTag(1); // one [ has already been read
			return false;
		}
		else {
			int delimiterCharsSkipped = 0;
			while (delimiterCharsSkipped < 3) {
				ch = this.readChar();
				switch (ch) {
					case ']':
						if (delimiterCharsSkipped < 2) {
							delimiterCharsSkipped += 1;
						}
						else {
							buf.append(']');
							buf.append(']');
							delimiterCharsSkipped = 0;
						}
						break;
					case '>':
						if (delimiterCharsSkipped < 2) {
							buf.append(IntStream.range(0, delimiterCharsSkipped).mapToObj(i -> "]").collect(Collectors.joining()));
							delimiterCharsSkipped = 0;
							buf.append('>');
						}
						else {
							delimiterCharsSkipped = 3;
						}
						break;
					default:
						buf.append(IntStream.range(0, delimiterCharsSkipped)
							.mapToObj(i -> "]")
							.collect(Collectors.joining("", "", String.valueOf(ch))));
						delimiterCharsSkipped = 0;
				}
			}
			return true;
		}
	}

	private void skipComment() throws IOException {
		int dashesToRead = 2;
		while (dashesToRead > 0) {
			char ch = this.readChar();
			if (ch == '-') {
				dashesToRead -= 1;
			}
			else {
				dashesToRead = 2;
			}
		}
		if (this.readChar() != '>') {
			throw this.expectedInput(">");
		}
	}

	private void skipSpecialTag(int bracketLevel) throws IOException {
		if (bracketLevel == 0) {
			char ch = this.readChar();
			if (ch == '[') {
				bracketLevel += 1;
			}
			else if (ch == '-') {
				ch = this.readChar();
				if (ch == '[') {
					bracketLevel += 1;
				}
				else if (ch == ']') {
					bracketLevel -= 1;
				}
				else if (ch == '-') {
					this.skipComment();
					return;
				}
			}
		}
		char stringDelimiter = '\0';
		// <
		int tagLevel = 1;
		while (tagLevel > 0) {
			char ch = this.readChar();
			if (stringDelimiter == '\0') {
				if ((ch == '"') || (ch == '\'')) {
					stringDelimiter = ch;
				}
				else if (bracketLevel <= 0) {
					if (ch == '<') {
						tagLevel += 1;
					}
					else if (ch == '>') {
						tagLevel -= 1;
					}
				}
				if (ch == '[') {
					bracketLevel += 1;
				}
				else if (ch == ']') {
					bracketLevel -= 1;
				}
			}
			else {
				if (ch == stringDelimiter) {
					stringDelimiter = '\0';
				}
			}
		}
	}

	private boolean checkLiteral(String literal) throws IOException {
		int length = literal.length();
		for (int i = 0; i < length; i += 1) {
			if (this.readChar() != literal.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private char readChar() throws IOException {
		if (this.charReadTooMuch != '\0') {
			char ch = this.charReadTooMuch;
			this.charReadTooMuch = '\0';
			return ch;
		}
		else {
			int i = this.reader.read();
			if (i < 0) {
				throw this.unexpectedEndOfData();
			}
			else if (i == 10) {
				this.parserLineNr += 1;
				return '\n';
			}
			else {
				return (char) i;
			}
		}
	}

	private void scanElement(XmlElement elt) throws IOException {
		StringBuffer buf = new StringBuffer();
		this.scanIdentifier(buf);
		String name = buf.toString();
		elt.name = name;
		char ch = this.scanWhitespace();
		while ((ch != '>') && (ch != '/')) {
			buf.setLength(0);
			this.unreadChar(ch);
			this.scanIdentifier(buf);
			String key = buf.toString();
			ch = this.scanWhitespace();
			if (ch != '=') {
				throw this.expectedInput("=");
			}
			this.unreadChar(this.scanWhitespace());
			buf.setLength(0);
			this.scanString(buf);
			elt.setAttribute(key, buf);
			ch = this.scanWhitespace();
		}
		if (ch == '/') {
			ch = this.readChar();
			if (ch != '>') {
				throw this.expectedInput(">");
			}
			return;
		}
		buf.setLength(0);
		ch = this.scanWhitespace(buf);
		if (ch != '<') {
			this.unreadChar(ch);
			this.scanPCData(buf);
		}
		else {
			for (;;) {
				ch = this.readChar();
				if (ch == '!') {
					if (this.checkCDATA(buf)) {
						this.scanPCData(buf);
						break;
					}
					else {
						ch = this.scanWhitespace(buf);
						if (ch != '<') {
							this.unreadChar(ch);
							this.scanPCData(buf);
							break;
						}
					}
				}
				else {
					buf.setLength(0);
					break;
				}
			}
		}
		if (buf.length() == 0) {
			while (ch != '/') {
				if (ch == '!') {
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					this.skipComment();
				}
				else {
					this.unreadChar(ch);
					XmlElement child = this.createAnotherElement();
					this.scanElement(child);
					elt.addChild(child);
				}
				ch = this.scanWhitespace();
				if (ch != '<') {
					throw this.expectedInput("<");
				}
				ch = this.readChar();
			}
			this.unreadChar(ch);
		}
		else {
			if (this.ignoreWhitespace) {
				elt.contents = buf.toString().trim();
			}
			else {
				elt.contents = buf.toString();
			}
		}
		ch = this.readChar();
		if (ch != '/') {
			throw this.expectedInput("/");
		}
		this.unreadChar(this.scanWhitespace());
		if (!this.checkLiteral(name)) {
			throw this.expectedInput(name);
		}
		if (this.scanWhitespace() != '>') {
			throw this.expectedInput(">");
		}
	}

	private void resolveEntity(StringBuffer buf) throws IOException {
		char ch;
		StringBuilder keyBuf = new StringBuilder();
		for (;;) {
			ch = this.readChar();
			if (ch == ';') {
				break;
			}
			keyBuf.append(ch);
		}
		String key = keyBuf.toString();
		if (key.charAt(0) == '#') {
			try {
				if (key.charAt(1) == 'x') {
					ch = (char) Integer.parseInt(key.substring(2), 16);
				}
				else {
					ch = (char) Integer.parseInt(key.substring(1), 10);
				}
			}
			catch (NumberFormatException e) {
				throw this.unknownEntity(key);
			}
			buf.append(ch);
		}
		else {
			char[] value = this.entities.get(key);
			if (value == null) {
				throw this.unknownEntity(key);
			}
			buf.append(value);
		}
	}

	private void unreadChar(char ch) {
		this.charReadTooMuch = ch;
	}

	private XmlParseException unexpectedEndOfData() {
		String msg = "Unexpected end of data reached";
		return new XmlParseException(this.name, this.parserLineNr, msg);
	}

	private XmlParseException expectedInput(String charSet) {
		String msg = "Expected: " + charSet;
		return new XmlParseException(this.name, this.parserLineNr, msg);
	}

	private XmlParseException unknownEntity(String name) {
		String msg = "Unknown or invalid entity: &" + name + ";";
		return new XmlParseException(this.name, this.parserLineNr, msg);
	}
}
