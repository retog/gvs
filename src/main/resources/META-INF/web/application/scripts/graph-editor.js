/** The functionality related to graph-editing
*/
function GraphEditor(source) {
	var sources = new Array();
	sources[0] = source;
	this.location = GVS.getGVSurl(sources);
	this.source = source;
}

GraphEditor.prototype.save = function() {
	try {
		var xmlhttp= GVS.createXMLHttpRequest();
	  	xmlhttp.open("PUT", this.location, false);
	  	xmlhttp.setRequestHeader("User-Agent", "Jena - GVS");
	  	var editArea = document.getElementById('editarea');
	  	xmlhttp.send(editArea.value);
	} catch (e) {
		alert(e);
	}
	if (xmlhttp.status == 200) {
		this.initialValue = editArea.value;
		alert("Succesully saved");
	} else {
		alert(xmlhttp.status+" " +xmlhttp.statusText);
	}

}

GraphEditor.prototype.init = function() {
	document.getElementById('source').appendChild(document.createTextNode(this.source));
	var editArea = document.getElementById('editarea');
	GVS.present(editArea, 
		this.location, 
		'/application/stylesheets/xml-source');
	this.initialValue = editArea.value;
	var graphEditor = this;
	window.onbeforeunload = function () {
		if (editArea.value == graphEditor.initialValue) {
			return undefined;
		} else {
			return "you modified the graph serialization";
		}
	}
}

			