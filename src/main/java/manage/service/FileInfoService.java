package manage.service;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import m.common.model.util.ModelUpdateUtil;
import m.common.service.Service;
import m.system.RuntimeData;
import m.system.db.DBManager;
import m.system.db.DataRow;
import m.system.db.SqlBuffer;
import m.system.exception.MException;
import m.system.util.DateUtil;
import m.system.util.FileUtil;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.model.FileInfo;

public class FileInfoService extends Service {

	/**
	 * 获取文件名称
	 * @param oid
	 * @return
	 */
	public static String getFileName(String oid){
		try {
			if(StringUtil.isSpace(oid))return "";
			if(oid.indexOf("\"")==0)oid=oid.substring(1);
			if(oid.lastIndexOf("\"")==oid.length()-1)oid=oid.substring(0,oid.length()-1);
			DataRow dr = DBManager.queryFirstRow("select name from os_file_info where oid=?",new String[]{oid});
			if(null!=dr){
				return dr.get(String.class, "name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 保存图片
	 * @param model
	 * @param file
	 * @throws Exception
	 */
	public void saveFile(FileInfo model, File file) throws Exception {
		String key=GenerateID.generatePrimaryKey();
		if(null==model.getPath()||model.getPath().equals("/")) {//null or "/"  上传文件目录/file/...
			model.setPath(new StringBuffer(RuntimeData.getFilePath()).append("file/").append(DateUtil.format(new Date(),"yyyyMM")).append("/").append(DateUtil.format(new Date(),"ddHH")).append("/").toString());
			model.setFilePath(new StringBuffer(model.getPath()).append(key).append("_").append(model.getName()).toString());
		}else if(!StringUtil.isSpace(model.getPath())){//非根目录  存放文件名唯一
			model.setFilePath(new StringBuffer(model.getPath()).append(key).append("_").append(model.getName()).toString());
		}else {//跟目录
			model.setFilePath(new StringBuffer(model.getPath()).append(model.getName()).toString());
			verifyPower(model.getFilePath(), model.getImageAdmin().getOid());
		}
		model.setOid(key);
		model.setCreateDate(new Date());
		FileUtil.writeWebFile(model.getFilePath(), file);
		ModelUpdateUtil.insertModel(model);
	}
	private void verifyPower(String filePath,String adminOid) throws MException, SQLException {
		SqlBuffer sql=new SqlBuffer();
		sql.append("select image_admin_oid from os_file_info where file_path=?",filePath);
		DataRow dr=sql.queryFirstRow();
		if(null!=dr&&!dr.get(String.class,"image_admin_oid").equals(adminOid)) {
			throw new MException(this.getClass(), "该文件已被占用,无权上传修改");
		}
	}
}
