package manage.flow.action;

import m.common.action.ActionMeta;
import m.common.model.util.QueryCondition;
import m.system.exception.MException;
import m.system.util.JSONMessage;
import manage.action.ManageAction;
import manage.flow.model.FlowInstanceSection;
import manage.model.AdminLogin;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.button.ParamMeta;
import manage.util.page.query.QueryMeta;
import manage.util.page.query.QueryMeta.QueryType;
import manage.util.page.query.SelectDataMeta;
import manage.util.page.table.ActionTableColMeta;
import manage.util.page.table.ActionTableColMeta.TableColType;
import manage.util.page.table.ActionTableMeta;
import manage.util.page.table.TableColData;

@ActionMeta(name="manageFlowInstanceSection")
public class FlowInstanceSectionAction extends ManageAction {
	private FlowInstanceSection model;

	@ActionTableMeta(dataUrl = "action/manageFlowInstanceSection/myTodoData",
			modelClass="manage.flow.model.FlowInstanceSection",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "flowInstance.business.title", title = "业务标题", width=200,
				fieldExpression="concat('(',#{flowInstance.business.type},')',#{flowInstance.business.title})"),
			@ActionTableColMeta(field = "flowInstance.currentSection.identity", title = "当前环节", width=100,
				fieldExpression="concat(#{flowInstance.currentSection.identity},'(',#{flowInstance.currentSection.name},')')"),
			@ActionTableColMeta(field = "user.realname", title = "处理人", width=100),
			@ActionTableColMeta(field = "acceptDate", title = "接收时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "flowSection.identity", title = "环节", width=100,
				fieldExpression="concat(#{flowSection.identity},'(',#{flowSection.name},')')"),
			@ActionTableColMeta(field = "doneStatus", title = "完成状态", width=100,
				colDatas= {@TableColData(value="Y",title="已完成"),@TableColData(value="N",title="未处理")}),
			@ActionTableColMeta(field = "doneDate", title = "完成时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "flowInstance.oid",title="操作",width=80,align="center",buttons={
				@ButtonMeta(title="处理", event = ButtonEvent.MODAL,modalWidth=650, url = "action/manageFlow/toFlowInfo",
					params={@ParamMeta(name = "business.oid", field="flowInstance.business.oid")},
					style=ButtonStyle.NORMAL,showValues = "N",showField = "doneStatus"
				),
				@ButtonMeta(title="查看", event = ButtonEvent.MODAL,modalWidth=650,url = "action/manageFlow/toFlowInfo",
					params={@ParamMeta(name = "business.oid", field="flowInstance.business.oid")},
					style=ButtonStyle.DEFAULT,showValues = "Y",showField = "doneStatus"
				),
			})
		},
		querys = {
			@QueryMeta(field = "doneStatus", name = "完成状态", type = QueryType.SELECT,
				querySelectDatas= {@SelectDataMeta(title = "已完成", value = "Y"),@SelectDataMeta(title = "未完成", value = "N")}),
			@QueryMeta(field = "flowInstance.business.type", name = "业务类型", type = QueryType.TEXT, hint="请输入描述", likeMode=true),
			@QueryMeta(field = "flowInstance.business.title", name = "业务标题", type = QueryType.TEXT, hint="请输入描述", likeMode=true)
		}
	)
	public JSONMessage myTodoData() throws MException{
		AdminLogin admin=getSessionAdmin();
		if(null==admin) throw noLoginException;
		return getListDataResult(new QueryCondition[] {
			QueryCondition.or(new QueryCondition[] {
				QueryCondition.eq("user.oid", admin.getOid()),
				QueryCondition.eq("org.oid", admin.getOrgGroup().getOid())
			})
		});
	}

	public FlowInstanceSection getModel() {
		return model;
	}

	public void setModel(FlowInstanceSection model) {
		this.model = model;
	}

	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}
	
}
