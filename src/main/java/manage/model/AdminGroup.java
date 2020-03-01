package manage.model;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="os_admin_group",description="用户组表")
public class AdminGroup extends StatusModel {

	@FieldMeta(name="name",type=FieldType.STRING,length=20,description="名称")
	private String name;
	@FieldMeta(name="num",type=FieldType.STRING,length=20,description="编号")
	private String num;
	@FieldMeta(name="description",type=FieldType.STRING,length=1000,description="描述")
	private String description;
	@FieldMeta(name="type",type=FieldType.STRING,length=1,defaultValue="A",description="类型|A:组,B:角色,C:机构")
	private String type;
	@FieldMeta(name="business",type=FieldType.STRING,length=1,defaultValue="A",description="类型|A系统,B:业务")
	private String business;
	@FieldMeta(name="sort",type=FieldType.INT,description="排序")
	private Integer sort;

	@LinkTableMeta(name="parent_oid",table=AdminGroup.class,description="父用户组")
	private AdminGroup parent;
	
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public Integer getSort() {
		return sort;
	}
	public AdminGroup getParent() {
		return parent;
	}
	public void setParent(AdminGroup parent) {
		this.parent = parent;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
