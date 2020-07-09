package manage.util.excel;

import java.util.ArrayList;
import java.util.List;

public class SheetRow {
	private Integer height;
	private List<SheetCell> cells;

	/**
	 * 
	 * @param cells
	 */
	public SheetRow(SheetCell[] cells){
		this(cells,null);
	}
	/**
	 * 
	 * @param cells
	 * @param height
	 */
	public SheetRow(SheetCell[] cells,Integer height){
		this.cells=new ArrayList<SheetCell>();
		for(SheetCell sc : cells) {
			this.cells.add(sc);
		}
		this.height=height;
	}


	public SheetCell[] getCells() {
		return cells.toArray(new SheetCell[] {});
	}

	public Integer getHeight() {
		return height;
	}
}
