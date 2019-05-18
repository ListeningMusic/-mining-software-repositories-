<%--
  Created by IntelliJ IDEA.
  User: 123
  Date: 2019/3/17
  Time: 14:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page  isELIgnored = "false" contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>识别成功</title>
</head>
<body>
      识别识曲为：${sessionScope.name}<br/>
      <%--原生api sessoion：${sessionScope.semsg}<br/>--%>
      <%--request: ${requestScope.msg}<br/>--%>
      <%--session: ${sessionScope.msg}<br/>--%>
      <%--application: ${applicationScope.msg}<br/>--%>
</body>
</html>
