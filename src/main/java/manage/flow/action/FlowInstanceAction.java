package manage.flow.action;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryUtil;
import m.common.model.util.QueryCondition;
import m.system.RuntimeData;
import m.system.exception.MException;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.action.ManageAction;
import manage.flow.model.FlowInstance;
import manage.flow.service.FlowInstanceService;
import manage.flow.service.FlowSectionService;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.button.ButtonMeta.SuccessMethod;
import manage.util.page.button.ParamMeta;
import manage.util.page.form.ActionFormMeta;
import manage.util.page.form.FormAlertMeta;
import manage.util.page.form.FormButtonMeta;
import manage.util.page.form.FormButtonMeta.FormButtonEvent;
import manage.util.page.form.FormButtonMeta.FormSuccessMethod;
import manage.util.page.form.FormFieldMeta;
import manage.util.page.form.FormFieldMeta.FormFieldType;
import manage.util.page.form.FormOtherMeta;
import manage.util.page.form.FormRowMeta;
import manage.util.page.query.LinkFieldMeta;
import manage.util.page.query.QueryMeta;
import manage.util.page.query.QueryMeta.QueryType;
import manage.util.page.query.QuerySelectMeta;
import manage.util.page.query.SelectDataMeta;
import manage.util.page.table.ActionTableColMeta;
import manage.util.page.table.ActionTableColMeta.TableColType;
import manage.util.page.table.ActionTableMeta;
import manage.util.page.table.TableColData;
import manage.util.page.table.TableColLink;

@ActionMeta(name="manageFlowInstance")
public class FlowInstanceAction extends ManageAction {
	private FlowInstance model;

	@ActionTableMeta(dataUrl = "action/manageFlowInstance/flowInstanceData",
			modelClass="manage.flow.model.FlowInstance",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "flowDefine.identity", title = "流程", width=130,
				fieldExpression="concat(#{flowDefine.identity},'(',#{flowDefine.name},')')",
				link=@TableColLink(event = ButtonEvent.MODAL,modalWidth=950,url = "action/manageFlowDefine/toView",
					params={@ParamMeta(name = "model.oid", field="flowDefine.oid")},success=SuccessMethod.NONE)
			),
			@ActionTableColMeta(field = "business.type", title = "业务类型", width=100),
			@ActionTableColMeta(field = "business.title", title = "业务标题", width=200),
			@ActionTableColMeta(field = "currentSection.identity", title = "当前环节", width=100,
				fieldExpression="concat(#{currentSection.identity},'(',#{currentSection.name},')')"),
			@ActionTableColMeta(field = "createDate", title = "创建时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "doneStatus", title = "完成状态", width=100,
			colDatas= {@TableColData(value="Y",title="已完成"),@TableColData(value="N",title="未完成")}),
			@ActionTableColMeta(field = "doneDate", title = "完成时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "oid",title="操作",width=130,align="center",buttons={
				@ButtonMeta(title="执行情况", event = ButtonEvent.MODAL,modalWidth=650, url = "action/manageFlowInstance/toList?method=flowInstanceSectionData",
					params={@ParamMeta(name = "params[flowInstance.oid]", field="oid")},style=ButtonStyle.NORMAL,
					power="manage_flow_power"
				),
			})
		},
		querys = {
			@QueryMeta(field = "doneStatus", name = "完成状态", type = QueryType.SELECT,
				querySelectDatas= {@SelectDataMeta(title = "已完成", value = "Y"),@SelectDataMeta(title = "未完成", value = "N")}),
			@QueryMeta(field = "flowDefine.identity", name = "流程标识", type = QueryType.TEXT, hint="请输入标识", likeMode=true),
			@QueryMeta(field = "flowDefine.name", name = "流程名称", type = QueryType.TEXT, hint="请输入名称", likeMode=true),
			@QueryMeta(field = "business.type", name = "业务类型", type = QueryType.TEXT, hint="请输入描述", likeMode=true),
			@QueryMeta(field = "business.title", name = "业务标题", type = QueryType.TEXT, hint="请输入描述", likeMode=true)
		}
	)
	public JSONMessage flowInstanceData(){
		return getListDataResult(null);
	}
	@ActionTableMeta(dataUrl = "action/manageFlowInstance/flowInstanceSectionData",
			modelClass="manage.flow.model.FlowInstanceSection",title="执行情况",
			orders= {"flowIndex","acceptDate asc"},
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "flowSection.identity", title = "环节", width=150,
				fieldExpression="concat(#{flowSection.identity},'(',#{flowSection.name},')')"),
			@ActionTableColMeta(field = "user.realname", title = "执行人", width=100),
			@ActionTableColMeta(field = "acceptDate", title = "接收时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "doneStatus", title = "完成状态", width=100,
			colDatas= {@TableColData(value="Y",title="已完成"),@TableColData(value="N",title="未完成")}),
			@ActionTableColMeta(field = "doneDate", title = "完成时间", width=100,dateFormat="yyyy-MM-dd")
		},
		querys = {
			@QueryMeta(field = "flowInstance.oid", name = "", type = QueryType.HIDDEN)
		}
	)
	public JSONMessage flowInstanceSectionData(){
		return getListDataResult(null);
	}

	public FlowInstance getModel() {
		return model;
	}

	public void setModel(FlowInstance model) {
		this.model = model;
	}

	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}
	
	
}
