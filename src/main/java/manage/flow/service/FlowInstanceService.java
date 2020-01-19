package manage.flow.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import m.common.model.util.ModelQueryList;
import m.common.model.util.ModelUpdateUtil;
import m.common.model.util.QueryCondition;
import m.common.model.util.QueryParameter;
import m.common.service.Service;
import m.system.db.TransactionManager;
import m.system.exception.MException;
import m.system.util.GenerateID;
import m.system.util.StringUtil;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowInstance;
import manage.flow.model.FlowInstanceSection;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowUser;

public class FlowInstanceService extends Service {
	/**
	 * 开始流程
	 * @param flowId 流程标识
	 * @param business 流程业务
	 * @param user 开始流程人
	 * @throws Exception
	 */
	public void start(String flowId,FlowBusiness business,FlowUser user) throws Exception {
		verifyBusiness(business);
		verifyUser(user);
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
			FlowInstanceSection sec=new FlowInstanceSection();
			sec.setOid(GenerateID.generatePrimaryKey());
			sec.setFlowInstance(ins);
			sec.setFlowSection(ins.getCurrentSection());
			sec.setFlowIndex(ins.getCurrentIndex());
			sec.setUser(user);
			sec.setAcceptDate(new Date());
			sec.setDoneStatus("N");
			ModelUpdateUtil.insertModel(sec);
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
	public void forward(String flowId,FlowBusiness business,FlowUser user,FlowUser nextUser) throws Exception {
		verifyBusiness(business);
		verifyUser(user);
		verifyUser(nextUser);
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(business.getOid());
			FlowInstance ins=getInstance(flowId,business.getOid());
			if(null==ins) throw new MException(this.getClass(), "该流程实例不存在");
			if(!StringUtil.noSpace(ins.getCurrentSection().getForwardable()).equals("Y")) {
				throw new MException(this.getClass(), "该环节不支持转发");
			}
			FlowInstanceSection sec=getInstanceSection(ins.getOid(), ins.getCurrentSection().getOid(), user.getOid());
			if(null==sec) throw new MException(this.getClass(), "实例环节不存在");
			sec.setNext("F");
			sec.setDoneDate(new Date());
			sec.setDoneStatus("Y");
			ModelUpdateUtil.updateModel(sec, new String[] {"next","doneDate","doneStatus"});
			sec.setOid(GenerateID.generatePrimaryKey());
			sec.setUser(nextUser);
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
	public FlowSection next(String flowId,String param,FlowBusiness business,FlowUser user,FlowUser[] nextUsers) throws Exception {
		verifyBusiness(business);
		verifyUser(user);
		verifyUsers(nextUsers);
		return toNext(flowId,"Y",param,business,user,nextUsers);
	}
	/**
	 * 完成流程
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @throws Exception
	 */
	public boolean finish(String flowId,String param,FlowBusiness business,FlowUser user) throws Exception {
		verifyBusiness(business);
		verifyUser(user);
		return null!=toNext(flowId,"N",param,business,user,null);
	}
	private FlowSection toNext(String flowId,String isNext,String param,FlowBusiness business,FlowUser user,FlowUser[] nextUsers) throws Exception {
		FlowSection result=null;
		TransactionManager tm=new TransactionManager();
		try {
			tm.begin(business.getOid());
			FlowInstance ins=getInstance(flowId,business.getOid());
			if(null==ins) throw new MException(this.getClass(), "该流程实例不存在");
			FlowInstanceSection sec=getInstanceSection(ins.getOid(), ins.getCurrentSection().getOid(), user.getOid());
			if(null==sec) throw new MException(this.getClass(), "实例环节不存在");
			sec.setNext("Y");
			sec.setParam(param);
			sec.setDoneDate(new Date());
			sec.setDoneStatus("Y");
			ModelUpdateUtil.updateModel(sec, new String[] {"next","param","doneDate","doneStatus"});
			if(isOverInstanceSection(ins.getOid(), ins.getCurrentSection().getOid())) {
				if(instanceSectionResult(ins.getOid(), ins.getCurrentSection().getOid())) {
					//按照当前环节走下一步
					FlowSection fs=getService(FlowSectionService.class).getNext(ins.getCurrentSection().getFlowDefine().getOid(),ins.getCurrentSection().getIdentity(), isNext, param);
					if(null!=fs) {//下一步
						if((!StringUtil.noSpace(fs.getCountersign()).equals("Y"))&&nextUsers.length>1) {
							throw new MException(this.getClass(), "下一环节不支持会签");
						}
						ins.setCurrentSection(fs);
						ins.setCurrentIndex(ins.getCurrentIndex()+1);
						ModelUpdateUtil.updateModel(ins, new String[] {"currentSection.oid","currentIndex"});
						toNextSection(ins, nextUsers);
						result=ins.getCurrentSection();
					}else {//结束
						ins.setDoneDate(new Date());
						ins.setDoneStatus("Y");
						ModelUpdateUtil.updateModel(ins, new String[] {"doneDate","doneStatus"});
						result=new FlowSection();
					}
				}else {
					//走当前环节的退回
					ins.setCurrentSection(ins.getCurrentSection().getBackSection());
					ins.setCurrentIndex(ins.getCurrentIndex()+1);
					ModelUpdateUtil.updateModel(ins, new String[] {"currentSection","currentIndex"});
					toBackSection(ins);
					result=ins.getCurrentSection();
				}
			}
			tm.commit();
		}catch(Exception e) {
			tm.rollback();
			throw e;
		}
		return result;
	}
	private void toBackSection(FlowInstance ins) throws SQLException, MException {
		List<FlowInstanceSection> list=ModelQueryList.getModelList(FlowInstanceSection.class,
			new String[] {"flowInstance.oid","flowSection.oid","user.oid"},null,
			QueryCondition.and(new QueryCondition[]{
				QueryCondition.eq("flowInstance.oid", ins.getOid()),
				QueryCondition.eq("flowSection.oid", ins.getCurrentSection().getOid()),
				QueryCondition.not(QueryCondition.eq("next", "F")),
				QueryCondition.in("flowIndex", 
					new QueryParameter(
						"select max(flow_index) idx from of_flow_ins_section where flow_instance_oid=? and flow_section_oid=?", 
						new Object[] {ins.getOid(),ins.getCurrentSection().getOid()}
					)
				)
			})
		);
		Date d=new Date();
		for(FlowInstanceSection fs : list) {
			fs.setOid(GenerateID.generatePrimaryKey());
			fs.setFlowIndex(ins.getCurrentIndex());
			fs.setAcceptDate(d);
			fs.setDoneStatus("N");
		}
		ModelUpdateUtil.insertModels(list.toArray(new FlowInstanceSection[] {}));
	}
	private void toNextSection(FlowInstance ins,FlowUser[] nextUsers) throws MException {
		List<FlowInstanceSection> list=new ArrayList<FlowInstanceSection>();
		Date d=new Date();
		for(FlowUser user : nextUsers) {
			FlowInstanceSection fis=new FlowInstanceSection();
			fis.setOid(GenerateID.generatePrimaryKey());
			fis.setFlowInstance(ins);
			fis.setFlowSection(ins.getCurrentSection());
			fis.setFlowIndex(ins.getCurrentIndex());
			fis.setUser(user);
			fis.setAcceptDate(d);
			fis.setDoneStatus("N");
			list.add(fis);
		}
		ModelUpdateUtil.insertModels(list.toArray(new FlowInstanceSection[] {}));
	}
	private void verifyBusiness(FlowBusiness business) throws MException {
		if(null==business||StringUtil.isSpace(business.getOid())) {
			throw new MException(this.getClass(), "流程业务信息错误");
		}
	}
	private void verifyUser(FlowUser user) throws MException {
		if(null==user||StringUtil.isSpace(user.getOid())) {
			throw new MException(this.getClass(), "流程人信息错误");
		}
	}
	private void verifyUsers(FlowUser[] users) throws MException {
		if(null==users||users.length<1) {
			throw new MException(this.getClass(), "下一步流程人信息错误");
		}
		for(FlowUser user : users) {
			verifyUser(user);
		}
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
	private FlowInstanceSection getInstanceSection(String insOid,String secOid,String userOid) throws SQLException, MException {
		return ModelQueryList.getModel(FlowInstanceSection.class, new String[] {"*"},
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowInstance.oid",insOid),
				QueryCondition.eq("flowSection.oid",secOid),
				QueryCondition.eq("doneStatus", "N"),
				QueryCondition.eq("user.oid", userOid)
			})
		);
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
	 * 该实例环节结果 true表示走下一步
	 * @param insOid
	 * @param secOid
	 * @return
	 * @throws SQLException
	 * @throws MException
	 */
	private boolean instanceSectionResult(String insOid,String secOid) throws SQLException, MException {
		return ModelQueryList.getModelList(FlowInstanceSection.class, new String[] {"next","param"},null,
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowInstance.oid",insOid),
				QueryCondition.eq("flowSection.oid",secOid),
				QueryCondition.eq("doneStatus", "Y"),
				QueryCondition.not(QueryCondition.eq("next", "F"))
			}),true
		).size()==1;
	}
}
