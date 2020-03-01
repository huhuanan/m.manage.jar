package manage.flow.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.model.util.QueryOrder;
import m.common.model.util.QueryParameter;
import m.common.service.Service;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.JSONMessage;
import m.system.util.StringUtil;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowInstance;
import manage.flow.model.FlowInstanceSection;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowSectionLink;
import manage.flow.util.FlowUtil;
import manage.model.AdminLogin;
import manage.model.OrgGroupView;

public class FlowInstanceService extends Service {
	public JSONMessage getInstance(FlowInstance model) throws Exception {
		model=ModelQueryList.getModel(FlowInstance.class, model.getOid(), new String[] {"*","business.*"});
		JSONMessage result=new JSONMessage();
		result.push("model", model);
		result.push("list", ModelQueryList.getModelList(FlowInstanceSection.class, new String[] {"*","flowSection.*","user.*","org.*"}, null, 
			QueryCondition.eq("flowInstance.oid", model.getOid()),
			QueryOrder.asc("acceptDate"))
		);
		return result;
	}
	public String doStart(FlowBusiness business,List<AdminLogin> adminList,List<OrgGroupView> orgList) throws MException, Exception {
		start(FlowUtil.getFlowId(business.getBusiService()),business,adminList,orgList);
		return "启动成功";
	}
	/**
	 * 处理流程 
	 * @param admin 当前处理人
	 * @param model 流程实例
	 * @param nextLink 下一环节链接
	 * @param adminList 下一环节执行人
	 * @return
	 * @throws Exception
	 */
	public String doProcess(AdminLogin admin,FlowInstance model,FlowSectionLink nextLink,List<AdminLogin> adminList,List<OrgGroupView> orgList) throws Exception {
		String msg="";
		model=ModelQueryList.getModel(model,new String[] {"*","flowDefine.*","business.*"});
		if(null==model) throw new MException(this.getClass(), "流程实例不存在");
		if(null==nextLink) {
			throw new MException(this.getClass(), "请选择下一步");
		}else if(StringUtil.isSpace(nextLink.getOid())) {
			if(adminList.size()!=1) throw new MException(this.getClass(), "请选择一个转发执行人");
			forward(model.getFlowDefine().getIdentity(), model.getBusiness(), admin, adminList.get(0));
			msg="转发成功";
		}else {
			nextLink=ModelQueryList.getModel(nextLink, new String[] {"*","fromSection.*","toSection.*"});
			if(null==nextLink) throw new MException(this.getClass(), "流程环节不存在");
			if("Y".equals(nextLink.getIsNext())) {
				next(model.getFlowDefine().getIdentity(),nextLink.getToParam(), model.getBusiness(), admin, adminList,orgList);
				msg="操作成功";
			}else if("N".equals(nextLink.getIsNext())) {
				finish(model.getFlowDefine().getIdentity(),nextLink.getToParam(), model.getBusiness(), admin);
				msg="已完成";
			}else if("B".equals(nextLink.getIsNext())) {
				back(model.getFlowDefine().getIdentity(), model.getBusiness(), admin);
				msg="退回成功";
			}
		}
		return msg;
	}
	
	/**
	 * 开始流程
	 * @param flowId 流程标识 
	 * @param business 流程业务
	 * @param user 开始流程人
	 * @throws Exception
	 */
	public void start(String flowId,FlowBusiness business,List<AdminLogin> userList,List<OrgGroupView> orgList) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(business.getOid());
			FlowInstance ins=getInstance(flowId,business.getOid());
			if(null!=ins) throw new MException(this.getClass(), "该业务已启动流程");
			ins=new FlowInstance();
			ins.setBusiness(business);
			ins.setFlowDefine(getService(FlowDefineService.class).getIssue(flowId));
			if(null==ins.getFlowDefine()) throw new MException(this.getClass(), "流程不存在");
			ins.setCurrentSection(ins.getFlowDefine().getStartSection());
			ins.setCurrentIndex(1);
			ins.setOid(GenerateID.generatePrimaryKey());
			ins.setCreateDate(new Date());
			ins.setDoneStatus("N");
			ModelUpdateUtil.insertModel(ins);
			getService(FlowBusinessService.class).updateCurrentSection(business.getOid(), ins.getCurrentSection(), ins.getCurrentIndex());
			FlowInstanceSection sec=new FlowInstanceSection();
			sec.setFlowInstance(ins);
			sec.setFlowSection(ins.getCurrentSection());
			sec.setFlowIndex(ins.getCurrentIndex());
			sec.setAcceptDate(new Date());
			sec.setDoneStatus("N");
			insertInsSec4Process(sec, business, ins.getFlowDefine().getStartOption(), userList, orgList);
			FlowUtil.getService(business.getBusiService()).startFlowAfter(ins);//流程执行后回调
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	/**
	 * 流程转发
	 * @param flowId 流程标识
	 * @param business 流程业务
	 * @param user 执行流程人
	 * @param nextUser 下一步执行人
	 * @throws Exception
	 */
	public void forward(String flowId,FlowBusiness business,AdminLogin user,AdminLogin nextUser) throws Exception {
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(business.getOid());
			FlowInstance ins=getInstance(flowId,business.getOid());
			if(null==ins) throw new MException(this.getClass(), "该流程实例不存在");
			if(!StringUtil.noSpace(ins.getCurrentSection().getForwardable()).equals("Y")) {
				throw new MException(this.getClass(), "该环节不支持转发");
			}
			if(user.getOid().equals(nextUser.getOid())) throw new MException(this.getClass(), "不能转发给自己");
			FlowInstanceSection sec=getInstanceSection(ins.getOid(), ins.getCurrentSection().getOid(), user.getOid(),user.getOrgGroup().getOid());
			if(null==sec) throw new MException(this.getClass(), "实例环节不存在");
			if(null==sec.getUser()) throw new MException(this.getClass(), "部门的实例环节不支持转发");
			sec.setNext("F");
			sec.setToDesc("转发");
			sec.setDoneDate(new Date());
			sec.setDoneStatus("Y");
			sec.setUser(user);
			sec.setOrg(user.getOrgGroup());
			ModelUpdateUtil.updateModel(sec, new String[] {"next","doneDate","doneStatus","user.oid","org.oid"});
			sec.setOid(GenerateID.generatePrimaryKey());
			sec.setUser(nextUser);
			sec.setOrg(null);
			sec.setAcceptDate(new Date());
			sec.setNext(null);
			sec.setDoneDate(null);
			sec.setDoneStatus("N");
			ModelUpdateUtil.insertModel(sec);
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
	}
	/**
	 * 执行流程下一步
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @param nextUsers 下一步流程人
	 * @return 下一步流程环节，通过后才返回
	 * @throws Exception
	 */
	public FlowInstance next(String flowId,String param,FlowBusiness business,AdminLogin user,List<AdminLogin> nextUsers,List<OrgGroupView> nextOrgs) throws Exception {
		FlowInstance ins=getInstance(flowId,business.getOid());
		String p=toNext(ins,"Y",param,business,user,nextUsers,nextOrgs);
		FlowUtil.getService(business.getBusiService()).processFlowAfter(p,ins);
		return ins;
	}
	/**
	 * 执行流程退回
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @return 下一步流程环节，通过后才返回
	 * @throws Exception 
	 */
	public FlowInstance back(String flowId,FlowBusiness business,AdminLogin user) throws Exception {
		FlowInstance ins=getInstance(flowId,business.getOid());
		String p=toNext(ins,"B","",business,user,new ArrayList<AdminLogin>(),new ArrayList<OrgGroupView>());
		FlowUtil.getService(business.getBusiService()).processFlowAfter(p,ins);
		return ins;
	}
	/**
	 * 完成流程
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @throws Exception
	 */
	public FlowInstance finish(String flowId,String param,FlowBusiness business,AdminLogin user) throws Exception {
		FlowInstance ins=getInstance(flowId,business.getOid());
		String p=toNext(ins,"N",param,business,user,new ArrayList<AdminLogin>(),new ArrayList<OrgGroupView>());
		FlowUtil.getService(business.getBusiService()).processFlowAfter(p,ins);
		return ins;
	}
	private String toNext(FlowInstance ins,String isNext,String param,FlowBusiness business,AdminLogin user,List<AdminLogin> nextUsers,List<OrgGroupView> nextOrgs) throws Exception {
		String p="";
		FlowSection result=null;
		if(null==ins) throw new MException(this.getClass(), "该流程实例不存在");
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(business.getOid());
			ins.setBusiness(business);
			FlowInstanceSection sec=getInstanceSection(ins.getOid(), ins.getCurrentSection().getOid(), user.getOid(),user.getOrgGroup().getOid());
			if(null==sec) throw new MException(this.getClass(), "当前登录人的实例环节不存在");
			sec.setNext(isNext);
			sec.setParam(param);
			sec.setDoneDate(new Date());
			sec.setDoneStatus("Y");
			sec.setUser(user);
			sec.setOrg(user.getOrgGroup());
			ModelUpdateUtil.updateModel(sec, new String[] {"next","param","doneDate","doneStatus","user.oid","org.oid"});
			if(!"Y".equals(ins.getCurrentSection().getCountersign())) {//非会签，设置该环节其他待办跳过
				setPassInstanceSection(ins.getOid(), ins.getCurrentSection().getOid());
			}
			FlowSectionLink link=getService(FlowSectionService.class).getLink(ins.getCurrentSection().getFlowDefine().getOid(),ins.getCurrentSection().getIdentity(), isNext, param);
			sec.setToDesc(link.getToDesc());
			ModelUpdateUtil.updateModel(sec, new String[] {"toDesc"});
			if(isOverInstanceSection(ins.getOid(), ins.getCurrentSection().getOid())) {//当前环节代表是否都完成
				ins.setCurrentIndex(ins.getCurrentIndex()+1);
				if(instanceSectionResult(ins.getOid(), ins.getCurrentSection().getOid(), ins.getCurrentIndex()-1)) {//统计结果是否一致
					//按照当前环节走下一步
					p=link.getToParam();
					if(!"N".equals(link.getIsNext())) {//下一步 or 退回
						if(isNext.equals("Y")) {
							toNextSection(ins,link, nextUsers, nextOrgs);
						}else {
							toBackSection(ins,link);
						}
						result=link.getToSection();
					}else {//结束
						ins.setDoneDate(new Date());
						ins.setDoneStatus("Y");
						ModelUpdateUtil.updateModel(ins, new String[] {"doneDate","doneStatus"});
						result=new FlowSection();
					}
				}else {//如果是会签，结果不一致，走退回环节
					link=getService(FlowSectionService.class).getLink(ins.getCurrentSection().getFlowDefine().getOid(),ins.getCurrentSection().getIdentity(), "B", "");
					//走当前环节的退回
					result=toBackSection(ins,link);
				}
				if(null==result) throw new MException(this.getClass(), "未找到下一环节");
				ins.setCurrentSection(result);
				ModelUpdateUtil.updateModel(ins, new String[] {"currentSection.oid","currentIndex"});
				getService(FlowBusinessService.class).updateCurrentSection(business.getOid(), ins.getCurrentSection(), ins.getCurrentIndex());
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
		return p;
	}
	private FlowSection toBackSection(FlowInstance ins,FlowSectionLink link) throws SQLException, MException {
		List<FlowInstanceSection> list=ModelQueryList.getModelList(FlowInstanceSection.class,
			new String[] {"flowInstance.oid","flowSection.oid","user.oid","org.oid"},null,
			QueryCondition.and(new QueryCondition[]{
				QueryCondition.eq("flowInstance.oid", ins.getOid()),
				QueryCondition.eq("flowSection.oid", link.getToSection().getOid()),
				QueryCondition.not(QueryCondition.eq("next", "F")),
				QueryCondition.in("flowIndex", 
					new QueryParameter(
						"select max(flow_index) idx from of_flow_ins_section where flow_instance_oid=? and flow_section_oid=?", 
						new Object[] {ins.getOid(),link.getToSection().getOid()}
					)
				)
			})
		);
		Date d=new Date();
		if(list.size()>0) {
			for(FlowInstanceSection fs : list) {
				fs.setOid(GenerateID.generatePrimaryKey());
				fs.setFlowIndex(ins.getCurrentIndex());
				fs.setAcceptDate(d);
				fs.setDoneStatus("N");
				if(link.getToOption().lastIndexOf("U")==1) {
					fs.setOrg(null);
				}else if(link.getToOption().lastIndexOf("O")==1){
					fs.setUser(null);
				}
			}
		}else {
			FlowInstanceSection fs=new FlowInstanceSection();
			fs.setOid(GenerateID.generatePrimaryKey());
			fs.setFlowInstance(ins);
			fs.setFlowSection(link.getToSection());
			fs.setUser(ins.getBusiness().getApplyUser());
			fs.setFlowIndex(ins.getCurrentIndex());
			fs.setAcceptDate(d);
			fs.setDoneStatus("N");
			list.add(fs);
		}
		ModelUpdateUtil.insertModels(list.toArray(new FlowInstanceSection[] {}));
		return link.getToSection();
	}
	private void toNextSection(FlowInstance ins,FlowSectionLink link,List<AdminLogin> nextUsers,List<OrgGroupView> nextOrgs) throws MException {
		FlowInstanceSection fis=new FlowInstanceSection();
		fis.setFlowInstance(ins);
		fis.setFlowSection(link.getToSection());
		fis.setFlowIndex(ins.getCurrentIndex());
		fis.setAcceptDate(new Date());
		fis.setDoneStatus("N");
		insertInsSec4Process(fis, ins.getBusiness(), link.getToOption(), nextUsers, nextOrgs);
	}
	/**
	 * 获取未完成的流程实例
	 * @param flowId 流程标识
	 * @param businessOid 业务oid
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	private FlowInstance getInstance(String flowId,String businessOid) throws SQLException, MException {
		return ModelQueryList.getModel(FlowInstance.class, new String[] {"*","currentSection.*","currentSection.backSection.*"}, 
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowDefine.identity", flowId),
				QueryCondition.eq("business.oid", businessOid),
				QueryCondition.eq("doneStatus", "N")
			})
		);
	}
	/**
	 * 获取未完成的流程实例环节
	 * @param insOid 实例oid
	 * @param secOid 环节oid
	 * @param userOid 环节执行人
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	private FlowInstanceSection getInstanceSection(String insOid,String secOid,String userOid,String orgOid) throws SQLException, MException {
		return getInstanceSection(insOid, secOid, userOid, orgOid,"N");
	}
	/**
	 * 获取流程实例环节 
	 * @param insOid
	 * @param secOid
	 * @param userOid
	 * @param doneStatus
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	public FlowInstanceSection getInstanceSection(String insOid,String secOid,String userOid,String orgOid,String doneStatus) throws SQLException, MException {
		List<QueryCondition> cons=new ArrayList<QueryCondition>();
		cons.add(QueryCondition.eq("flowInstance.oid",insOid));
		cons.add(QueryCondition.eq("flowSection.oid",secOid));
		cons.add(QueryCondition.or(new QueryCondition[] {
			QueryCondition.eq("user.oid", userOid),
			QueryCondition.eq("org.oid", orgOid)
		}));
		if(!StringUtil.isSpace(doneStatus)) {
			cons.add(QueryCondition.eq("doneStatus", doneStatus));
		}
		return ModelQueryList.getModel(FlowInstanceSection.class, new String[] {"*"},
			QueryCondition.and(cons.toArray(new QueryCondition[] {})),
			QueryOrder.asc("doneStatus"));
	}
	/**
	 * 该实例环节是否都完成 
	 * @param insOid
	 * @param secOid
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	private boolean isOverInstanceSection(String insOid,String secOid) throws SQLException, MException {
		return ModelQueryList.getModelList(FlowInstanceSection.class, new String[] {"*"},null,
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowInstance.oid",insOid),
				QueryCondition.eq("flowSection.oid",secOid),
				QueryCondition.eq("doneStatus", "N")
			})
		).size()==0;
	}
	/**
	 * 设置该实例环节其他待办跳过
	 * @param insOid
	 * @param secOid
	 * @throws SQLException
	 * @throws MException
	 */
	private void setPassInstanceSection(String insOid,String secOid) throws SQLException, MException {
		List<FlowInstanceSection> list=ModelQueryList.getModelList(FlowInstanceSection.class, new String[] {"oid"},null,
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowInstance.oid",insOid),
				QueryCondition.eq("flowSection.oid",secOid),
				QueryCondition.eq("doneStatus", "N")
			})
		);
		for(FlowInstanceSection sec : list) {
			sec.setNext("P");
			sec.setParam("");
			sec.setToDesc("");
			sec.setDoneDate(new Date());
			sec.setDoneStatus("Y");
		}
		ModelUpdateUtil.updateModels(list.toArray(new FlowInstanceSection[] {}), new String[] {"next","param","doneDate","doneStatus"});	
	}
	/**
	 * 该实例环节结果 true表示走下一步
	 * @param insOid
	 * @param secOid
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	private boolean instanceSectionResult(String insOid,String secOid,Integer flowIndex) throws SQLException, MException {
		return ModelQueryList.getModelList(FlowInstanceSection.class, new String[] {"next","param"},null,
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowInstance.oid",insOid),
				QueryCondition.eq("flowSection.oid",secOid),
				QueryCondition.eq("flowIndex",flowIndex),
				QueryCondition.eq("doneStatus", "Y"),
				QueryCondition.not(QueryCondition.eq("next", "F")),//转发
				QueryCondition.not(QueryCondition.eq("next", "P"))//跳过
			}),true
		).size()==1;
	}
	/**
	 * 插入实例环节
	 * @param insSec 只剩user或org未设置
	 * @param business 
	 * @param option 下一步参数 AU申请人,AO申请部门,MU多选用户,MO多选部门,OU单选用户,OO单选部门
	 * @param userList
	 * @param orgList
	 * @throws MException
	 */
	private void insertInsSec4Process(FlowInstanceSection insSec,FlowBusiness business,String option,List<AdminLogin> userList,List<OrgGroupView> orgList) throws MException {
		if(StringUtil.noSpace(option).length()!=2) {
			throw new MException(this.getClass(), "选项参数错误");
		}else if(option.lastIndexOf("U")==1||option.lastIndexOf("O")==1) {
			if(option.indexOf("A")==0) {//发起
				insSec.setOid(GenerateID.generatePrimaryKey());
				if(option.lastIndexOf("U")==1) {
					insSec.setUser(business.getApplyUser());
				}else if(option.lastIndexOf("O")==1) {
					insSec.setOrg(business.getApplyOrg());
				}
				ModelUpdateUtil.insertModel(insSec);
				return;
			}else if(option.indexOf("M")==0) {//多选
				if(option.lastIndexOf("U")==1) {
					if(userList.size()==0) throw new MException(this.getClass(), "请选择处理人");
					for(AdminLogin user : userList) {
						insSec.setOid(GenerateID.generatePrimaryKey());
						insSec.setUser(user);
						ModelUpdateUtil.insertModel(insSec);
					}
					return;
				}else if(option.lastIndexOf("O")==1) {
					if(orgList.size()==0) throw new MException(this.getClass(), "请选择处理部门");
					for(OrgGroupView org : orgList) {
						insSec.setOid(GenerateID.generatePrimaryKey());
						insSec.setOrg(org);
						ModelUpdateUtil.insertModel(insSec);
					}
					return;
				}
			}else if(option.indexOf("O")==0) {//单选
				insSec.setOid(GenerateID.generatePrimaryKey());
				if(option.lastIndexOf("U")==1) {
					if(userList.size()!=1) throw new MException(this.getClass(), "请选择一个处理人");
					insSec.setUser(userList.get(0));
				}else if(option.lastIndexOf("O")==1) {
					if(orgList.size()!=1) throw new MException(this.getClass(), "请选择一个处理部门");
					insSec.setOrg(orgList.get(0));
				}
				ModelUpdateUtil.insertModel(insSec);
				return;
			}
		} 
		throw new MException(this.getClass(),"选项参数错误");
	}
}
