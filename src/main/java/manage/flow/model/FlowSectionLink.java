package manage.flow.model;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="of_flow_section_link",description="流程环节链接")
public class FlowSectionLink extends Model {

	@LinkTableMeta(name="from_section_oid",table=FlowSection.class,description="当前环节")
	private FlowSection fromSection;
	@LinkTableMeta(name="to_section_oid",table=FlowSection.class,description="下一步环节")
	private FlowSection toSection;
	@FieldMeta(name="is_next",type=FieldType.STRING,length=1,description="下一步|Y有,N结束")
	private String isNext;
	@FieldMeta(name="to_desc",type=FieldType.STRING,length=50,description="下一步描述")
	private String toDesc;
	@FieldMeta(name="to_param",type=FieldType.STRING,length=50,description="下一步参数")
	private String toParam;
	public FlowSection getFromSection() {
		return fromSection;
	}
	public String getToDesc() {
		return toDesc;
	}
	public void setToDesc(String toDesc) {
		this.toDesc = toDesc;
	}
	public void setFromSection(FlowSection fromSection) {
		this.fromSection = fromSection;
	}
	public FlowSection getToSection() {
		return toSection;
	}
	public void setToSection(FlowSection toSection) {
		this.toSection = toSection;
	}
	public String getIsNext() {
		return isNext;
	}
	public void setIsNext(String isNext) {
		this.isNext = isNext;
	}
	public String getToParam() {
		return toParam;
	}
	public void setToParam(String toParam) {
		this.toParam = toParam;
	}
	
}
