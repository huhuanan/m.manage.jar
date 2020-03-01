package manage.flow.model;

import java.util.Date;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;
import manage.model.AdminLogin;
import manage.model.OrgGroupView;

@TableMeta(name="of_flow_business",description="流程业务")
public class FlowBusiness extends Model {

	@FieldMeta(name="type",type=FieldType.STRING,length=20,description="业务类型")
	private String type;
	@FieldMeta(name="title",type=FieldType.STRING,length=200,description="业务标题")
	private String title;
	@FieldMeta(name="busi_service",type=FieldType.STRING,length=100,description="业务服务类|实现manage.flow.service.FlowService接口")
	private String busiService;

	@LinkTableMeta(name="current_section_oid",table=FlowSection.class,description="当前环节")
	private FlowSection currentSection;
	@FieldMeta(name="current_index",type=FieldType.INT,description="环节索引")
	private Integer currentIndex;
	@LinkTableMeta(name="apply_user_oid",table=AdminLogin.class,description="申请人")
	private AdminLogin applyUser;
	@LinkTableMeta(name="apply_org_oid",table=OrgGroupView.class,description="申请部门")
	private OrgGroupView applyOrg;
	@FieldMeta(name="create_date",type=FieldType.DATE,description="创建时间")
	private Date createDate;
	@FieldMeta(name="done_status",type=FieldType.STRING,length=1,description="处理状态|Y已完成,N未完成")
	private String doneStatus;
	@FieldMeta(name="done_date",type=FieldType.DATE,description="完成时间")
	private Date doneDate;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public FlowSection getCurrentSection() {
		return currentSection;
	}
	public void setCurrentSection(FlowSection currentSection) {
		this.currentSection = currentSection;
	}
	public Integer getCurrentIndex() {
		return currentIndex;
	}
	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
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
	public String getBusiService() {
		return busiService;
	}
	public void setBusiService(String busiService) {
		this.busiService = busiService;
	}
	public AdminLogin getApplyUser() {
		return applyUser;
	}
	public void setApplyUser(AdminLogin applyUser) {
		this.applyUser = applyUser;
	}
	public OrgGroupView getApplyOrg() {
		return applyOrg;
	}
	public void setApplyOrg(OrgGroupView applyOrg) {
		this.applyOrg = applyOrg;
	}
	
}
