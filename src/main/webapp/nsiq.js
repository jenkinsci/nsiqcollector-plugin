function showSourceCode(url) {
	var source = document.getElementById("sourceholder");
	if (source.source != "on") {
		new Ajax.Request(
			url,
			{
				onComplete : function(rsp) {
					var responseText = rsp.responseText;
					if (rsp.status == 200) {
						source.source = "on";
						source.innerHTML = responseText;
					} else {
						source.source = "on";
						source.innerHTML = "Internal Error<br>" + responseText;
					}
				}
			}
		);
	} else {
		source.source = "off";
		source.innerHTML = "";
	}
}