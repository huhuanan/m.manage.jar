package manage.flow.model;

import m.common.model.FieldMeta;
import m.common.model.Model;
import m.common.model.TableMeta;
import m.common.model.type.FieldType;

@TableMeta(name="of_flow_business",description="流程业务")
public class FlowBusiness extends Model {

	@FieldMeta(name="type",type=FieldType.STRING,length=20,description="类型")
	private String type;
	@FieldMeta(name="title",type=FieldType.STRING,length=200,description="标题")
	private String title;
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
	
}
