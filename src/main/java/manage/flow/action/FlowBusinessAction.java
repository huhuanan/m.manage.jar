package manage.flow.action;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.util.ModelQueryList;
import m.system.RuntimeData;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.action.ManageAction;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowBusinessTest;
import manage.flow.service.FlowBusinessService;
import manage.model.AdminLogin;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.button.ButtonMeta.SuccessMethod;
import manage.util.page.button.ParamMeta;
import manage.util.page.form.ActionFormMeta;
import manage.util.page.form.FormButtonMeta;
import manage.util.page.form.FormButtonMeta.FormButtonEvent;
import manage.util.page.form.FormButtonMeta.FormButtonMethod;
import manage.util.page.form.FormButtonMeta.FormSuccessMethod;
import manage.util.page.form.FormFieldMeta;
import manage.util.page.form.FormFieldMeta.FormFieldType;
import manage.util.page.form.FormRowMeta;
import manage.util.page.table.ActionTableColMeta;
import manage.util.page.table.ActionTableColMeta.TableColType;
import manage.util.page.table.ActionTableMeta;
import manage.util.page.table.TableColData;

@ActionMeta(name="manageFlowBusiness")
public class FlowBusinessAction extends ManageAction {
	private FlowBusinessTest model;
	private FlowBusiness flowBusi;


	public JSONMessage doSave() {
		setLogContent("保存", "保存测试业务");
		JSONMessage result=new JSONMessage();
		try {
			AdminLogin admin=getSessionAdmin();
			if(null==admin) throw noLoginException;
			fillJSONResult(result,true,getService(FlowBusinessService.class).save(model,admin));
			result.push("model.oid", model.getOid());
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	@ActionFormMeta(titleExpression = "'业务 ('+#{model.type}+')'+#{model.title}",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="业务类型",field="model.type",type=FormFieldType.TEXT,span=8),
				@FormFieldMeta(title="业务标题",titleWidth=80,field="model.title",type=FormFieldType.TEXT,span=16)
			}),
		},
		buttons = {
			@FormButtonMeta(title = "流程",event = FormButtonEvent.MODAL,modalWidth = 600, url = "action/manageFlow/toFlowInfo",
				params = {@ParamMeta(name = "business.busiService",value = "manage.flow.service.FlowBusinessService"),
					@ParamMeta(name = "nodeGroup",value = "00100"),@ParamMeta(name = "business.oid",field = "model.oid"),
					@ParamMeta(name = "business.type",field = "model.type"),@ParamMeta(name = "business.title",field = "model.title")
				},method = FormButtonMethod.PARAMS_SUBMIT,success = FormSuccessMethod.DONE_BACK,
				showExpression = "#{model.oid}"),
			@FormButtonMeta(title = "保存",event = FormButtonEvent.AJAX, url = "action/manageFlowBusiness/doSave",
				success = FormSuccessMethod.REFRESH_OTHER)
		}
	)
	public ActionResult toEdit() throws Exception{
		if(null!=model&&!StringUtil.isSpace(model.getOid())){
			model=ModelQueryList.getModel(model,new String[] {"*"});
		}else {
			model=new FlowBusinessTest();
			model.setType("测试");
		}
		return getFormResult(this,ActionFormPage.EDIT);
	}

	@ActionTableMeta(dataUrl = "action/manageFlowBusiness/businessData",
			modelClass="manage.flow.model.FlowBusinessTest",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "type", title = "业务类型", width=100),
			@ActionTableColMeta(field = "title", title = "业务标题", width=200),
			@ActionTableColMeta(field = "busiStatus", title = "业务状态", width=100,
			colDatas= {@TableColData(value="C",title="草稿"),@TableColData(value="S",title="已提交"),@TableColData(value="N",title="不通过"),@TableColData(value="Y",title="通过")}),
			@ActionTableColMeta(field = "createDate", title = "创建时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "doneDate", title = "完成时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "oid",title="操作",width=130,align="center",buttons={
				@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=650, url = "action/manageFlowBusiness/toEdit",
					params={@ParamMeta(name = "model.oid", field="oid")},
					style=ButtonStyle.NORMAL,success =SuccessMethod.REFRESH
				),
			})
		},
		querys = {
		},
		buttons = {
			@ButtonMeta(title="新增", event = ButtonEvent.MODAL,modalWidth=650, url = "action/manageFlowBusiness/toEdit",
				style=ButtonStyle.NORMAL,success =SuccessMethod.REFRESH
			),
		}
	)
	public JSONMessage businessData(){
		return getListDataResult(null);
	}

	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}

	public FlowBusinessTest getModel() {
		return model;
	}
	public void setModel(FlowBusinessTest model) {
		this.model = model;
	}
	public FlowBusiness getFlowBusi() {
		return flowBusi;
	}
	public void setFlowBusi(FlowBusiness flowBusi) {
		this.flowBusi = flowBusi;
	}

	
}
