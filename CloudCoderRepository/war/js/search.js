/*
 * Javascript code for _view/search.jsp
 */

// Initiate an AJAX request to retrieve search results.
var onSubmit = function() {
	var problemTypeOrdinal = $("#selectedProblemType option:selected").attr('value');
	//alert("Problem type is " + problemType);
	
	var queryUri = "${pageContext.servletContext.contextPath}/search";
	$.ajax({
		url: queryUri,
		dataType: "json",
		type: "post",
		data: {
			problemType: problemTypeOrdinal 
		},
		success: function(data, textStatus, jqXHR) {
			// Result will be an array of JSON-encoded exercises
			alert("Search returned " + data.length + " exercises");
			
			// TODO: display the exercises by adding them to the DOM tree
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#errorElt").text(errorThrown);
		}
	});
};

$(document).ready(function() {
	$("#submitButton").click(onSubmit);
	
	// Enable DataTables on the search results table.
	$("#searchResultsTable").dataTable();
});
