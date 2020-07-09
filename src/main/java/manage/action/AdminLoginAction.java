package manage.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.util.ModelQueryUtil;
import m.system.RuntimeData;
import m.system.cache.CacheUtil;
import m.system.exception.MException;
import m.system.lang.HtmlBodyContent;
import m.system.util.JSONMessage;
import manage.dao.AdminLoginDao;
import manage.model.AdminGroupLink;
import manage.model.AdminLogin;
import manage.run.ModuleInitRun;
import manage.service.AdminLoginService;
import manage.service.SystemInfoService;
import manage.util.CaptchaUtil;
import manage.util.page.button.ButtonMeta;
import manage.util.page.button.ButtonMeta.ButtonEvent;
import manage.util.page.button.ButtonMeta.ButtonStyle;
import manage.util.page.button.ButtonMeta.SuccessMethod;
import manage.util.page.button.DropButtonMeta;
import manage.util.page.button.ParamMeta;
import manage.util.page.form.ActionFormMeta;
import manage.util.page.form.FormButtonMeta;
import manage.util.page.form.FormButtonMeta.FormSuccessMethod;
import manage.util.page.form.FormFieldMeta;
import manage.util.page.form.FormFieldMeta.FormFieldType;
import manage.util.page.form.FormRowMeta;
import manage.util.page.query.QueryMeta;
import manage.util.page.query.QueryMeta.QueryType;
import manage.util.page.query.QuerySelectMeta;
import manage.util.page.query.SelectConditionMeta;
import manage.util.page.table.ActionTableColMeta;
import manage.util.page.table.ActionTableColMeta.TableColType;
import manage.util.page.table.ActionTableMeta;

@ActionMeta(name="manageAdminLogin")
public class AdminLoginAction extends StatusAction {
	
	private AdminLogin model;
	private AdminGroupLink link;
	private String imageCode;
	private String autoLogin;
	private String password;
	private String nodeGroup;
	private String flterGroup;
	public ActionResult admin() throws MException, Exception{
		ActionResult result=new ActionResult(ModuleInitRun.getAdminPage());
		result.setMap(new HashMap<String, Object>());
		result.getMap().put("systemInfo", getService(SystemInfoService.class).getUniqueModel());
		return result;
	}
	public JSONMessage isLogin(){
		JSONMessage message=new JSONMessage();
		message.push("codeVerify", CaptchaUtil.isMastVerify(getIpAddress()+getSessionCookie()));
		try {
			model=getSessionAdmin();
			message.push("code", 0);
			message.push("model", model);
		} catch (Exception e) {
			message.push("code", 1);
			message.push("msg", e.getMessage());
			setLogError(e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	public HtmlBodyContent getCaptchaCode() throws IOException {
		HttpServletRequest request=getRequest();
		HttpServletResponse response=getResponse();
		response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        
        OutputStream os = response.getOutputStream();
        try {
            ImageIO.write(CaptchaUtil.getImageCode(getIpAddress()+getSessionCookie(), os), "jpg", os);
        }catch(IOException e) {
        }finally{
            if (os != null) {
                os.flush();
                os.close();
            }
        }
		return new HtmlBodyContent("");
	}
	/**
	 * 登录
	 * @return
	 */
	public JSONMessage doLogin(){
		setLogContent("登陆", "管理员登陆后台");
		JSONMessage message=new JSONMessage();
		try {
			CaptchaUtil.verifyCaptcha(getIpAddress()+getSessionCookie(), imageCode);
			model=getService(AdminLoginService.class).loginVerification(model);
			setSessionAdmin(model,autoLogin);
			getDao(AdminLoginDao.class).updateLastInfo(model, getIpAddress());
			message.push("code", 0);
			message.push("model", model);
			message.push("msg", "登录成功!");
			CaptchaUtil.clearMastVerify(getIpAddress()+getSessionCookie());
			CaptchaUtil.clearCode(getIpAddress()+getSessionCookie());
		} catch (Exception e) {
			CaptchaUtil.setMastVerify(getIpAddress()+getSessionCookie());
			message.push("code", 1);
			message.push("msg", e.getMessage());
			setLogError(e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	/**
	 * 退出登录
	 * @return
	 */
	public JSONMessage doLogout(){
		setLogContent("退出", "管理员退出后台");
		clearSessionAdmin();
		JSONMessage json=new JSONMessage();
		json.push("code", 0);
		return json;
	}

	/**
	 * 保存
	 * @return
	 */
	public JSONMessage doSave(){
		setLogContent("保存", "保存管理员信息");
		JSONMessage result=new JSONMessage();
		try {
			verifyAdminOperPower("manage_system_power");
			String msg=getService(AdminLoginService.class).save(model,link,password);
			CacheUtil.clear(AdminLogin.class, model.getOid());
			result.push("code", 0);
			result.push("msg", msg);
		} catch (Exception e) {
			result.push("code", 1);
			result.push("msg", e.getMessage());
			setLogError(e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	/**
	 * 保存
	 * @return
	 */
	public JSONMessage doSave4Self(){
		setLogContent("保存", "保存个人信息");
		JSONMessage result=new JSONMessage();
		try {
			AdminLogin admin=getSessionAdmin();
			if(null==admin) throw noLoginException;
			if(null==model||!model.getUsername().equals(admin.getUsername())){
				throw noPowerException;
			}
			model.setAdminGroup(admin.getAdminGroup());
			String msg=getService(AdminLoginService.class).save(model,password);
			CacheUtil.clear(AdminLogin.class, admin.getOid());
			result.push("code", 0);
			result.push("msg", msg);
		} catch (Exception e) {
			result.push("code", 1);
			result.push("msg", e.getMessage());
			setLogError(e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	
	public JSONMessage getAllAdmin() {
		JSONMessage result=new JSONMessage();
		try {
			AdminLogin admin=getSessionAdmin();
			if(null==admin) throw noLoginException;
			verifyAdminOperPower(getStatusPower());
			result.push("list", getService(AdminLoginService.class).getAll(this.nodeGroup,this.flterGroup));
			result.push("code", 0);
		} catch (Exception e) {
			result.push("code", 1);
			result.push("msg", e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return result;
	}
	@ActionFormMeta(title="添加管理员信息",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(field = "link.adminGroup.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="账号",field="model.username",type=FormFieldType.TEXT,span=12,hint="请输入账号"),
				@FormFieldMeta(title="真实姓名",field="model.realname",type=FormFieldType.TEXT,span=12,hint="请输入姓名")
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="密码", field = "model.password", type = FormFieldType.PASSWORD,span=12,hint="请输入密码"),
				@FormFieldMeta(title="重复密码", field = "password", type = FormFieldType.PASSWORD,span=12,hint="请再次输入密码")
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="管理员组",field ="model.adminGroup.oid", type=FormFieldType.SELECT,hint="请选择管理员组",span=12,
					querySelect=@QuerySelectMeta(modelClass = "manage.model.AdminGroup", title = "name", value = "oid",
					conditions={@SelectConditionMeta(field = "status", value = "0"),@SelectConditionMeta(field = "type", value = "A")})
				),
				@FormFieldMeta(title="头像", field = "model.headImage.oid", type = FormFieldType.IMAGE,span=12,imageType="defaultHead",hint="请选择头像")
			})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageAdminLogin/doSave",success=FormSuccessMethod.DONE_BACK)
		}
	)
	public ActionResult toAdd() throws Exception{
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionFormMeta(title="修改管理员信息",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="账号(不可改)",field="model.username",type=FormFieldType.TEXT,span=12,hint="请输入账号",disabled=true),
				@FormFieldMeta(title="真实姓名",field="model.realname",type=FormFieldType.TEXT,span=12,hint="请输入姓名")
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="密码", field = "model.password", type = FormFieldType.PASSWORD,span=12,hint="请输入密码"),
				@FormFieldMeta(title="重复密码", field = "password", type = FormFieldType.PASSWORD,span=12,hint="请再次输入密码")
			}),
			@FormRowMeta(fields={
				@FormFieldMeta(title="管理员组",field ="model.adminGroup.oid", type=FormFieldType.SELECT,hint="请选择管理员组",span=12,
					querySelect=@QuerySelectMeta(modelClass = "manage.model.AdminGroup", title = "name", value = "oid",
					conditions={@SelectConditionMeta(field = "status", value = "0"),@SelectConditionMeta(field = "type", value = "A")})
				),
				@FormFieldMeta(title="头像", field = "model.headImage.oid", type = FormFieldType.IMAGE,span=12,imageType="defaultHead",hint="请选择头像")
			})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageAdminLogin/doSave",success=FormSuccessMethod.DONE_BACK,power="manage_system_power")
		}
	)
	public ActionResult toEdit() throws Exception{
		model=ModelQueryUtil.getModel(model);
		model.setPassword("");
		return getFormResult(this,ActionFormPage.EDIT);
	}
	@ActionFormMeta(title="修改登陆信息",
		rows={
			@FormRowMeta(fields={
				@FormFieldMeta(field = "model.oid", type = FormFieldType.HIDDEN),
				@FormFieldMeta(title="账号(不可改)",field="model.username",type=FormFieldType.TEXT,hint="请输入账号",disabled=true)
			}),
			@FormRowMeta(fields={@FormFieldMeta(title="真实姓名",field="model.realname",type=FormFieldType.TEXT,hint="请输入姓名")}),
			@FormRowMeta(fields={@FormFieldMeta(title="密码", field = "model.password", type = FormFieldType.PASSWORD,hint="空代表不修改密码")}),
			@FormRowMeta(fields={@FormFieldMeta(title="重复密码", field = "password", type = FormFieldType.PASSWORD,hint="请再次输入密码")}),
			@FormRowMeta(fields={@FormFieldMeta(title="头像", field = "model.headImage.oid", type = FormFieldType.IMAGE,imageType="defaultHead",hint="请选择头像")})
		},
		buttons={
			@FormButtonMeta(title = "保存", url = "action/manageAdminLogin/doSave4Self",success=FormSuccessMethod.DONE_BACK)
		}
	)
	public ActionResult toEdit4Self() throws Exception{
		model=getSessionAdmin();
		model.setPassword("");
		return getFormResult(this,ActionFormPage.EDIT);
	}
	/**
	 * 查询列表
	 * @return
	 */
	@ActionTableMeta(dataUrl = "action/manageAdminLogin/adminLoginData",
			modelClass="manage.model.AdminLogin",
			searchField="username,adminGroup.name",searchHint="请输入管理员账号或者管理员组名称",
		cols = { 
			@ActionTableColMeta(field = "oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "username", title = "账号", width=130),
			@ActionTableColMeta(field = "realname", title = "真实姓名", width=80),
			@ActionTableColMeta(field = "adminGroup.name", title = "管理员组", align="center"),
			@ActionTableColMeta(field = "lastLoginTime", title = "最后登陆时间", align="center", width=150, dateFormat="yyyy-MM-dd HH:mm"),
			@ActionTableColMeta(field = "loginCount", title = "登陆次数", width=70, numberFormat="#,##0", align="right"),
			@ActionTableColMeta(field = "status", title = "状态",type=TableColType.STATUS,power="manage_system_power",dictionaryType="status",align="center"),
			@ActionTableColMeta(field="oid",title="操作",width=180,buttons={
				@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=700, url = "action/manageAdminLogin/toEdit",
					params={@ParamMeta(name = "model.oid", field="oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL,
					power="manage_system_power"
				),
				@ButtonMeta(title="查看权限", event = ButtonEvent.MODAL,modalWidth=800, url = "page/manage/adminLogin/viewGroupMenuPage.html", 
					params={@ParamMeta(name = "adminOid", field="oid")}, 
					style=ButtonStyle.DEFAULT,power="manage_system_power"
				),
			})
		},
		querys = {
			@QueryMeta(field = "oid", name = "账号", type = QueryType.HIDDEN),
			@QueryMeta(field = "username", name = "账号", type = QueryType.TEXT, hint="请输入账号", likeMode=true),
			@QueryMeta(field = "adminGroup.oid", name = "管理员组", type = QueryType.SELECT, hint="请选择管理员组",
				querySelect=@QuerySelectMeta(modelClass = "manage.model.AdminGroup", title = "name", value = "oid")
			),
			@QueryMeta(field = "loginCount", name = "登陆次数", type = QueryType.INT_RANGE),
			@QueryMeta(field = "lastLoginTime", name = "最后登陆时间", type = QueryType.DATE_RANGE)
		},
		buttons = {
			@ButtonMeta(title="新增", event = ButtonEvent.MODAL,modalWidth=700, url = "action/manageAdminLogin/toAdd",
				success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL,
				power="manage_system_power"
			)
		}
	)
	public JSONMessage adminLoginData(){
		return getListDataResult(null);
	}
	
	

	/**
	 * 查询列表
	 * @return
	 */
	@ActionTableMeta(dataUrl = "action/manageAdminLogin/groupAdminData",
			modelClass="manage.model.AdminGroupLink",
		cols = { 
			@ActionTableColMeta(field = "admin.oid", title = "",type=TableColType.INDEX),
			@ActionTableColMeta(field = "admin.username", title = "用户", width=150,
				fieldExpression = "concat(#{admin.realname},' (',#{admin.username},')')"),
			@ActionTableColMeta(field = "admin.adminGroup.name", title = "管理员组", align="center"),
			@ActionTableColMeta(field = "admin.lastLoginTime", title = "最后登陆时间", align="center", width=150, dateFormat="yyyy-MM-dd HH:mm"),
			@ActionTableColMeta(field = "admin.loginCount", title = "登陆次数", width=70, numberFormat="#,##0", align="right"),
			@ActionTableColMeta(field = "admin.status", title = "状态",type=TableColType.STATUS,power="manage_system_power",dictionaryType="status",align="center"),
			@ActionTableColMeta(field="admin.oid",title="操作",width=180,buttons={
				@ButtonMeta(title="修改", event = ButtonEvent.MODAL,modalWidth=700, url = "action/manageAdminLogin/toEdit",
					params={@ParamMeta(name = "model.oid", field="admin.oid")},success=SuccessMethod.REFRESH,style=ButtonStyle.NORMAL,
					power="manage_system_power"
				),
				@ButtonMeta(title="查看权限", event = ButtonEvent.MODAL,modalWidth=800, url = "page/manage/adminLogin/viewGroupMenuPage.html", 
					params={@ParamMeta(name = "adminOid", field="admin.oid")}, 
					style=ButtonStyle.DEFAULT,power="manage_system_power"
				),
			})
		},
		querys = {
			@QueryMeta(field = "adminGroup.oid", name = "组", type = QueryType.HIDDEN),
			@QueryMeta(field = "admin.username", name = "账号", type = QueryType.TEXT, hint="请输入账号", likeMode=true),
			@QueryMeta(field = "admin.adminGroup.oid", name = "管理员组", type = QueryType.SELECT, hint="请选择管理员组",
				querySelect=@QuerySelectMeta(modelClass = "manage.model.AdminGroup", title = "name", value = "oid")
			),
			@QueryMeta(field = "admin.loginCount", name = "登陆次数", type = QueryType.INT_RANGE),
			@QueryMeta(field = "admin.lastLoginTime", name = "最后登陆时间", type = QueryType.DATE_RANGE)
		},
		buttons = {
			@ButtonMeta(title="新增", event = ButtonEvent.MODAL,modalWidth=700, url = "action/manageAdminLogin/toAdd",
				queryParams = {@ParamMeta(name = "link.adminGroup.oid", field="adminGroup.oid")},
				style=ButtonStyle.NORMAL,power="manage_system_power",success=SuccessMethod.REFRESH
			),
			@ButtonMeta(title="关联用户", event = ButtonEvent.MODAL,modalWidth=900,  url = "page/manage/adminGroupLink/setAdminGroupLinkPage.html", 
				queryParams = {@ParamMeta(name = "adminGroupOid", field="adminGroup.oid")},
				style=ButtonStyle.NONE,power="manage_system_power",success = SuccessMethod.REFRESH
			),
		},dropButtons= {
			@DropButtonMeta(title = "权限",buttons = { 
				@ButtonMeta(title="菜单权限", event = ButtonEvent.MODAL,modalWidth=800,  url = "page/manage/groupMenuLink/setGroupMenuPage.html", 
					queryParams={@ParamMeta(name = "adminGroupOid", field="adminGroup.oid")},
					power="manage_system_power", style=ButtonStyle.NONE
				),
				@ButtonMeta(title="操作权限", event = ButtonEvent.MODAL,modalWidth=350,  url = "action/manageAdminGroupPower/setAdminGroupPowerPage", 
					queryParams={@ParamMeta(name = "model.adminGroup.oid", field="adminGroup.oid")},success=SuccessMethod.MUST_REFRESH,
					power="manage_system_power", style=ButtonStyle.NONE
				),
			})
		}
	)
	public JSONMessage groupAdminData(){
		return getListDataResult(null);
	}
	public AdminLogin getModel() {
		return model;
	}
	public void setModel(AdminLogin model) {
		this.model = model;
	}
	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}
	@Override
	public String getStatusPower() {
		return "manage_system_power";
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getImageCode() {
		return imageCode;
	}
	public void setImageCode(String imageCode) {
		this.imageCode = imageCode;
	}
	public String getAutoLogin() {
		return autoLogin;
	}
	public AdminGroupLink getLink() {
		return link;
	}
	public void setLink(AdminGroupLink link) {
		this.link = link;
	}
	public void setAutoLogin(String autoLogin) {
		this.autoLogin = autoLogin;
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
