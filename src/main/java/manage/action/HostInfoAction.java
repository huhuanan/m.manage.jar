package manage.action;

import m.common.action.ActionMeta;
import m.common.action.ActionResult;
import m.common.service.HostInfoService;
import m.system.RuntimeData;
import m.system.cache.CacheHost;
import m.system.cache.CacheMap;
import m.system.cache.CacheMap2;
import m.system.cache.CacheMapList;
import m.system.exception.MException;
import m.system.util.JSONMessage;

@ActionMeta(name="manageHostInfo")
public class HostInfoAction extends ManageAction {
	
	public ActionResult toList() throws MException{
		ActionResult result=new ActionResult("manage/hostInfo/hostInfoList");
		result.setList(getService(HostInfoService.class).getList());
		return result;
	}
	
	public JSONMessage getCacheList() {
		setLogContent("缓存", "查询缓存列表");
		JSONMessage message=new JSONMessage();
		try {
			verifyAdminOperPower("manage_system_power");
			message.push("code", 0);
			message.push("model", HostInfoService.getCurrentHost());
			message.push("cacheHost", CacheHost.getAll());
			message.push("cacheMap", CacheMap.getAll());
			message.push("cacheMap2", CacheMap2.getAll());
			message.push("cacheList", CacheMapList.getAll());
		} catch (Exception e) {
			message.push("code", 1);
			message.push("msg", e.getMessage());
			setLogError(e.getMessage());
			if(RuntimeData.getDebug()) e.printStackTrace();
		}
		return message;
	}
	
	@Override
	public Class<? extends ManageAction> getActionClass() {
		return this.getClass();
	}

}
