package manage.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.common.model.util.ModelCheckUtil;
import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.model.util.QueryOrder;
import m.common.service.Service;
import m.system.cache.CacheUtil;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.ArrayUtil;
import m.system.util.DateUtil;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.dao.AdminLoginDao;
import manage.model.AdminGroupLink;
import manage.model.AdminLogin;

public class AdminLoginService extends Service {
	private Map<String,Date> errorDate=new HashMap<String, Date>();
	private Map<String,Integer> errorNum=new HashMap<String, Integer>();
	private void errorVerify(String username) throws MException{
		Date date=errorDate.get(username);
		Integer num=errorNum.get(username);
		if(null==date||DateUtil.add(date, Calendar.HOUR_OF_DAY, 1).getTime()<new Date().getTime()){
			errorDate.put(username, new Date());
			errorNum.put(username, 1);
		}else if(null!=num&&num<10){
			errorNum.put(username, num+1);
		}else{
			throw new MException(this.getClass(),"操作频繁,请稍后再试.");
		}
	}
	public AdminLogin loginVerification(AdminLogin model) throws Exception{
		if(StringUtil.isSpace(model.getUsername())){
			throw new MException(this.getClass(),"账号不能为空!");
		}else if(StringUtil.isSpace(model.getPassword())){
			throw new MException(this.getClass(),"密码不能为空!");
		}else{
			AdminLoginDao dao=getDao(AdminLoginDao.class);
			model.setPassword(StringUtil.toMD5(model.getPassword()));
			String user_oid=dao.getUserOid(model.getUsername(),model.getPassword());
			if(StringUtil.isSpace(user_oid)){
				errorVerify(model.getUsername());
				throw new MException(this.getClass(),"账号或者密码错误!");
			}else{
				model=CacheUtil.get(AdminLogin.class,user_oid);
				if("9".equals(model.getStatus())){
					throw new MException(this.getClass(),"账号已被停用!");
				}
				//model.setToken(UUID.randomUUID().toString());
				//ModelUpdateUtil.updateModel(model,new String[]{"token"});
				return model;
			}
		}
	}
	/**
	 * 保存
	 * @param model
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public String save(AdminLogin model, String password) throws Exception {
		return save(model,null,password);
	}
	/**
	 * 保存并关联到用户组
	 * @param model
	 * @param link 只有新增才关联
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public String save(AdminLogin model,AdminGroupLink link, String password) throws Exception {
		if(!password.equals(model.getPassword())){
			throw new MException(this.getClass(),"两次输入的密码不一致");
		}
		ModelCheckUtil.checkUniqueCombine(model, new String[]{"username"});
		String msg="";
		TransactionManager tm=new TransactionManager();
		try{
			tm.begin();
			if(StringUtil.isSpace(model.getOid())){
				model.setOid(GenerateID.generatePrimaryKey());
				model.setCreateDate(new Date());
				model.setStatus("0");
				model.setPassword(StringUtil.toMD5(model.getPassword()));
				ModelUpdateUtil.insertModel(model);
				if(null!=link&&null!=link.getAdminGroup()&&!StringUtil.isSpace(link.getAdminGroup().getOid())) {
					link.setAdminGroup(ModelQueryList.getModel(link.getAdminGroup(), new String[] {"oid"}));
					if(null!=link.getAdminGroup()) {
						link.setOid(GenerateID.generatePrimaryKey());
						link.setAdmin(model);
						ModelUpdateUtil.insertModel(link);
					}else {
						throw new MException(this.getClass(), "需要关联的组不存在");
					}
				}
				msg="保存成功";
			}else{
				if(model.getOid().equals("1")&&!model.getAdminGroup().getOid().equals("1")){
					throw new MException(this.getClass(),"admin帐号不能修改管理员组");
				}
				if(StringUtil.isSpace(model.getPassword())){
					ModelUpdateUtil.updateModel(model, new String[]{"realname","adminGroup.oid","headImage.oid"});
				}else{
					model.setPassword(StringUtil.toMD5(model.getPassword()));
					ModelUpdateUtil.updateModel(model, new String[]{"realname","password","adminGroup.oid","headImage.oid"});
				}
				msg="修改成功";
			}
			ImageLinkService.addOnlyImageLink(model.getOid(),"头像", model.getHeadImage());//添加业务对应的唯一图片
			tm.commit();
		}catch(Exception e){
			tm.rollback();
			throw e;
		}
		return msg;
	}
	/**
	 * 获取全部管理员
	 * @param nodeGroup 节点组  空代表全查
	 * @param flterGroup 过滤组 空代表全查
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	public List<AdminLogin> getAll(String nodeGroup,String flterGroup) throws SQLException, MException{
		List<QueryCondition> cons=new ArrayList<QueryCondition>();
		if(!StringUtil.isSpace(nodeGroup)) {
			String[] nodes=ArrayUtil.toNoEmptyArray(StringUtil.noSpace(nodeGroup).split(","));
			List<QueryCondition> qc=new ArrayList<QueryCondition>();
			qc.addAll(QueryCondition.manyEq("adminGroup.oid", nodes));
			qc.addAll(QueryCondition.manyEq("adminGroup.num", nodes));
			qc.add(
				QueryCondition.in("oid", 
					ModelQueryList.instance(AdminGroupLink.class, new String[] {"admin.oid"}, null, 
						QueryCondition.or(new QueryCondition[] {
							QueryCondition.in("adminGroup.oid", nodes),
							QueryCondition.in("adminGroup.num", nodes),
							QueryCondition.in("adminGroup.parent.oid", nodes),
							QueryCondition.in("adminGroup.parent.num", nodes)
						})
					)
				)
			);
			cons.add(QueryCondition.or(qc.toArray(new QueryCondition[] {})));
		}
		if(!StringUtil.isSpace(flterGroup)) {
			String[] flter=ArrayUtil.toNoEmptyArray(StringUtil.noSpace(flterGroup).split(","));
			List<QueryCondition> qc=new ArrayList<QueryCondition>();
			qc.addAll(QueryCondition.manyEq("adminGroup.oid", flter));
			qc.addAll(QueryCondition.manyEq("adminGroup.num", flter));
			qc.add(
				QueryCondition.in("oid", 
					ModelQueryList.instance(AdminGroupLink.class, new String[] {"admin.oid"}, null, 
						QueryCondition.or(new QueryCondition[] {
							QueryCondition.in("adminGroup.oid", flter),
							QueryCondition.in("adminGroup.num", flter),
							QueryCondition.in("adminGroup.parent.oid", flter),
							QueryCondition.in("adminGroup.parent.num", flter)
						})
					)
				)
			);
			cons.add(QueryCondition.or(qc.toArray(new QueryCondition[] {})));
		}
		cons.add(QueryCondition.eq("status", "0"));
		return ModelQueryList.getModelList(AdminLogin.class, new String[] {"*","adminGroup.*"},null, 
			QueryCondition.and(cons.toArray(new QueryCondition[] {})), QueryOrder.asc("adminGroup.sort"));
	}
}
