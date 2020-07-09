package manage.util.excel;

import java.util.ArrayList;
import java.util.List;

public class SheetObject {
	private String name;
	private List<SheetRow> rows;
	private List<Integer[]> mergeCells;
	/**
	 * 
	 * @param rows
	 * @param name
	 */
	public SheetObject(SheetRow[] rows,String name){
		this.rows=new ArrayList<SheetRow>();
		for(SheetRow sr : rows) {
			this.rows.add(sr);
		}
		this.name=name;
		this.mergeCells=new ArrayList<Integer[]>();
	}
	public SheetObject addRow(SheetRow sr) {
		this.rows.add(sr);
		return this;
	}
	/**
	 * 设置合并单元格  
	 * int[][]{
	 * 	int[]{从列,从行,到列,到行}
	 * }
	 * @param mcs
	 * @return
	 */
	public SheetObject setMergeCells(Integer[][] mcs) {
		this.mergeCells=new ArrayList<Integer[]>();
		for(Integer[] ns : mcs) {
			this.mergeCells.add(ns);
		}
		return this;
	}
	public SheetObject setMergeCells(List<Integer[]> mcs) {
		this.mergeCells=mcs;
		return this;
	}
	/**
	 * 添加合并单元格  int[]{从列,从行,到列,到行}
	 * @param nc
	 * @return
	 */
	public SheetObject addMergeCells(Integer[] nc) {
		this.mergeCells.add(nc);
		return this;
	}
	
	public String getName() {
		return name;
	}
	public SheetRow[] getRows() {
		return rows.toArray(new SheetRow[] {});
	}
	public List<Integer[]> getMergeCells() {
		return this.mergeCells;
	}
}
