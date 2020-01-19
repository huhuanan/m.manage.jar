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
import manage.flow.model.FlowDefine;
import manage.flow.service.FlowDefineService;
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

@ActionMeta(name="manageFlowDefine")
public class FlowDefineAction extends ManageAction {
	private FlowDefine model;

	public JSONMessage doSave(){
		setLogContent("保存", "保存流程信息");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			String msg=getService(FlowDefineService.class).save(model);
			result.push("model.oid", model.getOid());
			result.push("model.issueStatus", model.getIssueStatus());
			fillJSONResult(result,true,msg);
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doIssue(){
		setLogContent("发布", "发布流程信息");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			getService(FlowDefineService.class).issue(model);
			result.push("model.oid", model.getOid());
			result.push("model.issueStatus", model.getIssueStatus());
			fillJSONResult(result,true,"发布成功");
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doDelete(){
		setLogContent("删除", "删除流程及环节");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			getService(FlowDefineService.class).delete(model);
			fillJSONResult(result,true,"删除成功");
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}

	@ActionFormMeta(title="流程信息",
		rows={
			@FormRowMeta(fields={
					@FormFieldMeta(field="model.version",type=FormFieldType.HIDDEN),
				@FormFieldMeta(title="标识",field="model.identity",type=FormFieldType.TEXT,hint="请输入标识"),
			})
		},
		buttons={
			@FormButtonMeta(title = "继续", url = "action/manageFlowDefine/toEdit",event=FormButtonEvent.MODAL,modalWidth=950,
				params= {@ParamMeta(field="model.identity",name="model.identity"),@ParamMeta(field="model.version",name="model.version")},
				success=FormSuccessMethod.DONE_BACK,power="manage_flow_power")
		}
	)
	public ActionResult toApply() throws Exception{
		model=new FlowDefine();
		model.setVersion(1);
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionFormMeta(title="流程信息",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "model.issueStatus", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="标识",field="model.identity",type=FormFieldType.TEXT,hint="请输入标识",span=9,disabled=true),
				@FormFieldMeta(title="名称",titleWidth=80,field="model.name",type=FormFieldType.TEXT,hint="请输入名称",span=9),
				@FormFieldMeta(title="版本",titleWidth=80,field="model.version",type=FormFieldType.INT,disabled=true,span=6)
			}),
			@FormRowMeta(fields={@FormFieldMeta(title="描述", field = "model.description", type = FormFieldType.TEXTAREA,hint="请输入描述")}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="开始环节", field = "model.startSection.oid", type = FormFieldType.SELECT,span=9,
					querySelect= @QuerySelectMeta(modelClass = "manage.flow.model.FlowSection", title="name",titleExpression = "concat(name,' (',identity,')')", value = "oid"),
					linkField=@LinkFieldMeta(valueField="model.oid",field="flowDefine.oid")
				),
				@FormFieldMeta(hideTitle=true,field = "model.oid", type = FormFieldType.ALERT,span=15,
					alert=@FormAlertMeta(title = "定义流程环节后再选择并保存"))
			})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageFlowDefine/doSave",power="manage_flow_power",success=FormSuccessMethod.REFRESH_OTHER,
				showField="model.issueStatus",showValues="C"),
			@FormButtonMeta(title = "发布", url = "action/manageFlowDefine/doIssue",power="manage_flow_power",success=FormSuccessMethod.DONE_BACK,
				showField="model.issueStatus",showValues="C",
				confirm="发布后不可修改, 确定要发布流程吗?",style=ButtonStyle.SUCCESS)
		},
		others= {
			@FormOtherMeta(title = "流程环节", url = "action/manageFlowSection/toList?method=flowSectionData",
				linkField=@LinkFieldMeta(field="params[fromSection.flowDefine.oid]",valueField="model.oid"))	
		}
	)
	public ActionResult toEdit() throws Exception{
		if(null!=model){
			if(!StringUtil.isSpace(model.getOid())) {
				model=ModelQueryUtil.getModel(model);
			}else if(!StringUtil.isSpace(model.getIdentity())) {
				ModelCheckUtil.checkUniqueCombine(model, new String[]{"identity"},"标识已存在");
				model.setIssueStatus("C");
			}
			return getFormResult(this,ActionFormPage.EDIT);
		}
		throw new MException(this.getClass(),"参数错误");
	}
	public ActionResult toNewEdit() throws Exception {
		model=getService(FlowDefineService.class).saveNew(model);
		return toEdit();
	}
	@ActionFormMeta(title="流程信息",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(title="标识",field="model.identity",type=FormFieldType.TEXT,hint="请输入标识",span=9,disabled=true),
				@FormFieldMeta(title="名称",titleWidth=80,field="model.name",type=FormFieldType.TEXT,span=9,disabled=true),
				@FormFieldMeta(title="版本",titleWidth=80,field="model.version",type=FormFieldType.INT,span=6,disabled=true)
			}),
			@FormRowMeta(fields={@FormFieldMeta(title="描述", field = "model.description", type = FormFieldType.TEXTAREA,disabled=true)}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="开始环节", field = "model.startSection.oid", type = FormFieldType.SELECT,span=9,disabled=true,
					querySelect= @QuerySelectMeta(modelClass = "manage.flow.model.FlowSection", title="name",titleExpression = "concat(name,' (',identity,')')", value = "oid"),
					linkField=@LinkFieldMeta(valueField="model.oid",field="flowDefine.oid")
				),
				@FormFieldMeta(hideTitle=true,field = "model.oid", type = FormFieldType.ALERT,span=15,
					alert=@FormAlertMeta(title = "定义流程环节后再选择并保存"))
			})
		},
		others= {
			@FormOtherMeta(title = "流程环节", url = "action/manageFlowSection/toList?method=viewFlowSectionData",
				linkField=@LinkFieldMeta(field="params[fromSection.flowDefine.oid]",valueField="model.oid"))	
		}
	)
	public ActionResult toView() throws Exception{
		model=ModelQueryUtil.getModel(model);
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionTableMeta(dataUrl = "action/manageFlowDefine/flowDefineData",
			modelClass="manage.flow.model.FlowDefine",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "identity", title = "标识", width=130),
			@ActionTableColMeta(field = "name", title = "名称", width=130,
				link=@TableColLink(event = ButtonEvent.MODAL,modalWidth=950,url = "action/manageFlowDefine/toView",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.NONE)),
			@ActionTableColMeta(field = "description", title = "描述", width=200),
			@ActionTableColMeta(field = "version", title = "版本", width=100),
			@ActionTableColMeta(field = "issueStatus", title = "发布状态", width=100,
			colDatas= {@TableColData(value="C",title="草稿"),@TableColData(value="Y",title="已发布"),@TableColData(value="N",title="历史")}),
			@ActionTableColMeta(field = "issueDate", title = "发布时间", width=100,dateFormat="yyyy-MM-dd"),
			@ActionTableColMeta(field = "oid",title="操作",width=170,align="center",buttons={
				@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=950, url = "action/manageFlowDefine/toEdit",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.MUST_REFRESH,style=ButtonStyle.NORMAL,
					showField="issueStatus",showValues="C",
					power="manage_flow_power"
				),
				@ButtonMeta(title="删除", event = ButtonEvent.AJAX,url = "action/manageFlowDefine/doDelete",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.DANGER,
					showField="issueStatus",showValues="C",confirm="确定要删除该流程吗?",
					power="manage_flow_power"
				),
				@ButtonMeta(title="生成新版本", event = ButtonEvent.MODAL,modalWidth=950,url = "action/manageFlowDefine/toNewEdit",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.MUST_REFRESH,style=ButtonStyle.SUCCESS,
					showField="issueStatus",showValues="Y",confirm="确定要生成该流程吗?",
					power="manage_flow_power"
				),
				@ButtonMeta(title="历史", event = ButtonEvent.MODAL,modalWidth=850, url = "action/manageFlowDefine/toList?method=historyFlowDefineData",
					params={@ParamMeta(name = "params[identity]", field="identity")},style=ButtonStyle.DEFAULT,
					showField="issueStatus",showValues="Y"
				),
			})
		},
		querys = {
			@QueryMeta(field = "issueStatus", name = "流程状态", type = QueryType.SELECT,
				querySelectDatas= {@SelectDataMeta(title = "草稿", value = "C"),@SelectDataMeta(title = "已发布", value = "Y")}),
			@QueryMeta(field = "identity", name = "标识", type = QueryType.TEXT, hint="请输入标识", likeMode=true),
			@QueryMeta(field = "name", name = "名称", type = QueryType.TEXT, hint="请输入名称", likeMode=true),
			@QueryMeta(field = "description", name = "描述", type = QueryType.TEXT, hint="请输入描述", likeMode=true)
		},
		buttons = {
			@ButtonMeta(title="新增", event = ButtonEvent.MODAL,modalWidth=500,  url = "action/manageFlowDefine/toApply", 
				success=SuccessMethod.MUST_REFRESH,style=ButtonStyle.NORMAL,
				power="manage_flow_power"
			)
		}
	)
	public JSONMessage flowDefineData(){
		return getListDataResult(new QueryCondition[] {
			QueryCondition.in("issueStatus", new Object[] {"C","Y"})
		});
	}

	@ActionTableMeta(dataUrl = "action/manageFlowDefine/historyFlowDefineData",
			modelClass="manage.flow.model.FlowDefine",title="历史流程",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "identity", title = "标识", width=130),
			@ActionTableColMeta(field = "name", title = "名称", width=130,
				link=@TableColLink(event = ButtonEvent.MODAL,modalWidth=950,url = "action/manageFlowDefine/toView",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.NONE)),
			@ActionTableColMeta(field = "description", title = "描述", width=200),
			@ActionTableColMeta(field = "version", title = "版本", width=100),
			@ActionTableColMeta(field = "issueStatus", title = "发布状态", width=100,
			colDatas= {@TableColData(value="C",title="草稿"),@TableColData(value="Y",title="已发布"),@TableColData(value="N",title="历史")}),
			@ActionTableColMeta(field = "issueDate", title = "发布时间", width=100,dateFormat="yyyy-MM-dd")
		},
		querys = {
			@QueryMeta(field = "identity", name = "标识", type = QueryType.HIDDEN)
		}
	)
	public JSONMessage historyFlowDefineData(){
		return getListDataResult(new QueryCondition[] {
			QueryCondition.in("issueStatus", new Object[] {"N"})
		});
	}
	public FlowDefine getModel() {
		return model;
	}

	public void setModel(FlowDefine model) {
		this.model = model;
	}

	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}
	
	
}
