//scripts for the disco-edito page, not to be confused with actual discobit edito scripts in
//disco/widget-factory.js nor with the storage retrieval binding to gvs in gvs-disco.js

WidgetFactory.createURIderefURL = GVSDisco.createURIderefURL;
WidgetFactory.putData = GVSDisco.putData;
WidgetFactory.root = "scripts/disco/"

function DiscoEditor () {
}

DiscoEditor.getEditableSources = function() {
	var xmlhttp= GVS.createXMLHttpRequest();
  	xmlhttp.open("GET", "/meta/sources", false);
  	xmlhttp.setRequestHeader("User-Agent", "Jena - GVS");
  	xmlhttp.send(null);
  	if (xmlhttp.status != 200) {
  		alert("server returned failure: "+xmlhttp.responseText);
  	}
  	var result = new Object();
    result.xmlContent =  xmlhttp.responseXML;
    var store = new RDFIndexedFormula();
    var parser = new RDFParser(store);
	parser.parse(xmlhttp.responseXML.documentElement.cloneNode(true), "");
	//TODO look for actual user (currently assumes only current user in result)
	var containsStatements = store.statementsMatching(undefined, new RDFSymbol("http://gvs.hpl.hp.com/ontologies/authorization#mayImpersonate"), undefined);
	var result = new Array(containsStatements.length);
	for (var i = 0; i < containsStatements.length; i++) {
		result[i] = containsStatements[i].object.uri;
	}
	return result;
}

	
DiscoEditor.init = function() {
	var parameters = new Parameters();
	var uriParam = parameters.getField("uri");
	var uri;
	if (!uriParam) {
		uri = "http://gvs.hpl.hp.com/welcome";/*document.location.toString().substring(
				0, document.location.toString().lastIndexOf('/')+1)*/
	} else {
		uri = uriParam;
	}
	var discobitURIElem = document.getElementById('discobitURI');
	discobitURIElem.value = uri;
	discobitURIElem.disabled = false;
	var select = parameters.getField("select");
	var sourceSelect = document.getElementById('source');
	var editableSources = DiscoEditor.getEditableSources();
	if (editableSources.length == 0) {
		sourceSelect.style.display ="none";
		var warningNoPermission = document.getElementById('warningNoPermission');
		warningNoPermission.style.display ="";
	}
	var parameters = new Parameters();
	var source = parameters.getField("source");
	while (sourceSelect.firstChild) {
		sourceSelect.removeChild(sourceSelect.firstChild);
	}
	for (var i = 0; i < editableSources.length; i++) {
		var option =  document.createElementNS("http://www.w3.org/1999/xhtml", "option")
		sourceSelect.appendChild(option);
		option.appendChild(document.createTextNode(editableSources[i]));
		if (source == editableSources[i]) {
			option.selected = "true";
		}
	}
}
	
DiscoEditor.setURI = function() {
	var place = document.getElementById('place');
	while (place.firstChild) {
		place.removeChild(container.firstChild);
	}
	var discobitURIElem = document.getElementById('discobitURI');
	var sourceSelect = document.getElementById('source');
	if (sourceSelect.value != "") {
		GVSDisco.source = sourceSelect.value;
	}
	var body = document.getElementsByTagNameNS(xhtmlNS,"body")[0];
	var origCursor = body.style.cursor;
	body.style.cursor = 'progress';
	WidgetFactory.createBackground(
		function() {
			body.style.cursor = origCursor;
		},
		new RDFSymbol(discobitURIElem.value), 
		place);
	var loadButton = document.getElementById('loadButton');
	loadButton.disabled = true;
	sourceSelect.disabled = true;
	discobitURIElem.disabled = true;
	var discoLink = document.getElementById('discoLink');
	discoLink.href += encodeURIComponent(sourceSelect.value);
	discoLink.style.display = "";
}
	
	