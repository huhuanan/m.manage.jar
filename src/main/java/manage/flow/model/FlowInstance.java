package manage.flow.model;

import java.util.Date;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="of_flow_instance",description="流程实例")
public class FlowInstance extends Model {

	@LinkTableMeta(name="flow_define_oid",table=FlowDefine.class,description="所属流程")
	private FlowDefine flowDefine;
	@LinkTableMeta(name="current_section_oid",table=FlowSection.class,description="当前环节")
	private FlowSection currentSection;
	@FieldMeta(name="current_index",type=FieldType.INT,description="环节索引")
	private Integer currentIndex;
	@LinkTableMeta(name="business_oid",table=FlowBusiness.class,description="业务属性")
	private FlowBusiness business;

	@FieldMeta(name="create_date",type=FieldType.DATE,description="创建时间")
	private Date createDate;
	@FieldMeta(name="done_status",type=FieldType.STRING,length=1,description="处理状态|Y已完成,N未完成")
	private String doneStatus;
	@FieldMeta(name="done_date",type=FieldType.DATE,description="完成时间")
	private Date doneDate;
	public FlowDefine getFlowDefine() {
		return flowDefine;
	}
	public Integer getCurrentIndex() {
		return currentIndex;
	}
	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
	}
	public void setFlowDefine(FlowDefine flowDefine) {
		this.flowDefine = flowDefine;
	}
	public FlowSection getCurrentSection() {
		return currentSection;
	}
	public void setCurrentSection(FlowSection currentSection) {
		this.currentSection = currentSection;
	}
	public FlowBusiness getBusiness() {
		return business;
	}
	public void setBusiness(FlowBusiness business) {
		this.business = business;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getDoneStatus() {
		return doneStatus;
	}
	public void setDoneStatus(String doneStatus) {
		this.doneStatus = doneStatus;
	}
	public Date getDoneDate() {
		return doneDate;
	}
	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}
	
}
