package manage.util.excel;

import java.awt.Color;

import jxl.format.Alignment;
import jxl.format.Colour;

public class SheetCell {
	private Object content;
	private String format;//格式   content为数字或日期时有效
	private Alignment align;
	private Integer width;
	private Colour color;//文字颜色
	private Colour background;//背景颜色
	
	public SheetCell(Object content,Integer width){
		this(content,width,Alignment.LEFT);
	}
	public SheetCell(Object content,String format,Integer width){
		this(content,format,width,Alignment.LEFT);
	}
	public SheetCell(Object content,Integer width,Alignment align){
		this(content,"",width,align);
	}
	public SheetCell(Object content,String format,Integer width,Alignment align){
		this.content=content;
		this.format=format;
		this.width=width;
		this.align=align;
		this.background=Colour.WHITE;
	}
	
	/**
	 * 
	 * @param content
	 * @param width
	 * @param align
	 * @return
	 */
	public static SheetCell headCell(String content,int width,Alignment align){
		SheetCell cell=new SheetCell(content,width,align);
		cell.setBackground(Colour.GRAY_25);
		return cell;
	}
	
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Alignment getAlign() {
		return align;
	}
	public void setAlign(Alignment align) {
		this.align = align;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Colour getBackground() {
		return background;
	}
	public SheetCell setBackground(Colour background) {
		this.background = background;
		return this;
	}
	/**
	 * 设置背景颜色
	 * @param color  #ff0000
	 * @return
	 */
	public SheetCell setBackground(String color) {
		this.background=getNearestColour(color);
		return this;
	}
	public Colour getColor() {
		return color;
	}
	public SheetCell setColor(Colour color) {
		this.color = color;
		return this;
	}
	/**
	 * 设置文字颜色
	 * @param color  #ff0000
	 * @return
	 */
	public SheetCell setColor(String color) {
		this.color = getNearestColour(color);
		return this;
	}
	
	private static Colour getNearestColour(String strColor) {
		Color cl = Color.decode(strColor);
		Colour color = null;
		Colour[] colors = Colour.getAllColours();
		if ((colors != null) && (colors.length > 0)) {
			Colour crtColor = null;
			int[] rgb = null;
			int diff = 0;
			int minDiff = 999;
			for (int i = 0; i < colors.length; i++) {
				crtColor = colors[i];
				rgb = new int[3];
				rgb[0] = crtColor.getDefaultRGB().getRed();
				rgb[1] = crtColor.getDefaultRGB().getGreen();
				rgb[2] = crtColor.getDefaultRGB().getBlue();
				
				diff = Math.abs(rgb[0] - cl.getRed())
				 + Math.abs(rgb[1] - cl.getGreen())
				 + Math.abs(rgb[2] - cl.getBlue());
				if (diff < minDiff) {
					minDiff = diff;
					color = crtColor;
				}
			}
		}
		if (color == null)
			color = Colour.BLACK;
		return color;
	}
}
