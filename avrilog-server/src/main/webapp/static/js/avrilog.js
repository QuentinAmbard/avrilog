ALOG = {page: {}};

ALOG.page.Index = function () {
	$('#addNewApp').click(function(){
		$('#mainContent').load('/application/addApplication');
	});
};

ALOG.page.Index.prototype = {
	
};