package org.openimaj.image.typography.hershey;

import org.openimaj.image.Image;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Renderer for the Hershey vector font set.
 * Based on HersheyFont.java by James P. Buzbee, which carried the 
 * following copyright statement:
 * 
 * <pre>
 * Copyright (c) James P. Buzbee 1996
 * House Blend Software
 * 
 * jbuzbee@nyx.net
 * Version 1.1 Dec 11 1996
 * Version 1.2 Sep 18 1997
 * Version 1.3 Feb 28 1998
 * Version 1.4 Aug 13 2000 : J++ bug workaround by  Paul Emory Sullivan
 * 
 * Permission to use, copy, modify, and distribute this software
 * for any use is hereby granted provided
 * this notice is kept intact within the source file
 * This is freeware, use it as desired !
 * 
 * Very loosly based on code with authors listed as :
 * Alan Richardson, Pete Holzmann, James Hurt
 * </pre>
 *  
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
final class HersheyFontRenderer<T> extends FontRenderer<T, HersheyFontStyle<T>> {
	protected static HersheyFontRenderer<?> INSTANCE = new HersheyFontRenderer<Object>(); 
	
	private HersheyFontRenderer() {}
	
	@Override
	public void renderText(Image<T, ?> image, String text, int x, int y, HersheyFontStyle<T> style) {
		drawText(text, style, x, y, true, new Rectangle(), image );
	}

	@Override
	public Rectangle getBounds(String text, HersheyFontStyle<T> style) {
		Rectangle r = new Rectangle();
		drawText(text, style, 0, 0, false, r, null);
		return r;
	}
	
	protected void drawText(String text, HersheyFontStyle<T> sty, int xc, int yc, boolean Draw, Rectangle r, Image<T,?> image) {
		HersheyFontData fnt = sty.getFont().data;
		int character;
		int len;
		int rotpx = 0, rotpy = 0;
		int xp, yp;
		boolean rotate = false;
		float cosTheta = 0, sinTheta = 0;
		float verticalOffsetFactor = 0;

		// set the flag to true if the angle is not 0.0
		rotate = (sty.getAngle() != 0.0) ? true : false;

		// if we are to do a rotation
		if (rotate) {
			// set up the rotation variables
			float theta = -sty.getAngle();
			cosTheta = (float) Math.cos(theta);
			sinTheta = (float) Math.sin(theta);

			// set the position to do all rotations about
			rotpx = xc;
			rotpy = yc;
		}

		// starting position
		xp = xc;

		yp = yc;

		// if we are not going to actually draw the string
		if (!Draw) {
			// set up to initialize the bounding rectangle
			r.x = xp;
			r.y = yp;
			r.width = xp;
			r.height = yp;
		}

		switch (sty.getVerticalAlignment()) {
		case VERTICAL_TOP:
			verticalOffsetFactor = 0;
			break;
		case VERTICAL_HALF:
			verticalOffsetFactor = 0.5f;
			break;
		case VERTICAL_BOTTOM:
			verticalOffsetFactor = 1;
			break;
		case VERTICAL_CAP:
			verticalOffsetFactor = 0.25f;
			break;
		}

		// move the y position based on the vertical alignment
		yp = yp - (int) (verticalOffsetFactor * (sty.getActualHeightScale() * (fnt.characterSetMaxY - fnt.characterSetMinY)));

		// if we have a non-standard horizontal alignment
		if ((sty.getHorizontalAlignment() != HorizontalAlignment.HORIZONTAL_LEFT)
				&& (sty.getHorizontalAlignment() != HorizontalAlignment.HORIZONTAL_LEFT)) {
			// find the length of the string in pixels ...
			len = 0;

			for (int j = 0; j < text.length(); j++) {
				// the character's number in the array ...
				character = text.charAt(j) - ' ';

				len += (fnt.characterMaxX[character] - fnt.characterMinX[character]) * sty.getActualWidthScale();
			}

			// if we are center aligned
			if (sty.getHorizontalAlignment() == HorizontalAlignment.HORIZONTAL_CENTER) {
				// move the starting point half to the left
				xp -= len / 2;
			} else {
				// alignment is right, move the start all the way to the left
				xp -= len;
			}
		}

		// loop through each character in the string ...
		for (int j = 0; j < text.length(); j++) {
			// the character's number in the array ...
			character = text.charAt(j) - ' ';

			// render this character
			drawCharacter(xp, yp, rotpx, rotpy, sty.getActualWidthScale(), sty.getActualHeightScale(), rotate,
					sinTheta, cosTheta, Draw, r, fnt.characterVectors[character],
					fnt.numberOfPoints[character], fnt.characterMinX[character],
					fnt.characterSetMinY, sty.getStrokeWidth(), sty.isItalic(), sty.getItalicSlant(), image,
					sty.getColour());

			// advance the starting coordinate
			xp += (int) ((fnt.characterMaxX[character] - fnt.characterMinX[character]) * sty.getActualWidthScale());

		} // end for each character
	}

	protected int fontAdjustment(String fontname) {
		int xadjust = 0;

		// if we do not have a script type font
		if (fontname.indexOf("scri") < 0) {
			// if we have a gothic font
			if (fontname.indexOf("goth") >= 0) {
				xadjust = 2;
			} else {
				xadjust = 3;
			}
		}

		return xadjust;
	}

	protected void drawCharacter(int xp, int yp, int rotpx, int rotpy,
			float width, float height, boolean rotate, float sinTheta,
			float cosTheta, boolean draw, Rectangle r, char vectors[][],
			int numberOfPoints, int minX, int characterSetMinY, int lineWidth,
			boolean italics, float slant, Image<T,?> image, T colour) {
		float xd, yd, xd2, yd2;
		int oldx = 0, oldy = 0, x, y, i;
		boolean skip = true;
		float finalSlant = height * (-slant);

		// loop through each vertex in the character
		for (i = 1; i < numberOfPoints; i++) {
			// if this is a "skip"
			if (vectors[HersheyFontData.X][i] == ' ') {
				// set the skip flag
				skip = true;
			} else {
				// calculate italics offset if necessary
				x = (int) ((italics) ? ((vectors[HersheyFontData.Y][i] - characterSetMinY) * finalSlant)
						: 0)
						+
						// add italics offset to the "normal" point
						// transformation
						transformX(xp, vectors[HersheyFontData.X][i], minX, width);

				// calculate the y coordinate
				y = transformY(yp, vectors[HersheyFontData.Y][i], characterSetMinY, height);

				// if we are doing a rotation
				if (rotate) {
					// apply the rotation matrix ...

					// transform the coordinate to the rotation center point
					xd = (x - rotpx);
					yd = (y - rotpy);

					// rotate
					xd2 = xd * cosTheta - yd * sinTheta;
					yd2 = xd * sinTheta + yd * cosTheta;

					// transform back
					x = (int) (xd2 + 0.5) + rotpx;
					y = (int) (yd2 + 0.5) + rotpy;
				}

				if (!draw) {
					// we just want the bounding box of the string
					if (x < r.x) {
						r.x = x;
					}
					if (y < r.y) {
						r.y = y;
					}

					if (x > r.width) {
						r.width = x;
					}
					if (y > r.height) {
						r.height = y;
					}
				}

				if (!skip) {
					// if we are to draw the string
					if (draw) {
						image.drawLine(oldx, oldy, x, y, lineWidth, colour);
					}
				} // end if not skip

				skip = false;

				oldx = x;
				oldy = y;

			} // end if skip
		} // end for each vertex in the character
	}

	protected final int transformX(int xoffset, int px, int minx, float mag) {
		return ((int) (xoffset + (px - minx) * mag));
	}

	protected final int transformY(int yoffset, int py, int miny, float mag) {
		return ((int) (yoffset + (py - miny) * mag));
	}
}
