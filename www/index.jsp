<%
	if (session.getAttribute("id") == null) {
		
		response.sendRedirect("/sign-in/");
		
	} else {
				
		response.sendRedirect("/heroes/");
		
	}
%>