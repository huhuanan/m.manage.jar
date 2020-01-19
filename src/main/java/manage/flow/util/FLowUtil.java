package manage.flow.util;

import m.system.RuntimeData;
import m.system.exception.MException;
import m.system.listener.InitListener;
import manage.flow.model.FlowBusiness;
import manage.flow.model.FlowSection;
import manage.flow.model.FlowUser;
import manage.flow.service.FlowBusinessService;
import manage.flow.service.FlowInstanceService;
import manage.flow.service.FlowUserService;
/**
 * 流程功能调用util
 * @author Administrator
 *
 */
public class FLowUtil {

	/**
	 * 转换成流程业务信息
	 * @param oid
	 * @param type
	 * @param title
	 * @return
	 * @throws Exception
	 */
	public static FlowBusiness toFlowBusiness(String oid,String type,String title) throws Exception {
		return RuntimeData.getService(FlowBusinessService.class).toFlowBusiness(oid,type,title);
	}
	/**
	 * 获取流程业务，找不到报错
	 * @param oid
	 * @return
	 * @throws Exception
	 */
	public static FlowBusiness toFlowBusiness(String oid) throws Exception {
		return RuntimeData.getService(FlowBusinessService.class).toFlowBusiness(oid);
	}
	/**
	 * 转换流程处理人
	 * @param oid
	 * @param realname
	 * @return
	 * @throws Exception
	 */
	public static FlowUser toUser(String oid,String realname) throws Exception {
		return RuntimeData.getService(FlowUserService.class).toUser(oid,realname);
	}
	/**
	 * 转换流程处理人
	 * @param oids
	 * @param realnames
	 * @return
	 * @throws MException
	 * @throws Exception
	 */
	public static FlowUser[] toUsers(String[] oids,String[] realnames) throws MException, Exception {
		return RuntimeData.getService(FlowUserService.class).toUsers(oids,realnames);
	}
	/**
	 * 开始流程
	 * @param flowId 流程标识
	 * @param business 流程业务
	 * @param user 开始流程人
	 * @throws Exception
	 */
	public static void startInstance(String flowId,FlowBusiness business,FlowUser user) throws Exception {
		RuntimeData.getService(FlowInstanceService.class).start(flowId, business, user);
	}
	/**
	 * 流程转发
	 * @param flowId 流程表示
	 * @param business 流程业务
	 * @param user 执行流程人
	 * @param nextUser 下一步执行人
	 * @throws Exception
	 */
	public static void forwardInstance(String flowId,FlowBusiness business,FlowUser user,FlowUser nextUser) throws Exception {
		RuntimeData.getService(FlowInstanceService.class).forward(flowId, business, user, nextUser);
	}
	/**
	 * 执行流程下一步
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @param nextUsers 下一步流程人
	 * @return 下一步流程环节，最后一个人通过后才返回，否则为null
	 * @throws Exception
	 */
	public static FlowSection nextInstance(String flowId,String param,FlowBusiness business,FlowUser user,FlowUser[] nextUsers) throws Exception {
		return RuntimeData.getService(FlowInstanceService.class).next(flowId, param, business, user, nextUsers);
	}
	/**
	 * 完成流程
	 * @param flowId 流程标识
	 * @param param 参数
	 * @param business 流程业务
	 * @param user 流程人
	 * @return 返回true代表最后一个人通过
	 * @throws Exception
	 */
	public static boolean finishInstance(String flowId,String param,FlowBusiness business,FlowUser user) throws Exception {
		return RuntimeData.getService(FlowInstanceService.class).finish(flowId, param, business, user);
	}
	public static void main(String[] a) throws Exception {
		InitListener.initDBConfig();
//		startInstance("flow-1", 
//			toFlowBusiness("1","1","111"),
//			toUser("1","11")
//		);
//		nextInstance("flow-1", "2", 
//			toFlowBusiness("1"),
//			toUser("1","11"),
//			toUsers(new String[] {"2"}, new String[] {"22"})
//		);
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
	}
}
