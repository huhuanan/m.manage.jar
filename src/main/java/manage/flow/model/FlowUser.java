package manage.flow.model;

import m.common.model.FieldMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.UserModel;
import m.common.model.type.FieldType;
@TableMeta(name="of_flow_user",description="流程执行人")
public class FlowUser extends Model {

	@FieldMeta(name="realname",type=FieldType.STRING,length=20,notnull=true,description="真实姓名")
	private String realname;

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}
}
