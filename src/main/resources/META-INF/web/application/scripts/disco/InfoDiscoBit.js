function InfoDiscoBitWidget(store, rdfSymbol, xhtmlContainer, controller, terminationListener) {
	
	this.rdfSymbol = rdfSymbol;
	this.controller = controller;
	this.xhtmlContainer = xhtmlContainer;
	var objectElement = document.createElementNS(xhtmlNS, "object");
	objectElement.type = "application/xhtml+xml";
	objectElement.style.width = "300px";
	objectElement.style.height = "100px";
	//objectElement.contentDocument = document.implementation.createDocument("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF", null);
	this.xhtmlContainer.appendChild(objectElement);
	this.xhtmlContainer.appendChild(document.createTextNode("FOO"));
	
	var afterObjectIsCreated = function() {
		var innerDoc = objectElement.contentDocument;
		if (innerDoc) {


			//alert(innerDoc.documentElement);
			var innerBody = innerDoc.createElementNS(xhtmlNS,"body");
			var innerHTML = innerDoc.createElementNS(xhtmlNS,"html");
			//innerDoc.documentElement = innerHTML;
			innerDoc.documentElement.appendChild(innerBody);
			innerBody.appendChild(innerDoc.createTextNode("BAR"));
			var form = innerDoc.createElementNS(xhtmlNS,"form");
			innerBody.appendChild(form);
			var fileInput = innerDoc.createElementNS(xhtmlNS,"input");
			fileInput.type = "file";
			fileInput.name = "file";
			form.appendChild(fileInput);
			
			var textInput = innerDoc.createElementNS(xhtmlNS,"input");
			textInput.type = "text";
			textInput.name = "text";
			form.appendChild(textInput);
			
			var locationInput = innerDoc.createElementNS(xhtmlNS,"input");
			locationInput.type = "hidden";
			locationInput.name = "location";
			locationInput.value = rdfSymbol.uri;
			form.appendChild(locationInput);
			
			form.method = "POST";
			form.enctype = "multipart/form-data";
			
			form.action = Util.uri.join("/put-infobit", window.location.toString());
			var submit = innerDoc.createElementNS(xhtmlNS,"input");
			submit.type = "submit";
			submit.value = "Upload";
			submit.name = "submit";
			submit.onclick = function() {

				return confirm("go ahead?");
			}
			form.appendChild(submit);
			
			
			
			terminationListener();
		} else {
			setTimeout(afterObjectIsCreated, 0);
		}
	};
	setTimeout(afterObjectIsCreated, 0);
	//WidgetFactory.appendChildrenInDiv(objectElement, this.xhtmlContainer);
}

WidgetFactory.typeWidgets.push(InfoDiscoBitWidget);

InfoDiscoBitWidget.type = new RDFSymbol("http://discobits.org/ontology#InfoDiscoBit");


InfoDiscoBitWidget.description = "*EXPERIMENTAL* InfoDiscoBit upload widget";

InfoDiscoBitWidget.prototype.getWidgetControls = function() {
	var controlFunctions = new Array();
  	return controlFunctions;
}   

InfoDiscoBitWidget.prototype.getStore = function() {
	var store = new RDFIndexedFormula();
	store.add(this.rdfSymbol, new RDFSymbol("http://discobits.org/ontology#infoBit"), new RDFLiteral(this.editableArea));
	store.add(this.rdfSymbol, 
		new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
		new RDFSymbol("http://discobits.org/ontology#InfoDiscoBit"));
	return store;
}