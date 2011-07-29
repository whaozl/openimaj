package org.openimaj.image.typography;

import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Map;

import org.openimaj.image.Image;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * Base class for the representation of font styles.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <F> the {@link Font} type
 * @param <T> the pixel type
 */
public class FontStyle<F extends Font<F>, T> {
	/**
	 * Attributes for styling {@link AttributedString}s.
	 *  
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class FontStyleAttribute extends Attribute {
		private static final long serialVersionUID = 1L;
		
		/**
		 * Default constructor
		 * @param name the name of the attribute
		 */
		public FontStyleAttribute(String name) {
			super(name);
		}
	}
	
	/**
	 * Horizontal alignment options
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 *
	 */
	public static enum HorizontalAlignment {
		/**
		 * Centered text from the starting point
		 */
		HORIZONTAL_CENTER,
		/**
		 * Left-aligned text from the starting point
		 */
		HORIZONTAL_LEFT,
		/**
		 * Right-aligned text from the starting point
		 */
		HORIZONTAL_RIGHT,
	}

	/**
	 * Vertical alignment options
	 *  
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 *
	 */
	public static enum VerticalAlignment {
		/**
		 * 
		 */
		VERTICAL_TOP,
		/**
		 * 
		 */
		VERTICAL_HALF,
		/**
		 * 
		 */
		VERTICAL_CAP,
		/**
		 * Bottom of characters aligned
		 */
		VERTICAL_BOTTOM,		
	}
	
	/**
	 * Attribute for the font. Value should be a {@link Font}.
	 */
	public static final Attribute FONT = new FontStyleAttribute("font");
	
	/**
	 * Attribute for italic text. Value should be Boolean.
	 */
	public static final Attribute ITALIC = new FontStyleAttribute("italic");
	
	/**
	 * Attribute for rotating the text. Value should be a Number in radians.
	 */
	public static final Attribute ANGLE = new FontStyleAttribute("angle");
	
	/**
	 * Attribute for stroke colour. Value should be of type <T>.
	 */
	public static final Attribute COLOUR = new FontStyleAttribute("colour");
	
	/**
	 * Attribute for horizontal alignment. Must be an instance of {@link HorizontalAlignment}
	 */
	public static final Attribute HORIZONTAL_ALIGNMENT = new FontStyleAttribute("horizontalAlignment");
	
	/**
	 * Attribute for vertical alignment. Must be an instance of {@link VerticalAlignment}
	 */
	public static final Attribute VERTICAL_ALIGNMENT = new FontStyleAttribute("verticalAlignment");
	
	/**
	 * Attribute for font size. Must be a Number in pixels.
	 */
	public static final Attribute FONT_SIZE = new FontStyleAttribute("fontSize");

	private static final Font<?> DEFAULT_FONT = HersheyFont.ROMAN_SIMPLEX;
	
	/**
	 * The font
	 */
	protected F font;
	
	/**
	 * should the associated text be rendered in italic? 
	 */
	protected boolean italic;
	
	/**
	 * Angle in radians for drawing the associated text
	 */
	protected float angle;
	
	/**
	 * Stroke colour for drawing the associated text 
	 */
	protected T colour;
	
	/**
	 * horizontal alignment of the text
	 */
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_LEFT;
	
	/**
	 * vertical alignment of the text
	 */
	protected VerticalAlignment verticalAlignment = VerticalAlignment.VERTICAL_BOTTOM;
	
	/**
	 * Font size in pixels
	 */
	protected int fontSize = 24;
	
	protected FontStyle(F font, Image<T, ?> image) {
		colour = image.defaultForegroundColour();
		this.font = font;
	}

	/**
	 * Parse the attributes map and set this FontStyle accordingly.
	 * Subclasses should override this method to add extra attributes.
	 * 
	 * @param attrs the attribute map
	 */
	@SuppressWarnings("unchecked")
	public void parseAttributes(Map<? extends Attribute,Object> attrs) {
		if (attrs.containsKey(FONT)) font = (F) attrs.get(FONT);
		if (attrs.containsKey(ITALIC)) italic = (Boolean) attrs.get(ITALIC);
		if (attrs.containsKey(ANGLE)) angle = ((Number) attrs.get(ANGLE)).floatValue();
		if (attrs.containsKey(COLOUR)) colour = (T) attrs.get(COLOUR);
		if (attrs.containsKey(HORIZONTAL_ALIGNMENT)) horizontalAlignment = (HorizontalAlignment) attrs.get(HORIZONTAL_ALIGNMENT);
		if (attrs.containsKey(VERTICAL_ALIGNMENT)) verticalAlignment = (VerticalAlignment) attrs.get(VERTICAL_ALIGNMENT);
		if (attrs.containsKey(FONT_SIZE)) fontSize = ((Number) attrs.get(FONT_SIZE)).intValue();
	}
	
	/**
	 * Get the renderer suitable for rendering text with this style
	 * into the given image.
	 * @param image the image
	 * @return the renderer
	 */
	public FontRenderer<T, FontStyle<F, T>> getRenderer(Image<T, ?> image) {
		return font.getRenderer(image);
	}
	
	/**
	 * Construct a new FontStyle from the given attribute map, suitable for
	 * rendering into the given image.
	 * @param <T> the pixel type.
	 * @param attrs the attribute map
	 * @param image the image
	 * @return the FontStyle
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> FontStyle parseAttributes(Map<? extends Attribute,Object> attrs, Image<T,?> image) {
		Font<?> fnt = (Font<?>) attrs.get(FONT);
		
		if (fnt == null)
			fnt = DEFAULT_FONT;
		
		FontStyle sty = fnt.createStyle(image);
		sty.parseAttributes(attrs);
		return sty;
	}

	/**
	 * @return the font
	 */
	public F getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(F font) {
		this.font = font;
	}

	/**
	 * @return the italic
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * @param italic the italic to set
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/**
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * @return the colour
	 */
	public T getColour() {
		return colour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setColour(T colour) {
		this.colour = colour;
	}

	/**
	 * @return the horizontalAlignment
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * @param horizontalAlignment the horizontalAlignment to set
	 */
	public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 * @return the verticalAlignment
	 */
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * @param verticalAlignment the verticalAlignment to set
	 */
	public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}
	
	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
}
