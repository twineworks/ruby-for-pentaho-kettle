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

import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.pentaho.di.ui.core.widget.StyledTextComp;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RubyStepParseErrorHelper {

  private Color errorLineColor;
  private Color normalLineColor;
  private ScriptingContainer container;

  private Pattern pErrorMessage = Pattern.compile(":[0-9]+:(.*?)$", Pattern.MULTILINE);
  private Pattern pErrorLine = Pattern.compile(":([0-9]+):");
  private Pattern pErrorChar = Pattern.compile("^(\\s+)\\^", Pattern.MULTILINE);

  RubyStepParseErrorHelper() {

    Display display = Display.getDefault();
    errorLineColor = new Color(display, new RGB(255, 220, 220));
    container = RubyStepFactory.createScriptingContainer(false);

    // do not litter the console with potential parse errors
    container.setError(new PrintStream(new NullOutputStream()));
    container.setOutput(new PrintStream(new NullOutputStream()));


  }

  boolean showParseErrors(StyledTextComp wText, Label wlSyntaxCheck) {

    if (normalLineColor == null) {
      wText.getStyledText().getLineBackground(0);
    }

    String script = wText.getText();

    // try to parse, if there's an error, highlight it
    try {
      container.parse(script, 0);
      wlSyntaxCheck.setText("OK");
      wlSyntaxCheck.setToolTipText(null);
    } catch (ParseFailedException e) {

      String errorMessage = e.getMessage();

      try {

        StyledText canvas = wText.getStyledText();

        // try to parse the error message
        Matcher m = pErrorMessage.matcher(errorMessage);

        if (m.find()) {
          String errorSummary = "Error: " + m.group(1).trim();
          canvas.setData("lastErrorMessage", errorSummary);
          wlSyntaxCheck.setText(errorSummary);
          wlSyntaxCheck.setToolTipText(wlSyntaxCheck.getText());
        }

        // try to parse the error line
        m = pErrorLine.matcher(errorMessage);

        int errorLine = 0;
        int errorCol = 0;
        if (m.find()) {
          errorLine = Integer.valueOf(m.group(1)) - 1;
          canvas.setData("lastErrorLine", errorLine);
          canvas.setLineBackground(errorLine, 1, errorLineColor);
        }

        // try to parse the actual error char
        m = pErrorChar.matcher(errorMessage);

        if (m.find()) {
          errorCol = m.group(1).length();
          canvas.setSelection(canvas.getOffsetAtLine(errorLine) + errorCol);
        }

      } catch (Exception ex) {
        //ex.printStackTrace();
        //ignore
      }
      return true;
    }
    return false;
  }

  void hideParseErrors(StyledTextComp wText, Label wlSyntaxCheck) {

    if (wlSyntaxCheck.getText().length() != 0) {
      wlSyntaxCheck.setText("");
      wlSyntaxCheck.setToolTipText(null);
    }

    Integer lastErrorLine = (Integer) wText.getStyledText().getData("lastErrorLine");
    if (lastErrorLine != null) {
      StyledText canvas = wText.getStyledText();
      canvas.setLineBackground(lastErrorLine, 1, normalLineColor);
      canvas.setData("lastErrorLine", null);
      canvas.setData("lastErrorMessage", null);

    }

  }

  void updateErrorToolTip(StyledTextComp wText, int x, int y) {
    StyledText canvas = wText.getStyledText();
    String errorText = (String) canvas.getData("lastErrorMessage");

    // if there's no errors, bail out
    if (errorText == null) {
      canvas.setToolTipText(null);
      return;
    }

    // shield against SWT anomalies
    try {
      // if there are errors, see if the user hovers over the error line
      int line = canvas.getLineAtOffset(canvas.getOffsetAtLocation(new Point(1, y)));

      if (line == (Integer) canvas.getData("lastErrorLine")) {
        canvas.setToolTipText(errorText);
        return;
      }

    } catch (IllegalArgumentException ignored) {

    }
    // in case something went wrong or the line was not correct, no tooltip
    canvas.setToolTipText(null);


  }

  boolean hasParseErrors(StyledTextComp wText) {

    try {
      container.parse(wText.getText(), 0);
      return false;
    } catch (Exception e) {
      return true;
    }

  }


}
