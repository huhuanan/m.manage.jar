package manage.util.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m.system.exception.MException;
import m.system.util.ObjectUtil;
import m.system.util.StringUtil;
import manage.util.page.viewui.AlertMeta;
import manage.util.page.viewui.ViewUIMeta;
import manage.util.page.viewui.ViewUIMeta.ViewUISite;

public class ViewUIMetaUtil {
	/**
	 * 转换成alert参数
	 * @param alert
	 * @return
	 * @throws MException
	 */
	public static Map<String,Object> toAlert(AlertMeta alert) throws MException{
		if(StringUtil.isSpace(alert.title())) return null;
		Map<String,Object> r=new HashMap<String, Object>();
		r.put("type", alert.type().toString());
		r.put("icon", alert.icon());
		r.put("title", alert.title());
		r.put("desc", alert.desc());
		return r;
	}
	/**
	 * 转换成ViewUI参数
	 * @param viewuis
	 * @return
	 */
	public static Map<String,String[]> toViewUI(ViewUIMeta[] viewuis){
		Map<String,String[]> map=new HashMap<String, String[]>();
		List<String> top=new ArrayList<String>();
		List<String> middle=new ArrayList<String>();
		List<String> bottom=new ArrayList<String>();
		List<String> buttonRight=new ArrayList<String>();
		for(ViewUIMeta vu : viewuis) {
			if(vu.site()==ViewUISite.TOP) {
				top.add(vu.template());
			}else if(vu.site()==ViewUISite.MIDDLE){
				middle.add(vu.template());
			}else if(vu.site()==ViewUISite.BOTTOM){
				bottom.add(vu.template());
			}else if(vu.site()==ViewUISite.BUTTON_RIGHT) {
				buttonRight.add(vu.template());
			}
		}
		map.put("top", top.toArray(new String[] {}));
		map.put("middle", middle.toArray(new String[] {}));
		map.put("bottom", bottom.toArray(new String[] {}));
		map.put("buttonRight", buttonRight.toArray(new String[] {}));
		return map;
	}
}
