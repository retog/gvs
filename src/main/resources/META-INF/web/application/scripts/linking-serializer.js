var xhtmlNS = "http://www.w3.org/1999/xhtml";

function LinkingSerializer() {
}

LinkingSerializer.serializeToXHTML = function(element) {
	var rootSpan = document.createElementNS(xhtmlNS, "div");
	//rootSpan.setAttributeNS(xhtmlNS, "class", "codeListing");
	rootSpan.className = "codeListing";
	LinkingSerializer.serializeToElement(element, rootSpan);
	return rootSpan;
}

LinkingSerializer.serializeToElement = function(serializingElement, rootSpan) {
	var namespaces = new Object();
	var namespacePane = document.createElementNS(xhtmlNS, "span");
	
	LinkingSerializer.serializeToElementNS(serializingElement, rootSpan, namespaces, true, namespacePane);
	var nsString = new String();
	for(var property in namespaces) {
	   var value = namespaces[property];
	   nsString += " xmlns";
	   if (property != "") {
	   	nsString += ":"+property;
	   }
	   nsString += "=\""+namespaces[property]+"\""
	}
	//alert("nsl: "+namespaces.length);
	namespacePane.appendChild(document.createTextNode(nsString));
	
}

LinkingSerializer.serializeToElementNS = function(serializingElement, targetElement, namespaces, inList, namespacePane) {
	
	//alert(serializingElement.nodeType);
	switch (serializingElement.nodeType) {
	case Node.TEXT_NODE:
		targetElement.appendChild(document.createTextNode(serializingElement.nodeValue));
		break;
	case Node.DOCUMENT_NODE:
		for (var i =0; i < serializingElement.childNodes.length; i++) {
			LinkingSerializer.serializeToElementNS(serializingElement.childNodes[i], targetElement, namespaces, true, namespacePane);
		}
		break;
	case Node.ELEMENT_NODE:
		var appendElem;
		if (inList) {
			var liElem = document.createElementNS(xhtmlNS, "div");
			targetElement.appendChild(liElem);
			appendElem = liElem;
		} else {
			appendElem = targetElement;
		}
		LinkingSerializer.handleNamespace(serializingElement.nodeName, serializingElement.namespaceURI, namespaces);
		appendElem.appendChild(document.createTextNode("<"+serializingElement.nodeName));
		if (serializingElement.attributes.length > 0) {
			appendElem.appendChild(LinkingSerializer.serializeAttributes(serializingElement, namespaces));
		}
		if (namespacePane) {
			appendElem.appendChild(namespacePane);
		}
		var openingTagPart2 = "";
		if (serializingElement.childNodes.length == 0) {
			openingTagPart2 += "/";
		}
		openingTagPart2 += ">";
		appendElem.appendChild(document.createTextNode(openingTagPart2));
		if (serializingElement.childNodes.length > 0) {
			if (LinkingSerializer.hasExactlyOneTextNodeChild(serializingElement)) {
				for (var i =0; i < serializingElement.childNodes.length; i++) {
					LinkingSerializer.serializeToElementNS(serializingElement.childNodes[i], appendElem, namespaces, false);
				}
			} else {
				var olElem = document.createElementNS(xhtmlNS, "div");
				olElem.className = "codeBlock";
				//olElem.setAttributeNS(xhtmlNS, "class", "codeBlock");
				appendElem.appendChild(olElem);
				for (var i =0; i < serializingElement.childNodes.length; i++) {
					LinkingSerializer.serializeToElementNS(serializingElement.childNodes[i], olElem, namespaces, true);
				}
			}
			var closingTag = "</"+serializingElement.nodeName+">";
			appendElem.appendChild(document.createTextNode(closingTag));
		}
	}
}

LinkingSerializer.handleNamespace = function(nodeName, namespaceURI, namespaces) {
	var colonPos = nodeName.indexOf(':');
	if (colonPos == -1) {
		prefix = "";
	} else {
		var prefix = nodeName.substring(0, colonPos);
	}
	namespaces[prefix] = namespaceURI;
}

LinkingSerializer.serializeAttributes = function(serializingElement, namespaces) {
	var result = document.createElementNS(xhtmlNS, "span");
	result.className = "sourceAttributes";
	for (var i = 0; i < serializingElement.attributes.length; i++) {
		if (serializingElement.attributes[i].nodeName.match(/^xmlns/)) {
			continue;
		}
		LinkingSerializer.handleNamespace(serializingElement.attributes[i].nodeName, serializingElement.attributes[i].namespaceURI, namespaces);
		result.appendChild(
		document.createTextNode(
		" "+serializingElement.attributes[i].name+"=\""));
		if ((serializingElement.attributes[i].name != "rdf:resource") &&
			(serializingElement.attributes[i].name != "rdf:about")) {
			result.appendChild(
			document.createTextNode(serializingElement.attributes[i].value+"\""));
		} else {
			var link = document.createElementNS(xhtmlNS, "a");
			result.appendChild(link);
			var uri = serializingElement.attributes[i].value;
			link.href = uri;
			link.onclick = function() {
				gvsBrowser.selectResource(uri);
				
				return false;
			}
			link.appendChild(
			document.createTextNode(uri));
			var addLink = document.createElementNS(xhtmlNS, "a");
			result.appendChild(addLink);
			addLink.href = uri;
			addLink.onclick = function() {
				gvsBrowser.addSelectedResource(uri);
				return false;
			}
			var addImg = document.createElementNS(xhtmlNS, "img");
			addLink.appendChild(addImg);
			addImg.src = "/application/images/arrow-right";
			//addImg.alt = "add resource neighbourhood";
			//<img src="/application/images/arrow-right" alt="go to contained"/></a>
			result.appendChild(document.createTextNode("\""));
		}
	}
	return result;
}

LinkingSerializer.hasExactlyOneTextNodeChild = function(element) {
	if (element.childNodes.length > 1) {
		return false;
	}
	return (element.childNodes[0].nodeType == Node.TEXT_NODE);
}