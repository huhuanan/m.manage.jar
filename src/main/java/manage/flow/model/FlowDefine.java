package manage.flow.model;

import java.util.Date;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="of_flow_define",description="流程定义")
public class FlowDefine extends Model {

	@FieldMeta(name="identity",type=FieldType.STRING,length=50,description="标识")
	private String identity;
	@FieldMeta(name="name",type=FieldType.STRING,length=50,description="名称")
	private String name;
	@FieldMeta(name="description",type=FieldType.STRING,length=500,description="描述")
	private String description;
	
	@FieldMeta(name="version",type=FieldType.INT,description="版本|版本号不为1的不能修改标识")
	private Integer version;

	@LinkTableMeta(name="start_section_oid",table=FlowSection.class,description="开始环节")
	private FlowSection startSection;
	@FieldMeta(name="start_option",type=FieldType.STRING,length=2,description="开始选项|AU申请人,AO申请部门,MU多选用户,MO多选部门,OU单选用户,OO单选部门")
	private String startOption;
	@FieldMeta(name="issue_status",type=FieldType.STRING,length=1,description="是否发布|C草稿,Y已发布,N历史")
	private String issueStatus;
	@FieldMeta(name="issue_date",type=FieldType.DATE,description="发布时间")
	private Date issueDate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getIssueStatus() {
		return issueStatus;
	}

	public void setIssueStatus(String issueStatus) {
		this.issueStatus = issueStatus;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public FlowSection getStartSection() {
		return startSection;
	}

	public void setStartSection(FlowSection startSection) {
		this.startSection = startSection;
	}

	public String getStartOption() {
		return startOption;
	}

	public void setStartOption(String startOption) {
		this.startOption = startOption;
	}
	
}
