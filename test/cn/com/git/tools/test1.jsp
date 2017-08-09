<%
// jsp对应的静态页面
// ddd
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/page/platform/common/common.jsp"%>
<!DOCTYPE html>
<html>
<title>系统管理</title>
<meta charset="UTF-8">
<div class="page-content">

<form method="post" class="form" id="test1">
<input name="aaa">
</form>
dddd1
<fieldset><div>dd</div></fieldset>
<!-- 工具栏 -->
<!--  <div class="form"></div> -->
<!--  <div class="form"></div> 
-->
 
<div class="form"></div> 
<script type="text/javascript">
function selectMapFun() {
	var dialog = parent.sy.modalDialog();
}
</script>
</div>
<script type="text/javascript" scr="${contextPath}/page/collmanage/colldetailsinfo/detailsInfoCommon.js?version=${version}"></script>
<script type="text/javascript" scr="${contextPath}/page/collmanage/colldetailsinfo/colBankAcceptanceForm.js?version=${version}"></script>
</html>