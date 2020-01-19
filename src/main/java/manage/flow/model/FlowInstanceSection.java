package manage.flow.model;

import java.util.Date;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;
import manage.model.AdminLogin;

@TableMeta(name="of_flow_ins_section",description="流程实例环节")
public class FlowInstanceSection extends Model {

	@LinkTableMeta(name="flow_instance_oid",table=FlowInstance.class,description="所属流程实例")
	private FlowInstance flowInstance;
	@LinkTableMeta(name="flow_section_oid",table=FlowSection.class,description="环节")
	private FlowSection flowSection;
	@FieldMeta(name="flow_index",type=FieldType.INT,description="环节索引")
	private Integer flowIndex;

	@LinkTableMeta(name="user_oid",table=FlowUser.class,description="执行人")
	private FlowUser user;
	@FieldMeta(name="accept_date",type=FieldType.DATE,description="接收时间")
	private Date acceptDate;

	@FieldMeta(name="next",type=FieldType.STRING,length=1,description="下一步|F转发,Y下一步,N结束")
	private String next;
	@FieldMeta(name="param",type=FieldType.STRING,length=50,description="处理参数")
	private String param;
	@FieldMeta(name="done_status",type=FieldType.STRING,length=1,description="处理状态|Y已处理,N未处理")
	private String doneStatus;
	@FieldMeta(name="done_date",type=FieldType.DATE,description="完成时间")
	private Date doneDate;
	public FlowInstance getFlowInstance() {
		return flowInstance;
	}
	public void setFlowInstance(FlowInstance flowInstance) {
		this.flowInstance = flowInstance;
	}
	public FlowSection getFlowSection() {
		return flowSection;
	}
	public Integer getFlowIndex() {
		return flowIndex;
	}
	public void setFlowIndex(Integer flowIndex) {
		this.flowIndex = flowIndex;
	}
	public void setFlowSection(FlowSection flowSection) {
		this.flowSection = flowSection;
	}
	public FlowUser getUser() {
		return user;
	}
	public void setUser(FlowUser user) {
		this.user = user;
	}
	public Date getAcceptDate() {
		return acceptDate;
	}
	public void setAcceptDate(Date acceptDate) {
		this.acceptDate = acceptDate;
	}
	public String getNext() {
		return next;
	}
	public void setNext(String next) {
		this.next = next;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
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
