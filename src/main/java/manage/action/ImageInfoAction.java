package manage.action;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.model.type.FieldType;
import m.common.model.util.ModelQueryUtil;
import m.common.model.util.QueryOrder;
import m.common.model.util.QueryPage;
import m.system.RuntimeData;
import m.system.document.DocumentMeta;
import m.system.document.DocumentMethodMeta;
import m.system.document.DocumentParamMeta;
import m.system.exception.MException;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.model.AdminLogin;
import manage.model.ImageAdmin;
import manage.model.ImageInfo;
import manage.service.ImageAdminService;
import manage.service.ImageInfoService;

@ActionMeta(name="manageImageInfo",title="系统-图片管理")
public class ImageInfoAction extends ManageAction {
	private String adminToken;
	private Boolean isUsed;
	private String selected;
	private String field;
	private String imageType;
	private String businessOid;
	private String imageOid;
	private Double thumRatio=1.0;
	private Double thumWidth=500.0;
	private String[] synchPath;
	private String[] synchName;
	/**
	 * 接收非主控服务器同步过来的文件
	 * @return
	 * @throws MException 
	 */
//	public HtmlBodyContent synchFile() throws MException{
//		for(int i=0;i<synchName.length;i++){
//			getService(ImageInfoService.class).saveSynchFile(synchPath[i], super.getFileMap().get(synchName[i]));
//		}
//		return new HtmlBodyContent("success");
//	}
	
	/**
	 * 图片列表
	 * @return
	 */
	public ActionResult toImageList(){
		ActionResult result=new ActionResult("manage/image/imageList");
		return result;
	}
	/**
	 * 上传图片
	 * @return
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="上传图片",description="图片上传后没有关联业务",permission=true,
			result="无法调试, 调用接口需要传输文件流."),
		params={
			@DocumentParamMeta(name="adminToken",description="token",type=FieldType.STRING,length=20),
			@DocumentParamMeta(name="imageType",description="图片类型",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="thumRatio",description="缩略图比例",type=FieldType.DOUBLE),
			@DocumentParamMeta(name="thumWidth",description="缩略图宽",type=FieldType.DOUBLE),
		}
	)
	public JSONMessage uploadImage(){
		JSONMessage message=new JSONMessage();
		try {
			ImageInfo model=new ImageInfo();
			ImageAdmin ia=new ImageAdmin();
			if(StringUtil.isSpace(adminToken)||StringUtil.noSpace(adminToken).length()<3){
				AdminLogin admin=getSessionAdmin();
				if(null==admin) throw noLoginException;
				ia.setOid(admin.getOid());
			}else{
				ia.setOid(getService(ImageAdminService.class).getOid(adminToken));
			}
			Map<String,File> map=super.getFileMap();
			if(null==map||map.size()<1) throw new MException(this.getClass(),"没有接收到图片");
			for(String key : map.keySet()){
				model.setOid("");
				model.setImageType(imageType);
				model.setImageAdmin(ia);
				model.setThumRatio(thumRatio);
				model.setThumWidth(thumWidth);
				getService(ImageInfoService.class).saveImage(model, map.get(key));
				message.push("model", model);
				JSONMessage data=new JSONMessage();
				data.push("src", model.getImgPath());
				data.push("title", "");
				message.push("data", data);
				break;
			}
			message.push("code", 0);
		} catch (Exception e) {
			message.push("code", 1);
			message.push("msg", e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	/**
	 * 选择图片数据
	 * @return
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="图片列表",description="查询自己上传的所有图片",permission=true),
		params={
			@DocumentParamMeta(name="adminToken",description="token",type=FieldType.STRING,length=20),
			@DocumentParamMeta(name="imageType",description="图片类型",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="isUsed",description="是否已使用",type=FieldType.STRING,length=10),
			@DocumentParamMeta(name="page.index",description="分页开始位置",type=FieldType.INT),
			@DocumentParamMeta(name="page.num",description="分页每页数量",type=FieldType.INT),
		}
	)
	public JSONMessage imageList(){
		JSONMessage message=new JSONMessage();
		try{
			ImageAdmin ia=new ImageAdmin();
			if(StringUtil.isSpace(adminToken)||StringUtil.noSpace(adminToken).length()<3){
				AdminLogin admin=getSessionAdmin();
				if(null==admin) throw noLoginException;
				ia.setOid(admin.getOid());
			}else{
				ia.setOid(getService(ImageAdminService.class).getOid(adminToken));
			}
			message.push("list", getService(ImageInfoService.class).getImageList(ia, getPage(),imageType, isUsed));
			message.push("code", 0);
		}catch(Exception e){
			message.push("msg", e.getMessage());
			message.push("code", 1);
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	/**
	 * 选择图片页面
	 * @return
	 */
	public ActionResult selectImagePage(){
		ActionResult result=new ActionResult("manage/image/imageList");
		List<String> arr=new ArrayList<String>();
		if(!StringUtil.isSpace(selected)){
			for(String oid : selected.split(",")){
				arr.add(oid);
			}
		}
		result.setArray(arr);
		result.setHtmlBody(imageType);
		result.setPower(adminToken);
		result.setMap(new HashMap<String, Object>());
		result.getMap().put("thumRatio", thumRatio);
		result.getMap().put("thumWidth", thumWidth);
		result.getMap().put("openKey", getOpenKey());
		result.getMap().put("field", field);
		return result;
	}
	/**
	 * 删除图片方法
	 * @return
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="删除图片",description="已使用的不能删除",permission=true),
		params={
			@DocumentParamMeta(name="adminToken",description="token",type=FieldType.STRING,length=20),
			@DocumentParamMeta(name="imageOid",description="图片oid",type=FieldType.STRING,length=20,notnull=true),
		}
	)
	public JSONMessage delete(){
		JSONMessage message=new JSONMessage();
		try{
			ImageAdmin ia=new ImageAdmin();
			if(StringUtil.isSpace(adminToken)||StringUtil.noSpace(adminToken).length()<3){
				AdminLogin admin=getSessionAdmin();
				if(null==admin) throw noLoginException;
				ia.setOid(admin.getOid());
			}else{
				ia.setOid(getService(ImageAdminService.class).getOid(adminToken));
			}
			message.push("oid", getService(ImageInfoService.class).delete(ia, imageOid));
			message.push("msg", "删除成功");
			message.push("code", 0);
		}catch(Exception e){
			message.push("msg", e.getMessage());
			message.push("code", 1);
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	@DocumentMeta(
		method=@DocumentMethodMeta(title="图片详情",description="根据图片oid获取图片信息"),
		params={
			@DocumentParamMeta(name="imageOid",description="图片oid",type=FieldType.STRING,length=20,notnull=true),
		}
	)
	public JSONMessage imageInfo(){
		JSONMessage message=new JSONMessage();
		try{
			ImageInfo image=new ImageInfo();
			image.setOid(imageOid);
			image=ModelQueryUtil.getModel(image);
			message.push("model", image);
			message.push("code", 0);
		}catch(Exception e){
			message.push("msg", e.getMessage());
			message.push("code", 1);
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
		
	}
	/**
	 * 查看图片
	 * @return
	 * @throws Exception
	 */
	public ActionResult viewImage() throws Exception{
		ActionResult result=new ActionResult("manage/image/viewImage");
		result.setModel(new ImageInfo());
		result.getModel().setOid(imageOid);
		result.setModel(ModelQueryUtil.getModel(result.getModel()));
		return result;
	}
	public ActionResult viewImages() throws Exception {
		ActionResult result=new ActionResult("manage/image/viewImages");
		String[] oids=imageOid.split(",");
		result.setList(getService(ImageInfoService.class).getImageList(oids, new QueryPage(0,oids.length), QueryOrder.asc("createDate")));
		return result;
	}
	/**
	 * 上传图片
	 * @return
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="上传业务图片",description="根据业务oid上传图片",permission=true,
			result="无法调试, 调用接口需要传输文件流."),
		params={
			@DocumentParamMeta(name="adminToken",description="token",type=FieldType.STRING,length=20),
			@DocumentParamMeta(name="businessOid",description="业务主键",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="imageType",description="图片类型",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="thumRatio",description="缩略图比例",type=FieldType.DOUBLE),
			@DocumentParamMeta(name="thumWidth",description="缩略图宽",type=FieldType.DOUBLE),
		}
	)
	public JSONMessage uploadBusinessImage(){
		JSONMessage message=new JSONMessage();
		try {
			ImageInfo model=new ImageInfo();
			ImageAdmin ia=new ImageAdmin();
			if(StringUtil.isSpace(adminToken)||StringUtil.noSpace(adminToken).length()<3){
				AdminLogin admin=getSessionAdmin();
				if(null==admin) throw noLoginException;
				ia.setOid(admin.getOid());
			}else{
				ia.setOid(getService(ImageAdminService.class).getOid(adminToken));
			}
			if(StringUtil.isSpace(businessOid)) throw new MException(this.getClass(),"业务主键为空!");
			Map<String,File> map=super.getFileMap();
			if(null==map||map.size()<1) throw new MException(this.getClass(),"没有接收到图片");
			for(String key : map.keySet()){
				model.setOid("");
				model.setImageType(imageType);
				model.setImageAdmin(ia);
				model.setThumRatio(thumRatio);
				model.setThumWidth(thumWidth);
				getService(ImageInfoService.class).saveImageAndSelect(model, map.get(key),businessOid);
				message.push("model", model);
				JSONMessage data=new JSONMessage();
				data.push("src", model.getImgPath());
				data.push("title", "");
				message.push("data", data);
			}
			message.push("code", 0);
		} catch (Exception e) {
			message.push("code", 1);
			message.push("msg", e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	/**
	 * 删除图片方法 业务列表
	 * @return
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="删除业务图片",description="删除根据业务oid上传的图片",permission=true),
		params={
			@DocumentParamMeta(name="adminToken",description="token",type=FieldType.STRING,length=20),
			@DocumentParamMeta(name="imageOid",description="图片oid",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="businessOid",description="业务oid",type=FieldType.STRING,length=20,notnull=true),
		}
	)
	public JSONMessage deleteBusinessImage(){
		JSONMessage message=new JSONMessage();
		try{
			ImageAdmin ia=new ImageAdmin();
			if(StringUtil.isSpace(adminToken)||StringUtil.noSpace(adminToken).length()<3){
				AdminLogin admin=getSessionAdmin();
				if(null==admin) throw noLoginException;
				ia.setOid(admin.getOid());
			}else{
				ia.setOid(getService(ImageAdminService.class).getOid(adminToken));
			}
			if(StringUtil.isSpace(businessOid)) throw new MException(this.getClass(),"业务主键为空!");
			message.push("oid", getService(ImageInfoService.class).deleteBusiness(ia,imageOid,businessOid));
			message.push("msg", "删除成功");
			message.push("code", 0);
		}catch(Exception e){
			message.push("msg", e.getMessage());
			message.push("code", 1);
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	/**
	 * 查看对应业务的图片列表
	 * @return
	 */
	public ActionResult toBusinessImageList(){
		ActionResult result=new ActionResult("manage/image/businessImageList");
		result.setMap(new HashMap<String,Object>());
		result.getMap().put("imageType", imageType);
		result.getMap().put("businessOid", businessOid);
		result.getMap().put("adminToken", adminToken);
		result.getMap().put("thumRatio", thumRatio);
		result.getMap().put("thumWidth", thumWidth);
		result.setPower(null!=getPower()&&getPower().toString().equals("view")?false:true);
		return result;
	}
	/**
	 * 查看对应业务的图片数据
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	@DocumentMeta(
		method=@DocumentMethodMeta(title="业务图片列表",description="根据业务oid获取所有关联的图片"),
		params={
			@DocumentParamMeta(name="businessOid",description="业务oid",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="imageType",description="图片类型",type=FieldType.STRING,length=20,notnull=true),
			@DocumentParamMeta(name="page.index",description="分页开始位置",type=FieldType.INT),
			@DocumentParamMeta(name="page.num",description="分页每页数量",type=FieldType.INT),
		}
	)
	public JSONMessage businessImageList() throws SQLException, MException{
		JSONMessage message=new JSONMessage();
		try{
			message.push("list", getService(ImageInfoService.class).getImageList(this.getBusinessOid(), getPage(), getImageType()));
			message.push("code", 0);
		}catch(Exception e){
			message.push("msg", e.getMessage());
			message.push("code", 1);
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}

	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}

	public Boolean getIsUsed() {
		return isUsed;
	}
	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}
	public String getSelected() {
		return selected;
	}
	public void setSelected(String selected) {
		this.selected = selected;
	}
	public String getImageOid() {
		return imageOid;
	}
	public void setImageOid(String imageOid) {
		this.imageOid = imageOid;
	}
	public String getAdminToken() {
		return adminToken;
	}
	public void setAdminToken(String adminToken) {
		this.adminToken = adminToken;
	}
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	public String getBusinessOid() {
		return businessOid;
	}
	public void setBusinessOid(String businessOid) {
		this.businessOid = businessOid;
	}
	public Double getThumRatio() {
		return thumRatio;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public void setThumRatio(Double thumRatio) {
		this.thumRatio = thumRatio;
	}
	public Double getThumWidth() {
		return thumWidth;
	}
	public void setThumWidth(Double thumWidth) {
		this.thumWidth = thumWidth;
	}

	public String[] getSynchPath() {
		return synchPath;
	}

	public void setSynchPath(String[] synchPath) {
		this.synchPath = synchPath;
	}

	public String[] getSynchName() {
		return synchName;
	}

	public void setSynchName(String[] synchName) {
		this.synchName = synchName;
	}



}
