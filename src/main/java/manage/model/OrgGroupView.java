package manage.model;

import m.common.model.TableMeta;

@TableMeta(name="v_org_group",description="机构视图",isView = true,
	viewSql = "select * from os_admin_group where type='C'")
public class OrgGroupView extends AdminGroup {

}
