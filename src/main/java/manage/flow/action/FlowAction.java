package manage.flow.action;

import java.util.ArrayList;
import java.util.List;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.util.ModelQueryList;
import m.common.model.util.QueryCondition;
import m.common.model.util.QueryOrder;
import m.system.RuntimeData;
import m.system.exception.MException;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.action.ManageAction;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowDefine;
import manage.flow.model.FlowInstance;
import manage.flow.model.FlowInstanceSection;
import manage.flow.model.FlowSectionLink;
import manage.flow.service.FlowBusinessService;
import manage.flow.service.FlowDefineService;
import manage.flow.service.FlowInstanceService;
import manage.flow.util.FlowUtil;
import manage.model.AdminLogin;
import manage.model.OrgGroupView;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ParamMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.form.ActionFormMeta;
import manage.util.page.form.FormButtonMeta;
import manage.util.page.form.FormButtonMeta.FormButtonEvent;
import manage.util.page.form.FormButtonMeta.FormButtonMethod;
import manage.util.page.form.FormButtonMeta.FormSuccessMethod;
import manage.util.page.form.FormFieldMeta;
import manage.util.page.form.FormFieldMeta.FormFieldType;
import manage.util.page.form.FormRowMeta;
import manage.util.page.form.FormViewUIMeta;

@ActionMeta(name="manageFlow")
public class FlowAction extends ManageAction {
	private FlowBusiness business;
	private FlowDefine define;
	private FlowInstance instance;
	private FlowInstanceSection instanceSection;
	private List<FlowSectionLink> sectionLinks;
	private FlowSectionLink selectLink;
	private List<AdminLogin> adminList=new ArrayList<AdminLogin>();
	private List<OrgGroupView> orgList=new ArrayList<OrgGroupView>();
	private String nodeGroup;
	private String flterGroup;

	public JSONMessage doStart() {
		setLogContent("处理", "开启流程实例");
		JSONMessage result=new JSONMessage();
		try {
			AdminLogin admin=getSessionAdmin();
			if(null==admin) throw noLoginException;
			business=getService(FlowBusinessService.class).toFlowBusiness(business.getOid());
			fillJSONResult(result,true,getService(FlowInstanceService.class).doStart(business, adminList, orgList));
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	public JSONMessage doProcess() {
		setLogContent("处理", "处理流程实例");
		JSONMessage result=new JSONMessage();
		try {
			AdminLogin admin=getSessionAdmin();
			if(null==admin) throw noLoginException;
			fillJSONResult(result,true,getService(FlowInstanceService.class).doProcess(admin,instance, selectLink, adminList,orgList));
		} catch (Exception e) {
			fillJSONResult(result,false,e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	
	@ActionFormMeta(title = "流程",//流程信息
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "business.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "instance.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "define.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "instance", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "instanceSection", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "instance.currentSection.forwardable", type = FormFieldType.HIDDEN),
				@FormFieldMeta(hideTitle = true,field = "define", type = FormFieldType.HTML,
					html = "<breadcrumb>"
						+ "<breadcrumb-item>流程： {{#{define}.name}} ( {{#{define}.identity}} ) </breadcrumb-item>"
						+ "<breadcrumb-item v-if=\"#{instance}.currentSection.oid\">当前环节： {{#{instance}.currentSection.name}} ( {{#{instance}.currentSection.identity}} ) </breadcrumb-item>"
						+ "<breadcrumb-item v-if=\"#{instance}.doneStatus=='Y'\"> 已完成 </breadcrumb-item>"
						+ "</breadcrumb>"
				),
			}),
			@FormRowMeta(showExpression = "!#{instance.oid}",fields={//未启动流程
				@FormFieldMeta(field = "nodeGroup", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "flterGroup", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="处理人",field="adminList",type=FormFieldType.SELECT_PAGE,
					selectPageWidth=900,selectPageUrlExpression = "'page/manage/pageUtil/selectOneAdmin.html?nodeGroup='+#{nodeGroup}+'&flterGroup='+#{flterGroup}",
					html="<tag v-if=\"#{adminList}&&#{adminList}.oid\">{{#{adminList}.realname}}</tag>"
				)
			}),
			@FormRowMeta(showExpression = "#{instanceSection}&&#{instanceSection}.doneStatus=='N'",fields={
				@FormFieldMeta(field = "sectionLinks", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "selectLink",title = "下一步", type = FormFieldType.HTML,
					html="<Radio-Group v-model=\"#{selectLink}\">"
						+ "<Radio v-if=\"#{instance.currentSection.forwardable}=='Y'\" :label=\"{}\">转发</Radio>"
						+ "<Radio v-for=\"item in #{sectionLinks}\" :label=\"item\">{{item.toDesc}} ("
						+ "{{item.isNext=='Y'?item.toSection.name:''}}{{item.isNext=='N'?'结束':''}}{{item.isNext=='B'?'退回':''}})"
						+ "</Radio>"
						+ "</Radio-Group>"
				),
				//AU申请人,AO申请部门,MU多选用户,MO多选部门,OU单选用户,OO单选部门
				@FormFieldMeta(title="处理人",field="adminList",type=FormFieldType.SELECT_PAGE,
					selectPageWidth=900,selectPageUrlExpression =  "'page/manage/pageUtil/selectOneAdmin.html?nodeGroup='+#{nodeGroup}+'&flterGroup='+#{flterGroup}",
					html="<tag v-if=\"#{adminList}&&#{adminList}.oid\">{{#{adminList}.realname}}</tag>",
					showExpression = "#{selectLink}&&(#{selectLink}.isNext=='Y'&&#{selectLink}.toOption=='OU'||!#{selectLink}.oid)"//selectLink.oid为空显示代表转发
				),
				@FormFieldMeta(title="处理人",field="adminList",type=FormFieldType.SELECT_PAGE,
					selectPageWidth=900,selectPageUrlExpression = "'page/manage/pageUtil/selectManyAdmin.html?nodeGroup='+#{nodeGroup}+'&flterGroup='+#{flterGroup}",
					html="<tag v-for=\"item in #{adminList}\">{{item.realname}}</tag>",
					showExpression = "#{selectLink}&&#{selectLink}.isNext=='Y'&&#{selectLink}.toOption=='MU'"
				),
				@FormFieldMeta(title="处理部门",field="orgList",type=FormFieldType.SELECT_PAGE,
					selectPageWidth=400,selectPageUrlExpression = "'page/manage/pageUtil/selectOneGroup.html?nodeGroup='+#{nodeGroup}",
					html="<tag v-if=\"#{orgList}&&#{orgList}.oid\">{{#{orgList}.name}}</tag>",
					showExpression = "#{selectLink}&&#{selectLink}.isNext=='Y'&&#{selectLink}.toOption=='OO'"
				),
				@FormFieldMeta(title="处理部门",field="orgList",type=FormFieldType.SELECT_PAGE,
					selectPageWidth=400,selectPageUrlExpression = "'page/manage/pageUtil/selectManyGroup.html?nodeGroup='+#{nodeGroup}",
					html="<tag v-for=\"item in #{orgList}\">{{item.name}}</tag>",
					showExpression = "#{selectLink}&&#{selectLink}.isNext=='Y'&&#{selectLink}.toOption=='MO'"
				)
			})
		},
		buttons = {
			@FormButtonMeta(title = "启动流程",event = FormButtonEvent.AJAX, url = "action/manageFlow/doStart",
				params = {@ParamMeta(name = "business.oid",field = "business.oid"),@ParamMeta(name = "adminList",field = "adminList")},method = FormButtonMethod.PARAMS_SUBMIT,
				showExpression = "!#{instance.oid}",success = FormSuccessMethod.DONE_BACK,
				confirm = "确定要启动流程吗？"),
			@FormButtonMeta(title = "提交",event = FormButtonEvent.AJAX, url = "action/manageFlow/doProcess",
				params = {@ParamMeta(name = "instance.oid",field = "instance.oid"),@ParamMeta(name = "selectLink",field = "selectLink"),@ParamMeta(name = "adminList",field = "adminList"),@ParamMeta(name = "orgList",field = "orgList")},method = FormButtonMethod.PARAMS_SUBMIT,
				showExpression = "#{instanceSection}&&#{instanceSection}.doneStatus=='N'",success = FormSuccessMethod.DONE_BACK,
				confirm = "确定要提交吗？"),
			@FormButtonMeta(title = "流程图",event = FormButtonEvent.MODAL,modalWidth = 500, url = "page/manage/flow/viewFlowDefine.html",
				params = {@ParamMeta(name = "defineOid",field = "define.oid"),@ParamMeta(name = "instanceOid",field = "instance.oid")},method = FormButtonMethod.PARAMS_SUBMIT,
				showExpression = "#{instance.oid}"),
		}
	)
	public ActionResult toFlowInfo() throws Exception {
		nodeGroup=StringUtil.noSpace(nodeGroup);
		flterGroup=StringUtil.noSpace(flterGroup);
		AdminLogin admin=getSessionAdmin();
		if(null==admin) throw noLoginException;
		if(null==business) throw new MException(this.getClass(), "参数错误");
		business=getService(FlowBusinessService.class).toFlowBusiness(admin,business.getOid(), business.getType(), business.getTitle(), business.getBusiService());
		instance=FlowUtil.getInstance4Business(business.getBusiService(), business.getOid());
		if(null!=instance) {
			define=instance.getFlowDefine();
			if("N".equals(instance.getDoneStatus())) {
				instanceSection=getService(FlowInstanceService.class).getInstanceSection(instance.getOid(), instance.getCurrentSection().getOid(), admin.getOid(), admin.getOrgGroup().getOid(), null);
				sectionLinks=ModelQueryList.getModelList(FlowSectionLink.class, new String[] {"*","toSection.*"}, null, 
					QueryCondition.eq("fromSection.oid", instance.getCurrentSection().getOid()), QueryOrder.asc("isNext"),QueryOrder.asc("toSection.identity"));
			}
		}else {
			define=getService(FlowDefineService.class).getIssue(FlowUtil.getFlowId(business.getBusiService()));
		}
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}

	public FlowBusiness getBusiness() {
		return business;
	}

	public void setBusiness(FlowBusiness business) {
		this.business = business;
	}

	public FlowDefine getDefine() {
		return define;
	}

	public void setDefine(FlowDefine define) {
		this.define = define;
	}

	public FlowInstance getInstance() {
		return instance;
	}

	public void setInstance(FlowInstance instance) {
		this.instance = instance;
	}

	public FlowInstanceSection getInstanceSection() {
		return instanceSection;
	}

	public void setInstanceSection(FlowInstanceSection instanceSection) {
		this.instanceSection = instanceSection;
	}

	public List<FlowSectionLink> getSectionLinks() {
		return sectionLinks;
	}

	public void setSectionLinks(List<FlowSectionLink> sectionLinks) {
		this.sectionLinks = sectionLinks;
	}

	public FlowSectionLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FlowSectionLink selectLink) {
		this.selectLink = selectLink;
	}

	public List<AdminLogin> getAdminList() {
		return adminList;
	}

	public void setAdminList(List<AdminLogin> adminList) {
		this.adminList = adminList;
	}

	public List<OrgGroupView> getOrgList() {
		return orgList;
	}

	public void setOrgList(List<OrgGroupView> orgList) {
		this.orgList = orgList;
	}
	public String getNodeGroup() {
		return nodeGroup;
	}
	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	public String getFlterGroup() {
		return flterGroup;
	}
	public void setFlterGroup(String flterGroup) {
		this.flterGroup = flterGroup;
	}


}
