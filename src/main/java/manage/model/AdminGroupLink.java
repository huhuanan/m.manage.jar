package manage.model;

import m.common.model.FieldMeta;
import m.common.model.LinkTableMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;
@TableMeta(name="os_admin_group_link",description="管理员组关系表")
public class AdminGroupLink extends Model {

	@LinkTableMeta(name="admin_group_oid",table=AdminGroup.class,description="管理员组")
	private AdminGroup adminGroup;
	@FieldMeta(name="type",type=FieldType.STRING,length=1,defaultValue="A",description="组类型|A:组,B:角色,C:机构")
	private String type;
	@LinkTableMeta(name="admin_oid",table=AdminLogin.class,description="管理员")
	private AdminLogin admin;
	public AdminGroup getAdminGroup() {
		return adminGroup;
	}
	public void setAdminGroup(AdminGroup adminGroup) {
		this.adminGroup = adminGroup;
	}
	public AdminLogin getAdmin() {
		return admin;
	}
	public void setAdmin(AdminLogin admin) {
		this.admin = admin;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
