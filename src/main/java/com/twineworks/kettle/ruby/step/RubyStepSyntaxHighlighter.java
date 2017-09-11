/*
 * Ruby for pentaho kettle
 * Copyright (C) 2017 Twineworks GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.twineworks.kettle.ruby.step;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.jcodings.Encoding;
import org.jruby.Ruby;
import org.jruby.common.NullWarnings;
import org.jruby.lexer.ByteListLexerSource;
import org.jruby.lexer.LexerSource;
import org.jruby.lexer.LexingCommon;
import org.jruby.lexer.yacc.RubyLexer;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.ParserConfiguration;
import org.jruby.parser.ParserSupport;
import org.jruby.parser.RubyParserResult;
import org.jruby.parser.Tokens;
import org.jruby.util.ByteList;
import org.pentaho.di.ui.core.widget.StyledTextComp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class RubyStepSyntaxHighlighter {

  private final int TOKEN_COMMENT = -100;
  private final int COLOR_BLACK = 0;
  private final int COLOR_GREENISH = 1;
  private final int COLOR_VIOLET = 2;
  private final int COLOR_BLUE = 3;
  private final int COLOR_BLUE_GREEN = 4;
  private final int COLOR_YELLOW = 5;
  private final int COLOR_RED = 6;
  private final int COLOR_GRAY = 7;
  private final int COLOR_ORANGE = 8;
  private final int STYLE_DEFAULT = 0;
  private final int STYLE_STRING = 1;
  private final int STYLE_SYMBOL = 2;
  private final int STYLE_KEYWORD = 3;
  private final int STYLE_GLOBAL_FUNCTION = 4;
  private final int STYLE_STANDARD_METHOD = 5;
  private final int STYLE_LITERAL_BOUNDARY = 6;
  private final int STYLE_COMMENT = 7;
  private final int STYLE_CONSTANT = 8;
  private final int STYLE_VARIABLE = 9;
  private final int STYLE_NUMBER = 10;

  private final StyleRange[] styles;
  private RubyLexer lexer;
  private ParserSupport parserSupport;
  private byte[] utf8Bytes;
  private int[] codePointIndex;
  private String[] STANDARD_GLOBAL_FUNCTIONS = {"abort", "autoload", "autoload?", "binding", "block_given?", "callcc", "caller", "chomp", "chomp!", "chop",
    "chop!", "evel", "exec", "exit", "exit!", "fail", "fork", "format", "getc", "gets", "gsub", "gsub!", "iterator?", "load", "open", "p", "print", "printf", "putc", "puts", "rand",
    "readline", "readlines", "scan", "select", "sleep", "split", "sprintf", "srand", "sub", "sub!", "syscall", "system", "test", "trap", "warn"

  };
  SortedSet<String> GLOBAL_FUNCTIONS_SET = new TreeSet<>(Arrays.asList(STANDARD_GLOBAL_FUNCTIONS));
  private String[] STANDARD_METHODS = {"allocate", "clone", "display", "dup", "enum_for", "eql?", "equal?", "extend", "freeze", "frozen?", "hash", "id", "inherited", "inspect", "instance_of?", "is_a?",
    "kind_of?", "method", "methods", "new", "nil?", "object_id", "respond_to?", "send", "superclass", "taint", "tainted?", "to_a", "to_enum", "to_s", "untaint"

  };
  SortedSet<String> STANDARD_METHODS_SET = new TreeSet<>(Arrays.asList(STANDARD_METHODS));
  private String[] PSEUDO_KEYWORDS = {"at_exit", "attr", "attr_accessor", "attr_reader", "attr_writer", "include", "lambda", "load", "proc", "loop", "private", "protected", "public", "raise", "catch",
    "java_import", "require", "import", "include_package"

  };
  SortedSet<String> PSEUDO_KEYWORDS_SET = new TreeSet<>(Arrays.asList(PSEUDO_KEYWORDS));
  private String script;

  RubyStepSyntaxHighlighter() {

    // -- the colors to use --
    Display display = Display.getDefault();
    Color[] colors = new Color[]{
      new Color(display, new RGB(0, 0, 0)),          // Black
      new Color(display, new RGB(63, 127, 95)),      // Greenish
      new Color(display, new RGB(137, 89, 168)),     // Violet
      new Color(display, new RGB(66, 113, 174)),     // Blue
      new Color(display, new RGB(62, 153, 159)),     // BlueGreen
      new Color(display, new RGB(234, 183, 0)),      // Yellow
      new Color(display, new RGB(200, 40, 41)),      // Red
      new Color(display, new RGB(142, 144, 140)),    // Gray
      new Color(display, new RGB(245, 135, 31))      // Orange
    };

    styles = new StyleRange[]{
      new StyleRange(0, 0, null, null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_GREENISH], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_GREENISH], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_VIOLET], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_BLUE], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_BLUE], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_BLUE_GREEN], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_GRAY], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_YELLOW], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_RED], null, SWT.NORMAL),
      new StyleRange(0, 0, colors[COLOR_ORANGE], null, SWT.NORMAL)
    };

  }

  private StyleRange tokenToStyleRange(int token, Object value, int previousToken) {

    // determine keyword style up front
    if (token >= Tokens.kCLASS && token <= Tokens.kDO_LAMBDA) {
      return styles[STYLE_KEYWORD];
    }

    switch (token) {
      case TOKEN_COMMENT:
        return styles[STYLE_COMMENT];

      case Tokens.tINTEGER:
      case Tokens.tFLOAT:
      case Tokens.tRATIONAL:
        return styles[STYLE_NUMBER];
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
        if (previousToken == Tokens.tSYMBEG) {
          return styles[STYLE_SYMBOL];
        }
        // fall through
      case Tokens.tFID:

        if (value != null && PSEUDO_KEYWORDS_SET.contains(value.toString())) {
          return styles[STYLE_KEYWORD];
        }

        if (value != null && STANDARD_METHODS_SET.contains(value.toString())) {
          return styles[STYLE_STANDARD_METHOD];
        }

        if (value != null && GLOBAL_FUNCTIONS_SET.contains(value.toString())) {
          return styles[STYLE_GLOBAL_FUNCTION];
        }
        // fall through
      default:
        return styles[STYLE_DEFAULT];
    }

  }

  private int totalUtf8Bytes(byte initialByte) {
    int i = initialByte;
    if ((i & 0x000000F0) == 0x000000F0) {
      return 4;
    }
    if ((i & 0x000000E0) == 0x000000E0) {
      return 3;
    }
    if ((i & 0x000000C0) == 0x000000C0) {
      return 2;
    }
    return 1;
  }

  private void initBytes() {

    utf8Bytes = script.getBytes(StandardCharsets.UTF_8);
    codePointIndex = new int[utf8Bytes.length + 1];

    int c = 0;
    for (int i = 0; i < utf8Bytes.length; i++) {
      byte utf8Byte = utf8Bytes[i];
      codePointIndex[i] = c;
      int eatBytes = totalUtf8Bytes(utf8Byte) - 1;
      for (int j = 0; j < eatBytes; j++) {
        codePointIndex[++i] = c;
      }
      c += 1;
    }
    codePointIndex[utf8Bytes.length] = c;
  }

  private int charOffset(int byteOffset) {
    if (byteOffset >= codePointIndex.length) return script.length();
    return script.offsetByCodePoints(0, codePointIndex[byteOffset]);
  }

  private void initLexer(String title) {
    LexerSource lexerSource = new ByteListLexerSource(title, 0, new ByteList(utf8Bytes), null);
    lexerSource.setEncoding(Encoding.load("UTF8"));

    ParserSupport parserSupport = new ParserSupport();
    lexer = new RubyLexer(parserSupport, lexerSource, new NullWarnings(Ruby.getGlobalRuntime()));
    parserSupport.setLexer(lexer);
    parserSupport.setConfiguration(new ParserConfiguration(Ruby.getGlobalRuntime(), 0, false, true, false));
    parserSupport.setResult(new RubyParserResult());
    parserSupport.setWarnings(new NullWarnings(Ruby.getGlobalRuntime()));
    parserSupport.initTopLocalVariables();

    lexer.setState(LexingCommon.EXPR_BEG);

  }

  private int lexerOffset() {
    int p = lexer.lex_p;
    int pend = lexer.lex_pend;
    int lo = lexer.getLineOffset();
    int ret = lexer.eofp ? lo : p + lo;
    return ret;
  }

  void highlight(String title, StyledTextComp wText) {
    // set up lexer process
    script = wText.getText();
    StyledText canvas = wText.getStyledText();
    initBytes();
    initLexer(title);

    // remember bounds of current token
    int leftTokenBorder = 0;
    int rightTokenBorder = 0;
    int token = 0;
    int previousToken = 0;
    int lastCommentEnd = 0;

    ArrayList<StyleRange> ranges = new ArrayList<>(200);
    ArrayList<Integer> intRanges = new ArrayList<>(400);

    try {

      boolean keepParsing = true;

      while (keepParsing) {

				/* take care of comments, which are stripped out by the lexer */
        int[] upcomingComment = null;
        while ((rightTokenBorder >= lastCommentEnd || rightTokenBorder == 0) && (upcomingComment = getUpcomingCommentPos(rightTokenBorder)) != null) {
          leftTokenBorder = upcomingComment[0];
          rightTokenBorder = leftTokenBorder + upcomingComment[1];
          lastCommentEnd = rightTokenBorder;
//          System.out.println("Found comment -> [" + leftTokenBorder + "," + rightTokenBorder + "]");
          ranges.add(tokenToStyleRange(TOKEN_COMMENT, null, previousToken));

          int start = charOffset(leftTokenBorder);
          int count = charOffset(rightTokenBorder) - start;

          intRanges.add(start);
          intRanges.add(count);
        }

				/* read language syntax */
        int oldOffset = lexerOffset();
        previousToken = token;
        token = lexer.nextToken();
        keepParsing = !lexer.eofp;

        if (token > 0 && token != 10) {

          Object v = lexer.value();

          leftTokenBorder = oldOffset;
          if (leftTokenBorder < lastCommentEnd && lexerOffset() > lastCommentEnd) {
            leftTokenBorder = lastCommentEnd;
          }
          rightTokenBorder = lexerOffset();

//          System.out.println("Found token " + token + " -> " + lexer.value() + " [" + leftTokenBorder + "," + rightTokenBorder + "]");

          ranges.add(tokenToStyleRange(token, v, previousToken));
          int start = charOffset(leftTokenBorder);
          int count = charOffset(rightTokenBorder) - start;
          intRanges.add(start);
          intRanges.add(count);
        }

      }

      // don't mind anything that might go wrong during parsing
    } catch (SyntaxException e) {
      // apply the latest style to the rest of the file in case there is a syntax error
      if (ranges.size() > 0) {
        ranges.remove(ranges.size() - 1);
        intRanges.remove(intRanges.size() - 1);
        intRanges.remove(intRanges.size() - 1);
      }
      ranges.add(tokenToStyleRange(token, null, previousToken));
      int start = charOffset(leftTokenBorder);
      intRanges.add(start);
      intRanges.add(script.length() - start);

    } catch (Exception ignored) {
      // the lexer will sometimes throw a non-syntax exception when confronted with malformed input
//      ignored.printStackTrace();
    }

    // don't mind swt errors in case some unforeseen input brought the style ranges out of order
    try {
      canvas.setStyleRanges(ArrayUtils.toPrimitive(intRanges.toArray(new Integer[0])), ranges.toArray(new StyleRange[0]));
    } catch (Exception e) {
//      e.printStackTrace();
    }


  }

  // returns position and length pair of a comment that starts at this position (forwarding through whitespace)
  // returns null if there's no comment coming up
  private int[] getUpcomingCommentPos(int pos) {

    // if we're in the middle of a string or regex, there's no comments
    if (lexer.getStrTerm() != null)
      return null;

    // looking for next comment while ignoring whitespace
    boolean searchingComment = true;
    boolean isComment = false;
    int idx = pos;
    do {
      if (idx >= utf8Bytes.length) {
        searchingComment = false;
        break;
      }
      switch (utf8Bytes[idx]) {
        case '\t':
        case ' ':
        case '\n':
        case '\r':
          idx += 1;
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
      do {
        end += 1;
        if (end >= utf8Bytes.length) {
          foundEnd = true;
          break;
        }
        switch (utf8Bytes[end]) {
          case '\n':
            foundEnd = true;
        }
      } while (!foundEnd);

      return new int[]{idx, end - idx};
    } else {
      return null;
    }

  }

}