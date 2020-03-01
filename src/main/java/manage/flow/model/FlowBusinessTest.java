package manage.flow.model;

import java.util.Date;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;
import manage.model.AdminLogin;
import manage.model.OrgGroupView;

@TableMeta(name="of_flow_business_test",description="流程业务")
public class FlowBusinessTest extends Model {

	@FieldMeta(name="type",type=FieldType.STRING,length=20,description="业务类型")
	private String type;
	@FieldMeta(name="title",type=FieldType.STRING,length=200,description="业务标题")
	private String title;
	@FieldMeta(name="busi_status",type=FieldType.STRING,length=1,description="业务状态|C草稿,S已提交,N不通过,Y通过")
	private String busiStatus;
	@LinkTableMeta(name="apply_user_oid",table=AdminLogin.class,description="申请人")
	private AdminLogin applyUser;
	@LinkTableMeta(name="apply_org_oid",table=OrgGroupView.class,description="申请部门")
	private OrgGroupView applyOrg;
	@FieldMeta(name="create_date",type=FieldType.DATE,description="创建时间")
	private Date createDate;
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
	public String getBusiStatus() {
		return busiStatus;
	}
	public void setBusiStatus(String busiStatus) {
		this.busiStatus = busiStatus;
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
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getDoneDate() {
		return doneDate;
	}
	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}
	
}
