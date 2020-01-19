package manage.flow.action;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelQueryUtil;
import m.system.RuntimeData;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.action.ManageAction;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowSectionLink;
import manage.flow.service.FlowSectionService;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.button.ButtonMeta.SuccessMethod;
import manage.util.page.button.DropButtonMeta;
import manage.util.page.button.ParamMeta;
import manage.util.page.form.ActionFormMeta;
import manage.util.page.form.FormAlertMeta;
import manage.util.page.form.FormButtonMeta;
import manage.util.page.form.FormButtonMeta.FormSuccessMethod;
import manage.util.page.form.FormFieldMeta;
import manage.util.page.form.FormFieldMeta.FormFieldType;
import manage.util.page.form.FormRowMeta;
import manage.util.page.query.LinkFieldMeta;
import manage.util.page.query.QueryMeta;
import manage.util.page.query.QueryMeta.QueryType;
import manage.util.page.query.QuerySelectMeta;
import manage.util.page.query.SelectDataMeta;
import manage.util.page.table.ActionTableColMeta;
import manage.util.page.table.ActionTableMeta;
import manage.util.page.table.TableColData;

@ActionMeta(name="manageFlowSection")
public class FlowSectionAction extends ManageAction {
	private FlowSection model;
	private FlowSectionLink link;

	public JSONMessage doSave(){
		setLogContent("保存", "保存流程环节");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			String msg=getService(FlowSectionService.class).save(model);
			result.push("model.oid", model.getOid());
			fillJSONResult(result,true,msg);
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doDelete(){
		setLogContent("删除", "删除环节和环节链接");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			getService(FlowSectionService.class).delete(model);
			fillJSONResult(result,true,"删除成功");
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doVerify(){
		setLogContent("验证", "验证环节和环节链接");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			getService(FlowSectionService.class).verifySection(model.getFlowDefine().getOid());
			fillJSONResult(result,true,"验证通过");
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doSaveNext(){
		setLogContent("保存", "保存环节链接");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			String msg=getService(FlowSectionService.class).saveNext(link);
			result.push("link.oid", link.getOid());
			fillJSONResult(result,true,msg);
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doDeleteNext(){
		setLogContent("删除", "删除环节链接");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_flow_power");
			getService(FlowSectionService.class).deleteNext(link);
			fillJSONResult(result,true,"删除成功");
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}

	@ActionFormMeta(title="流程环节",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "model.flowDefine.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="标识",field="model.identity",type=FormFieldType.TEXT,hint="请输入标识",span=9),
				@FormFieldMeta(title="名称",titleWidth=80,field="model.name",type=FormFieldType.TEXT,hint="请输入名称",span=9),
				@FormFieldMeta(title="排序",titleWidth=80,field="model.sort",type=FormFieldType.INT,span=6)
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="是否可转发",field = "model.forwardable", type = FormFieldType.RADIO,span=9,
					querySelectDatas= {@SelectDataMeta(value="Y",title="是"),@SelectDataMeta(value="N",title="否")}
				),
				@FormFieldMeta(hideTitle=true,field = "model.oid", type = FormFieldType.ALERT,span=15,
					alert=@FormAlertMeta(title = "转发是环节不变，更换执行人。")),
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="是否会签",field = "model.countersign", type = FormFieldType.RADIO,span=9,
					querySelectDatas= {@SelectDataMeta(value="Y",title="是"),@SelectDataMeta(value="N",title="否")},
					clearField="model.backSection.oid"
				),
				@FormFieldMeta(hideTitle=true,field = "model.oid", type = FormFieldType.ALERT,span=15,
					alert=@FormAlertMeta(title = "会签环节的所有人都通过才能进入下一步，否则走退回环节。")),
				@FormFieldMeta(title="退回环节",field = "model.backSection.oid", type = FormFieldType.SELECT,span=15,
					querySelect= @QuerySelectMeta(modelClass = "manage.flow.model.FlowSection", title = "name",titleExpression="concat(name,' (',identity,')')", value = "oid"),
					linkField=@LinkFieldMeta(valueField="model.flowDefine.oid",field="flowDefine.oid"),
					showField="model.countersign",showValues="Y"
				),
			})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageFlowSection/doSave",success=FormSuccessMethod.DONE_BACK)
		}
	)
	public ActionResult toEdit() throws Exception{
		if(null!=model&&!StringUtil.isSpace(model.getOid())){
			model=ModelQueryUtil.getModel(model);
		}else {
			model.setCountersign("N");
			model.setForwardable("N");
		}
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionFormMeta(title="环节下一步",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "link.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "link.fromSection.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "link.fromSection.flowDefine.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="当前环节",titleWidth = 80,field="link.fromSection.identity",type=FormFieldType.TEXT,span=14,disabled=true),
				@FormFieldMeta(hideTitle=true,field="link.fromSection.name",type=FormFieldType.TEXT,span=10,disabled=true),
				@FormFieldMeta(title="下一步",titleWidth = 80,field = "link.isNext", type = FormFieldType.RADIO,span=8,
					querySelectDatas= {@SelectDataMeta(value="Y",title="有"),@SelectDataMeta(value="N",title="结束")},
					clearField="link.toSection.oid"
				),
				@FormFieldMeta(title="描述",titleWidth = 60,field="link.toDesc",type=FormFieldType.TEXT,span=16,hint="请输入下一步描述"),
				@FormFieldMeta(title="参数",titleWidth = 60,field="link.toParam",type=FormFieldType.TEXT,span=16,hint="请输入下一步参数"),
				@FormFieldMeta(title="环节",titleWidth = 80, field = "link.toSection.oid", type = FormFieldType.SELECT,hint="请选择下一环节",
					querySelect= @QuerySelectMeta(modelClass = "manage.flow.model.FlowSection", title = "name",titleExpression="concat(name,' (',identity,')')", value = "oid"),
					linkField=@LinkFieldMeta(valueField="link.fromSection.flowDefine.oid",field="flowDefine.oid"),
					showField="link.isNext",showValues="Y"
				),
			})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageFlowSection/doSaveNext",success=FormSuccessMethod.DONE_BACK)
		}
	)
	public ActionResult toEditNext() throws Exception {
		if(null!=link&&!StringUtil.isSpace(link.getOid())){
			link=ModelQueryList.getModel(link,new String[] {"*","fromSection.*"});
		}else {
			link.setFromSection(ModelQueryList.getModel(link.getFromSection(), new String[] {"*"}));
			link.setIsNext("N");
		}
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionTableMeta(dataUrl = "action/manageFlowSection/flowSectionData",
			modelClass="manage.flow.model.FlowSectionLink",
			orders= {"fromSection.sort asc","fromSection.identity asc","isNext desc","oid asc"},
			rowspanIndex=0,rowspanNum=7,
		cols = { 
			@ActionTableColMeta(field = "fromSection.identity", title = "标识", width=80),
			@ActionTableColMeta(field = "fromSection.name", title = "名称", width=100),
			@ActionTableColMeta(field = "fromSection.forwardable", title = "可转发", width=60,
			colDatas= {@TableColData(value="Y",title="是"),@TableColData(value="N",title="否")}),
			@ActionTableColMeta(field = "fromSection.countersign", title = "会签", width=50,
			colDatas= {@TableColData(value="Y",title="是"),@TableColData(value="N",title="否")}),
			@ActionTableColMeta(field = "fromSection.backSection.name", title = "退回环节", width=100),
			@ActionTableColMeta(field = "fromSection.sort", title = "排序", width=50),
			@ActionTableColMeta(field = "fromSection.oid",title="环节",width=90,align="center",power="manage_flow_power",dropButtons= {
				@DropButtonMeta(title="操作",style=ButtonStyle.NORMAL,buttons={
					@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=700, url = "action/manageFlowSection/toEdit",
						params={@ParamMeta(name = "model.oid", field="fromSection.oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL
					),
					@ButtonMeta(title="删除", event = ButtonEvent.AJAX, url = "action/manageFlowSection/doDelete",
						params={@ParamMeta(name = "model.oid", field="fromSection.oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.DANGER,
						confirm="确定要删除该环节和所有环节链接吗?"
					),
					@ButtonMeta(title="添加环节链接", event = ButtonEvent.MODAL,modalWidth=500, url = "action/manageFlowSection/toEditNext",
						params={@ParamMeta(name = "link.fromSection.oid", field="fromSection.oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL				)
					}
				)	
			}),
			@ActionTableColMeta(field = "isNext", title = "下一步", width=60,
				colDatas= {@TableColData(value="Y",title="有"),@TableColData(value="N",title="结束")}),
			@ActionTableColMeta(field = "toSection.name", title = "环节", width=100),
			@ActionTableColMeta(field = "toParam", title = "参数", width=100),
			@ActionTableColMeta(field = "toDesc", title = "描述", width=100),
			@ActionTableColMeta(field = "oid",title="链接",width=90,fixed ="right",align="center",power="manage_flow_power",dropButtons={
				@DropButtonMeta(title = "操作",style=ButtonStyle.NORMAL,buttons= {
					@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=500, url = "action/manageFlowSection/toEditNext",
						params={@ParamMeta(name = "link.oid", field="oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL
					),
					@ButtonMeta(title="删除", event = ButtonEvent.AJAX, url = "action/manageFlowSection/doDeleteNext",
						params={@ParamMeta(name = "link.oid", field="oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.DANGER,
						confirm="确定要删除该环节链接吗?"
					)
				})
			}),
		},
		querys = {
			@QueryMeta(field = "fromSection.flowDefine.oid", name = "", type = QueryType.HIDDEN),
		},
		buttons = {
			@ButtonMeta(title="新增", event = ButtonEvent.MODAL,modalWidth=700,  url = "action/manageFlowSection/toEdit", 
				queryParams= {@ParamMeta(name = "model.flowDefine.oid",field="fromSection.flowDefine.oid")},
				success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL,
				power="manage_flow_power"
			),
			@ButtonMeta(title="验证环节", event = ButtonEvent.AJAX,url = "action/manageFlowSection/doVerify", 
				queryParams= {@ParamMeta(name = "model.flowDefine.oid",field="fromSection.flowDefine.oid")},
				style=ButtonStyle.NONE,
				power="manage_flow_power"
			)
		}
	)
	public JSONMessage flowSectionData(){
		return getListDataResult(null);
	}
	@ActionTableMeta(dataUrl = "action/manageFlowSection/viewFlowSectionData",
			modelClass="manage.flow.model.FlowSectionLink",
			orders= {"fromSection.sort asc","fromSection.identity asc","isNext desc","oid asc"},
			rowspanIndex=0,rowspanNum=6,
		cols = { 
			@ActionTableColMeta(field = "fromSection.identity", title = "标识", width=80),
			@ActionTableColMeta(field = "fromSection.name", title = "名称", width=100),
			@ActionTableColMeta(field = "fromSection.forwardable", title = "可转发", width=60,
			colDatas= {@TableColData(value="Y",title="是"),@TableColData(value="N",title="否")}),
			@ActionTableColMeta(field = "fromSection.countersign", title = "会签", width=50,
			colDatas= {@TableColData(value="Y",title="是"),@TableColData(value="N",title="否")}),
			@ActionTableColMeta(field = "fromSection.backSection.name", title = "退回环节", width=100),
			@ActionTableColMeta(field = "fromSection.sort", title = "排序", width=50),
			@ActionTableColMeta(field = "isNext", title = "下一步", width=60,
				colDatas= {@TableColData(value="Y",title="有"),@TableColData(value="N",title="结束")}),
			@ActionTableColMeta(field = "toSection.name", title = "环节", width=100),
			@ActionTableColMeta(field = "toParam", title = "参数", width=100),
			@ActionTableColMeta(field = "toDesc", title = "描述", width=100),
		},
		querys = {
			@QueryMeta(field = "fromSection.flowDefine.oid", name = "", type = QueryType.HIDDEN),
		}
	)
	public JSONMessage viewFlowSectionData(){
		return getListDataResult(null);
	}

	public FlowSection getModel() {
		return model;
	}

	public void setModel(FlowSection model) {
		this.model = model;
	}

	public FlowSectionLink getLink() {
		return link;
	}

	public void setLink(FlowSectionLink link) {
		this.link = link;
	}

	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}	
	
	
}
