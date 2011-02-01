package org.typeexit.kettle.plugin.steps.ruby;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.jruby.CompatVersion;
import org.jruby.common.RubyWarnings;
import org.jruby.lexer.yacc.ByteArrayLexerSource;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.RubyYaccLexer;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.lexer.yacc.Token;
import org.jruby.parser.ParserConfiguration;
import org.jruby.parser.ParserSupport;
import org.jruby.parser.RubyParserResult;
import org.jruby.parser.Tokens;
import java.util.Arrays;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.apache.commons.lang.ArrayUtils;

public class RubyStepSyntaxHighlighter {

	RubyYaccLexer lexer;
	ParserSupport parserSupport;
	Color[] colors;
	final int TOKEN_COMMENT = -100;

	String[] STANDARD_GLOBAL_FUNCTIONS = {"abort", "autoload", "autoload?", "binding", "block_given?", "callcc", "caller", "chomp", "chomp!", "chop",
			"chop!", "evel", "exec", "exit", "exit!", "fail", "fork", "format", "getc", "gets", "gsub", "gsub!", "iterator?", "load", "open", "p", "print", "printf", "putc", "puts", "rand",
			"readline", "readlines", "scan", "select", "sleep", "split", "sprintf", "srand", "sub", "sub!", "syscall", "system", "test", "trap", "warn"

	};

	String[] STANDARD_METHODS = { "allocate", "clone", "display", "dup", "enum_for", "eql?", "equal?", "extend", "freeze", "frozen?", "hash", "id", "inherited", "inspect", "instance_of?", "is_a?",
			"kind_of?", "method", "methods", "new", "nil?", "object_id", "respond_to?", "send", "superclass", "taint", "tainted?", "to_a", "to_enum", "to_s", "untaint"

	};

	String[] PSEUDO_KEYWORDS = { "at_exit", "attr", "attr_accessor", "attr_reader", "attr_writer", "include", "lambda", "load", "proc", "loop", "private", "protected", "public", "raise", "catch",
			"java_import", "require", "import", "include_package"

	};

	SortedSet<String> GLOBAL_FUNCTIONS_SET = new TreeSet<String>(Arrays.asList(STANDARD_GLOBAL_FUNCTIONS));
	SortedSet<String> STANDARD_METHODS_SET = new TreeSet<String>(Arrays.asList(STANDARD_METHODS));
	SortedSet<String> PSEUDO_KEYWORDS_SET = new TreeSet<String>(Arrays.asList(PSEUDO_KEYWORDS));
	
	final int COLOR_BLACK = 0;
	final int COLOR_GREENISH = 1;
	final int COLOR_BLUE = 2;
	final int COLOR_ORANGE = 4;
	final int COLOR_RED = 5;
	final int COLOR_GREEN = 6;
	final int COLOR_GRAY = 7;
	
	final int STYLE_DEFAULT = 0;
	final int STYLE_STRING = 1;
	final int STYLE_SYMBOL = 2;
	final int STYLE_KEYWORD = 3;
	final int STYLE_GLOBAL_FUNCTION = 4;
	final int STYLE_STANDARD_METHOD = 5;
	final int STYLE_LITERAL_BOUNDARY = 6;
	final int STYLE_COMMENT = 7;
	final int STYLE_CONSTANT = 8;
	final int STYLE_VARIABLE = 9;
	
	
	StyleRange[] styles;
	

	public RubyStepSyntaxHighlighter() {

		// -- the colors to use --
		Display display = Display.getDefault();
		colors = new Color[] {
				new Color(display, new RGB(0, 0, 0)), 		// black
				new Color(display, new RGB(63, 127, 95)), 	// Greenish 
				new Color(display, new RGB(0, 0, 192)), 	// Blue
				new Color(display, new RGB(127, 0, 85)), 	// -- not used --
				new Color(display, new RGB(255, 102, 0)), 	// Orange	
				new Color(display, new RGB(225, 0, 0)), 	// Red
				new Color(display, new RGB(0, 128, 0)), 	// Green
				new Color(display, new RGB(128, 128, 128)) 	// Gray
		};
		
		styles = new StyleRange[] {
			new StyleRange(0, 0, null, null, SWT.NORMAL),
			new StyleRange(0, 0, colors[COLOR_RED], null, SWT.NORMAL),
			new StyleRange(0, 0, colors[COLOR_ORANGE], null, SWT.NORMAL),
			new StyleRange(0, 0, colors[COLOR_BLUE], null, SWT.BOLD),
			new StyleRange(0, 0, colors[COLOR_GREEN], null, SWT.NORMAL),
			new StyleRange(0, 0, colors[COLOR_GREEN], null, SWT.NORMAL),
			new StyleRange(0, 0, colors[COLOR_GRAY], null, SWT.BOLD),
			new StyleRange(0, 0, colors[COLOR_GREENISH], null, SWT.ITALIC),
			new StyleRange(0, 0, colors[COLOR_GRAY], null, SWT.BOLD),
			new StyleRange(0, 0, colors[COLOR_GRAY], null, SWT.NORMAL)
		};

		// -- lexer for finding language parts --
		lexer = new RubyYaccLexer();

		ParserSupport parserSupport = new ParserSupport();
		ParserConfiguration parserConfig = new ParserConfiguration(null, 0, true, CompatVersion.BOTH);
		parserSupport.setConfiguration(parserConfig);
		parserSupport.setResult(new RubyParserResult());
		parserSupport.setWarnings(new RubyWarnings(null));
		parserSupport.initTopLocalVariables();

		lexer.setEncoding(RubyYaccLexer.UTF8_ENCODING);
		lexer.setParserSupport(parserSupport);
		lexer.setState(RubyYaccLexer.LexState.EXPR_BEG);

	}

	private StyleRange tokenToStyleRange(int t, Object value, int prevt) {

		// determine keyword style up front
		if (t >= Tokens.kCLASS && t <= Tokens.kDO_LAMBDA) {
			return styles[STYLE_KEYWORD];
		}

		switch (t) {
		case TOKEN_COMMENT:
			return styles[STYLE_COMMENT];
		case Tokens.tSTRING_BEG:
		case Tokens.tSTRING_CONTENT:
		case Tokens.tSTRING_END:
		case Tokens.tSTRING_DBEG:
		case Tokens.tSTRING_DVAR:
			return styles[STYLE_STRING];
		case Tokens.tCONSTANT:
			return styles[STYLE_CONSTANT];
		case Tokens.tGVAR:
		case Tokens.tIVAR:
			return styles[STYLE_VARIABLE];
		case Tokens.tREGEXP_BEG:
		case Tokens.tREGEXP_END:
		case Tokens.tPIPE:
			return styles[STYLE_LITERAL_BOUNDARY];
		case Tokens.tSYMBEG:
			return styles[STYLE_SYMBOL];
		case Tokens.tIDENTIFIER:
			if (prevt == Tokens.tSYMBEG) {
				return styles[STYLE_SYMBOL];
			}
			// fall through
		case Tokens.tFID:

			if (value instanceof Token && PSEUDO_KEYWORDS_SET.contains(((Token) value).getValue().toString())) {
				return styles[STYLE_KEYWORD];
			}

			if (value instanceof Token && STANDARD_METHODS_SET.contains(((Token) value).getValue().toString())) {
				return styles[STYLE_STANDARD_METHOD];
			}

			if (value instanceof Token && GLOBAL_FUNCTIONS_SET.contains(((Token) value).getValue().toString())) {
				return styles[STYLE_GLOBAL_FUNCTION];
			}

		default:
			return styles[STYLE_DEFAULT];
		}

	}

	public void highlight(String title, StyledTextComp wText) {

		// set up lexer process
		String script = wText.getText();
		StyledText canvas = wText.getStyledText();
		byte[] utf8Script = null;

		try {
			utf8Script = script.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		List<String> lines = new ArrayList<String>(canvas.getLineCount());

		LexerSource lexerSource = new ByteArrayLexerSource(title, utf8Script, lines, 0, true);

		lexer.reset();
		lexer.setSource(lexerSource);
		lexer.setState(RubyYaccLexer.LexState.EXPR_BEG);

		// remember bounds of current token
		int leftTokenBorder = 0;
		int rightTokenBorder = 0;
		int t = 0;
		int prevt = 0;
		int lastCommentEnd = 0;

		ArrayList<StyleRange> ranges = new ArrayList<StyleRange>(200);
		ArrayList<Integer> intRanges = new ArrayList<Integer>(400);

		try {
			
			boolean keepParsing = true;
			
			while (keepParsing) {
				
				/* take care of comments, which are stripped out by the lexer */
				int[] upcomingComment = null;
				while ((rightTokenBorder >= lastCommentEnd || rightTokenBorder == 0 ) && (upcomingComment = getUpcomingCommentPos(utf8Script, rightTokenBorder)) != null){
					leftTokenBorder = upcomingComment[0];
					rightTokenBorder = leftTokenBorder + upcomingComment[1];
					lastCommentEnd = rightTokenBorder;
					//System.out.println("Found comment -> [" + leftTokenBorder + "," + rightTokenBorder + "]");
					ranges.add(tokenToStyleRange(TOKEN_COMMENT, null, prevt));
					intRanges.add(leftTokenBorder);
					intRanges.add(rightTokenBorder-leftTokenBorder);
				}
				
				/* read language syntax */
				int oldOffset = lexerSource.getOffset();
				keepParsing = lexer.advance();
				prevt = t;
				t = lexer.token();
				Object v = lexer.value();

				leftTokenBorder = oldOffset;
				if (leftTokenBorder < lastCommentEnd && lexerSource.getOffset() > lastCommentEnd){
					leftTokenBorder = lastCommentEnd;
				}
				rightTokenBorder = lexerSource.getOffset();				
				
				//System.out.println("Found token " + t + " -> " + lexer.value() + " [" + leftTokenBorder + "," + rightTokenBorder + "]");

				// skip whitespace and error formatting
				if (t != '\n' && t != -1){ 
					ranges.add(tokenToStyleRange(t, v, prevt));
					intRanges.add(leftTokenBorder);
					intRanges.add(rightTokenBorder-leftTokenBorder);
				}
			
			}

			// don't mind anything that might go wrong during parsing
		} catch (SyntaxException e) {
			// apply the latest style to the rest of the file in case there is a syntax error
			if (ranges.size() > 0) {
				ranges.remove(ranges.size() - 1);
				intRanges.remove(intRanges.size()-1);
				intRanges.remove(intRanges.size()-1);
			}
			ranges.add(tokenToStyleRange(t, null, prevt));
			intRanges.add(leftTokenBorder);
			intRanges.add(wText.getText().length() - leftTokenBorder);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// don't mind swt errors in case some unforseen input brought the style ranges out of order
		try {
			canvas.setStyleRanges(ArrayUtils.toPrimitive(intRanges.toArray(new Integer[0])), ranges.toArray(new StyleRange[0]));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		

	}

	// returns position and length pair of a comment that starts at this position (forwarding through whitespace)
	// return null if there's no comment coming up
	private int[] getUpcomingCommentPos(byte[] utf8Script, int pos) {

		// if we're in the middle of a string or regex, there's no comments 
		if (lexer.getStrTerm() != null)
			return null;

		// looking for next comment while ignoring whitespace
		boolean searchingComment = true;
		boolean isComment = false;
		int idx = pos;
		do {
			if (idx >= utf8Script.length) {
				searchingComment = false;
				break;
			}
			switch (utf8Script[idx]) {
			case '\t':
			case ' ':
			case '\n':
			case '\r':
				idx++;
				break;
			case '#':
				isComment = true;
				searchingComment = false;
				break;
			default:
				searchingComment = false;
			}
		} while (searchingComment);

		if (isComment) {
			// now to determine it's length, just scan up to \n or EOF
			int end = idx;
			boolean foundEnd = false;
			do{
				end += 1;
				if (end >= utf8Script.length){
					foundEnd = true;
					break;
				}
				switch(utf8Script[end]){
				case '\n':
					foundEnd = true;
				}
			}while(!foundEnd);

			return new int[] {idx, end-idx};
		} else {
			return null;
		}

	}

}
