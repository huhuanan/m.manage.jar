package manage.flow.util;

import java.sql.SQLException;

import m.common.model.util.ModelQueryList;
import m.common.model.util.QueryCondition;
import m.system.exception.MException;
import m.system.util.ClassUtil;
import m.system.util.StringUtil;
import manage.flow.model.FlowInstance;
import manage.flow.service.IFlowService;
/**
 * 流程功能调用util
 * @author Administrator
 *
 */
public class FlowUtil {

	/**
	 * 反射业务服务类
	 * @param busiService
	 * @return
	 * @throws MException
	 */
	public static IFlowService getService(String busiService) throws MException {
		if(StringUtil.isSpace(busiService)) throw new MException(FlowUtil.class, "业务服务类不能为空");
		return ((IFlowService)ClassUtil.newInstance(busiService));
	}
	/**
	 * 获取流程标识
	 * @param busiService
	 * @return
	 * @throws MException
	 */
	public static String getFlowId(String busiService) throws MException {
		return getService(busiService).getFlowIdentity();
	}
	/**
	 * 获取业务的流程实例
	 * @param busiService
	 * @param busiOid
	 * @return
	 * @throws MException
	 * @throws SQLException
	 */
	public static FlowInstance getInstance4Business(String busiService,String busiOid) throws MException, SQLException {
		String flowId=getFlowId(busiService);
		return ModelQueryList.getModel(FlowInstance.class, 
			new String[] {"*","currentSection.*","flowDefine.*","flowDefine.startSection.*"}, 
			QueryCondition.and(new QueryCondition[] {
				QueryCondition.eq("flowDefine.identity", flowId),
				QueryCondition.eq("business.oid", busiOid)
			})
		);
		
	}
//	/**
//	 * 转换成流程业务信息
//	 * @param oid
//	 * @param type
//	 * @param title
//	 * @param service 业务对应的服务类
//	 * @return
//	 * @throws Exception
//	 */
//	public static FlowBusiness toFlowBusiness(String oid,String type,String title,String service) throws Exception {
//		return RuntimeData.getService(FlowBusinessService.class).toFlowBusiness(oid,type,title,service);
//	}
//	/**
//	 * 获取流程业务，找不到报错
//	 * @param oid
//	 * @return
//	 * @throws Exception
//	 */
//	public static FlowBusiness toFlowBusiness(String oid) throws Exception {
//		return RuntimeData.getService(FlowBusinessService.class).toFlowBusiness(oid);
//	}
//	/**
//	 * 转换流程处理人
//	 * @param oid
//	 * @param realname
//	 * @return
//	 * @throws Exception
//	 */
//	public static FlowUser toUser(String oid,String realname) throws Exception {
//		return RuntimeData.getService(FlowUserService.class).toUser(oid,realname);
//	}
//	/**
//	 * 转换流程处理人
//	 * @param oids
//	 * @param realnames
//	 * @return
//	 * @throws MException
//	 * @throws Exception
//	 */
//	public static FlowUser[] toUsers(String[] oids,String[] realnames) throws MException, Exception {
//		return RuntimeData.getService(FlowUserService.class).toUsers(oids,realnames);
//	}
//	/**
//	 * 开始流程
//	 * @param flowId 流程标识
//	 * @param business 流程业务
//	 * @param user 开始流程人
//	 * @throws Exception
//	 */
//	public static void startInstance(String flowId,FlowBusiness business,FlowUser user) throws Exception {
//		RuntimeData.getService(FlowInstanceService.class).start(flowId, business, user);
//	}
//	/**
//	 * 流程转发
//	 * @param flowId 流程表示
//	 * @param business 流程业务
//	 * @param user 执行流程人
//	 * @param nextUser 下一步执行人
//	 * @throws Exception
//	 */
//	public static void forwardInstance(String flowId,FlowBusiness business,FlowUser user,FlowUser nextUser) throws Exception {
//		RuntimeData.getService(FlowInstanceService.class).forward(flowId, business, user, nextUser);
//	}
//	/**
//	 * 执行流程下一步
//	 * @param flowId 流程标识
//	 * @param param 参数
//	 * @param business 流程业务
//	 * @param user 流程人
//	 * @param nextUsers 下一步流程人
//	 * @return 下一步流程环节，最后一个人通过后才返回，否则为null
//	 * @throws Exception
//	 */
//	public static FlowSection nextInstance(String flowId,String param,FlowBusiness business,FlowUser user,FlowUser... nextUsers) throws Exception {
//		return RuntimeData.getService(FlowInstanceService.class).next(flowId, param, business, user, nextUsers);
//	}
//	/**
//	 * 执行流程退回
//	 * @param flowId 流程标识
//	 * @param param 参数
//	 * @param business 流程业务
//	 * @param user 流程人
//	 * @return 下一步流程环节，通过后才返回
//	 * @throws MException 
//	 * @throws Exception 
//	 */
//	public static FlowSection backInstance(String flowId,FlowBusiness business,FlowUser user) throws Exception {
//		return RuntimeData.getService(FlowInstanceService.class).back(flowId, business, user);
//	}
//	/**
//	 * 完成流程
//	 * @param flowId 流程标识
//	 * @param param 参数
//	 * @param business 流程业务
//	 * @param user 流程人
//	 * @return 返回true代表最后一个人通过
//	 * @throws Exception
//	 */
//	public static boolean finishInstance(String flowId,String param,FlowBusiness business,FlowUser user) throws Exception {
//		return RuntimeData.getService(FlowInstanceService.class).finish(flowId, param, business, user);
//	}
//	public static void main(String[] a) throws Exception {
//		InitListener.initDBConfig();
//		startInstance("flow1", 
//			toFlowBusiness("7","t7","业务7"),
//			toUser("1","管理员")
//		);
//		nextInstance("flow1", "2", 
//			toFlowBusiness("1"),
//			toUser("zhangsan","张三"),
//			toUsers(new String[] {"lisi","wangwu"}, new String[] {"李四","王五"})
//		);
//		nextInstance("flow1", "", 
//			toFlowBusiness("1"), 
//			toUser("lisi", "李四"), 
//			toUser("wangwu","王五")
//		);
//		backInstance("flow1", toFlowBusiness("1"), toUser("wangwu", ""));
////		forwardInstance("flow-1", 
////			toFlowBusiness("1"),
////			toUser("2","22"),
////			toUser("3","33")
////		);
//		nextInstance("flow-1", "3", 
//			toFlowBusiness("1"),
//			toUser("2","11"),
//			toUsers(new String[] {"3"}, new String[] {"33"})
//		);
//		finishInstance("flow-1", "Y", 
//			toFlowBusiness("1"),
//			toUser("3","11")
//		);
//		nextInstance("flow-1", "1", 
//			toFlowBusiness("1"),
//			toUser("3","11"),
//			toUsers(new String[] {"2","3"}, new String[] {"22","33"})
//		);
//	}
}
