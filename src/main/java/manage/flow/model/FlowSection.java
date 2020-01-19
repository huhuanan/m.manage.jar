package manage.flow.model;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="of_flow_section",description="流程环节")
public class FlowSection extends Model {
	@LinkTableMeta(name="flow_define_oid",table=FlowDefine.class,description="所属流程")
	private FlowDefine flowDefine;

	@FieldMeta(name="identity",type=FieldType.STRING,length=50,description="标识")
	private String identity;
	@FieldMeta(name="name",type=FieldType.STRING,length=50,description="名称")
	private String name;
	@FieldMeta(name="sort",type=FieldType.INT,description="排序")
	private Integer sort;

	@FieldMeta(name="countersign",type=FieldType.STRING,length=1,description="会签|Y是,N否")
	private String countersign;
	@LinkTableMeta(name="back_section_oid",table=FlowSection.class,description="退回环节|会签Y必填,会签有一个没通过则退回")
	private FlowSection backSection;
	@FieldMeta(name="forwardable",type=FieldType.STRING,length=1,description="可转发|Y是,N否")
	private String forwardable;
	
	public String getForwardable() {
		return forwardable;
	}
	public void setForwardable(String forwardable) {
		this.forwardable = forwardable;
	}
	public FlowDefine getFlowDefine() {
		return flowDefine;
	}
	public void setFlowDefine(FlowDefine flowDefine) {
		this.flowDefine = flowDefine;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public String getCountersign() {
		return countersign;
	}
	public void setCountersign(String countersign) {
		this.countersign = countersign;
	}
	public FlowSection getBackSection() {
		return backSection;
	}
	public void setBackSection(FlowSection backSection) {
		this.backSection = backSection;
	}
	
}
