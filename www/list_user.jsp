<%@ page contentType="application/json; charset=utf-8"  %>
<%@ page import="java.util.*" %>
<jsp:useBean id="webBean" scope="session" class="winter.Web" />
<%= webBean.listUser(request, session) %>