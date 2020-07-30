/*
	Copyright (C) 2010- Peer internet solutions
	
	This file is part of ESAN App.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with This program.  If not, see <http://www.gnu.org/licenses/>.
*/
package pe.edu.esan.appostgrado.mixare.gui;

import android.graphics.Color;

import java.text.BreakIterator;
import java.util.ArrayList;

public class TextObj implements ScreenObj {
	String txt;
	float fontSize;
	float width, height;
	float areaWidth, areaHeight;
	String lines[];
	float lineWidths[];
	float lineHeight;
	float maxLineWidth;
	float pad;
	int borderColor, bgColor, textColor, textShadowColor;
	boolean underline;

	public TextObj(String txtInit, float fontSizeInit, float maxWidth,
                   PaintScreen dw, boolean underline) {
		this(txtInit, fontSizeInit, maxWidth, Color.rgb(255, 255, 255), Color
				.argb(128, 0, 0, 0), Color.rgb(255, 255, 255), Color.argb(64, 0, 0, 0),
				dw.getTextAsc() / 2, dw, underline);
	}

	public TextObj(String txtInit, float fontSizeInit, float maxWidth,
                   int borderColor, int bgColor, int textColor, int textShadowColor, float pad,
                   PaintScreen dw, boolean underline) {
		
		this.borderColor = borderColor;
		this.bgColor = bgColor;
		this.textColor = textColor;
		this.textShadowColor = textShadowColor;
		this.pad = pad;
		this.underline = underline;

		try {
			prepTxt(txtInit, fontSizeInit, maxWidth, dw);
		} catch (Exception ex) {
			ex.printStackTrace();
			prepTxt("TEXT PARSE ERROR", 12, 200, dw);
		}
	}

	private void prepTxt(String txtInit, float fontSizeInit, float maxWidth,
                         PaintScreen dw) {
		dw.setFontSize(fontSizeInit);

		txt = txtInit;
		fontSize = fontSizeInit;
		areaWidth = maxWidth - pad * 2;
		lineHeight = dw.getTextAsc() + dw.getTextDesc()
				+ dw.getTextLead();

		ArrayList<String> lineList = new ArrayList<String>();

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(txt);

		String[] palabras = txt.split(" ");
		
		String linea = "";
		for(String palabra : palabras) {
			linea = linea.equals("") ? palabra : linea + " " + palabra;
			
			if (dw.getTextWidth(linea) > areaWidth)	{
				lineList.add(linea);
				linea = "";
			}
		}
		
		if (!linea.equals(""))
			lineList.add(linea);
		
		lines = new String[lineList.size()];
		lineWidths = new float[lineList.size()];
		lineList.toArray(lines);

		maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++) {
			lineWidths[i] = dw.getTextWidth(lines[i]);
			if (maxLineWidth < lineWidths[i])
				maxLineWidth = lineWidths[i];
		}
		areaWidth = maxLineWidth;
		areaHeight = lineHeight * lines.length;

		width = areaWidth + pad * 2;
		height = areaHeight + pad * 2;
	}

	public void paint(PaintScreen dw) {
		dw.setFontSize(fontSize);

		dw.setFill(true);
		dw.setColor(bgColor);
		dw.paintRect(0, 0, width, height);

		dw.setFill(false);
		dw.setColor(borderColor);
		dw.paintRect(0, 0, width, height);

		
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			
			// stroke
/* 			dw.setFill(false);
			dw.setStrokeWidth(4);
		    dw.setColor(textShadowColor);
			dw.paintText(pad, pad + lineHeight * i + dw.getTextAsc(), line);
*/
			
			// actual text

			dw.setFill(true);
			dw.setStrokeWidth(0);
			dw.setColor(textColor);
			dw.paintText(pad, pad + lineHeight * i + dw.getTextAsc(), line, underline);
			
		}
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}
