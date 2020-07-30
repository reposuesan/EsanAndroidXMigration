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
package pe.edu.esan.appostgrado.mixare;

import pe.edu.esan.appostgrado.mixare.gui.PaintScreen;
import pe.edu.esan.appostgrado.mixare.gui.ScreenObj;

public class Label implements ScreenObj {
	private float x, y;
	private float width, height;
	private ScreenObj obj;

	public void prepare(ScreenObj drawObj) {
		obj = drawObj;
		float w = obj.getWidth();
		float h = obj.getHeight();

		x = w / 2;
		y = 0;

		width = w * 2;
		height = h * 2;
	}

	public void paint(PaintScreen dw) {
		dw.paintObj(obj, x, y, 0, 1, false);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}