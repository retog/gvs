function GVSBrowser() {
	this.suspendRefresh = false;
	this.sourceURIs = new Array();
	this.availableSourceURIs = new Array();
	this.editableSourceURIs = new Array();
	this.resourceURIs = new Array();
	this.moment = new Date();
	this.template = "combined";
	this.previousDates = new Array();
	this.followingDates = new Array();
	this.resourceRowCount = 1;
}

GVSBrowser.prototype.init = function() {
	this.removeResourceRowLink = document.getElementById("removeResourceRowLink");
	this.removeResourceRowLink.style.display ="none";
	this.autoRefreshCheckbox = document.getElementById("autoRefresh");
	GVS.present(document.getElementById("sourceSeletion"), 
	"/meta/sources","/application/stylesheets/source-selection");
	document.getElementById("dateField").value = this.moment.toISO8601String();
	if (GVSBrowser.readCookie("login")) {
		var loginArea = document.getElementById("login");
		loginArea.style.display ="none";
	} else {
		var logoutArea = document.getElementById("logout");
		logoutArea.style.display = "none";
	}
	this.autoRefreshCheckbox.checked = false;
	if (this.selectEditableSources() == 0) {
		var checkboxes = getChildrenByClass(document,"welcomeCB");
		for (var i = 0; i < checkboxes.length; i++) {
			checkboxes[i].checked = true;
			checkboxes[i].onclick();
		}
	}
	//TODO prevent double-loading
	this.refresh(); //just to load model to select start res from
	this.selectStartResources();
	this.autoRefreshCheckbox.checked = true;
	this.sourceDefinitionChanged();
	this.initClosable();
	//this.initOverview();
	if (document.getElementById("account").offsetHeight > document.getElementById("head").offsetHeight) {
		document.getElementById("head").style.height=document.getElementById("account").offsetHeight+"px";
	}
}
GVSBrowser.prototype.initClosable = function() {
	var elements = getChildrenByClass(document,"closeable");
	for (var i = 0; i < elements.length; i++) {
		var img = document.createElementNS("http://www.w3.org/1999/xhtml","img");
		img.src = "/application/images/close";
		var a = document.createElementNS("http://www.w3.org/1999/xhtml","a");
		a.href = "#";
		var element = elements[i];
		a.i = i;
		a.onclick = function() {
			elements[this.i].style.display = "none";
		};		
		a.appendChild(img);
		var closeDiv = document.createElementNS("http://www.w3.org/1999/xhtml","div");
		closeDiv.style.float = "right";
		closeDiv.className = "closeButton";
		closeDiv.appendChild(a);
		elements[i].insertBefore(closeDiv, elements[i].childNodes[0]);
		//alert("precessed: " + element.id);
		elements[i].style.display = "none";
	}
}

GVSBrowser.prototype.initOverview = function() {

	var selectedTriplesCountField =  document.getElementById("selectedTriplesCount");
	selectedTriplesCountField.removeChild(selectedTriplesCountField.childNodes[0]);
	selectedTriplesCountField.appendChild(document.createTextNode(this.currentVersion.rdfContent.statements.length));


	var selectedDateField =  document.getElementById("selectedDate");
	selectedDateField.removeChild(selectedDateField.childNodes[0]);
	var dateField = document.getElementById("dateField");
	var dateText = this.moment;//dateField.value;
	selectedDateField.appendChild(document.createTextNode(dateText));
	
	var selectedTemplateField =  document.getElementById("selectedTemplate");
	selectedTemplateField.removeChild(selectedTemplateField.childNodes[0]);
	selectedTemplateField.appendChild(document.createTextNode(this.template));
	
	var selectedResourcesTextField =  document.getElementById("selectedResourcesText");
	selectedResourcesTextField.removeChild(selectedResourcesTextField.childNodes[0]);
	var resourcesText;
	if (!document.getElementById("resourceSelected").checked) {
		resourcesText = "all resources";
	} else {
		if (this.resourceURIs.length == 1) {
			resourcesText = "the resource \""+this.resourceURIs[0]+"\"";
		} else {
			resourcesText = this.resourceURIs.length+" resources";
		}
	}
	selectedResourcesTextField.appendChild(document.createTextNode(resourcesText));
	
	
	var selectedSourcesTextField =  document.getElementById("selectedSourcesText");
	selectedSourcesTextField.removeChild(selectedSourcesTextField.childNodes[0]);
	var sourcesText;
	if (this.sourceURIs.length == 1) {
		sourcesText = "the source \""+this.sourceURIs[0]+"\"";
	} else {
		sourcesText = this.sourceURIs.length+" sources";
	}
	selectedSourcesTextField.appendChild(document.createTextNode(sourcesText));
	
}

GVSBrowser.prototype.initActions = function() {	
	var actionVisible = false;
	var seeAlsoCountText =  document.getElementById("seeAlsoCountText");
	
	var seeAlsoText;
	var seeAlsos = this.getSeeAlsoSourceURIs();
	var seeAlsoAction = document.getElementById("seeAlsoAction");
	if (seeAlsos.length == 0) {
		seeAlsoAction.style.display = "none";
	} else {
		seeAlsoCountText.removeChild(seeAlsoCountText.childNodes[0]);
		seeAlsoAction.style.display = "";
		actionVisible = true;
		if (seeAlsos.length == 1) {
			seeAlsoText = "source "+seeAlsos[0];
		} else {
			seeAlsoText = seeAlsos.length+" source";
		}
		seeAlsoCountText.appendChild(document.createTextNode(seeAlsoText));
		var gvsBrowser = this;
		var seeAlsoLink = document.getElementById("seeAlsoLink");
		seeAlsoLink.onclick = function() {
			gvsBrowser.selectSources(seeAlsos);
			return false;
		}
	}
	
	
	var editAction = document.getElementById("editAction");
	var exitingEditActions = getChildrenByClass(editAction.parentNode, "editActionInstance");
	for (var i = 0; i < exitingEditActions.length; i++) {
		editAction.parentNode.removeChild(exitingEditActions[i]);
	}
	editAction.style.display = "none";
	for (var i = 0; i < this.editableSourceURIs.length; i++) {
		actionVisible = true;
		var currentAction = editAction.cloneNode(true);
		currentAction.style.display = "";
		currentAction.id = undefined;
		currentAction.className = "editActionInstance";
		var currentEditableSourceURI = this.editableSourceURIs[i];
		var editableSourceNameField = getChildrenByClass(currentAction, "editableSourceName")[0];
		editableSourceNameField.appendChild(document.createTextNode("<"+currentEditableSourceURI+">"));
		editAction.parentNode.appendChild(currentAction);
		var link = currentAction.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "a")[0];
		link.href = "edit-graph?source="+encodeURIComponent(currentEditableSourceURI);
		/*link.onclick = function() {
			alert("do it!");
			return false;
		}*/
	}
	var actionsSection = document.getElementById("actionsSection");
	if (actionVisible) {
		actionsSection.style.display = "";
	} else {
		actionsSection.style.display = "none";
	}
}

function RDF(localName) {
	return new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#'+localName);
}

function RDFS(localName) {
	return new RDFSymbol('http://www.w3.org/2000/01/rdf-schema#'+localName);
}

function GVSBROWSER(localName) {
	return new RDFSymbol('http://gvs.hpl.hp.com/ontologies/gvs-browser#'+localName);
}

GVSBrowser.prototype.selectStartResources = function() {
	var startResourceStatements = this.currentVersion.rdfContent.statementsMatching(undefined, RDF('type'), GVSBROWSER('StartResource'));
	for(var i=0;i<startResourceStatements.length;i++) {
        var startResource = startResourceStatements[i].subject;
        if (i == 0) {
        	this.selectResource(startResource.uri);
        } else {
        	this.addSelectedResource(startResource.uri);
       	}
    }
}

GVSBrowser.prototype.getSeeAlsoSourceURIs = function() {
	return this.getSeelAlsoSourceURIsFromGraph(this.currentVersion.rdfContent);
}

/** returns the not yet selected available sources references with rdfs.seeAlso in graph
*/
GVSBrowser.prototype.getSeelAlsoSourceURIsFromGraph = function(graph) {
	var result = new Array();
	var recommendedSourceStatements = graph.statementsMatching(undefined, RDFS('seeAlso'), undefined);
	for(var i=0;i<recommendedSourceStatements.length;i++) {
        var recommendedSource = recommendedSourceStatements[i].object;
        if (!this.sourceURIs.contains(recommendedSource.uri)) {
        	if (this.availableSourceURIs.contains(recommendedSource.uri)) {
        		var comparer = function(first, other) {
        				return first.uri == other.uri;
        		};
        		if (!result.contains(recommendedSource.uri)) {
  			      	//alert(recommendedSource);
        			result[result.length] = recommendedSource.uri;
        		}
        	}
        }
        
    }
    return result;
}

/**called from source-selection.xsl*/
GVSBrowser.prototype.registerSource = function(uri, editable) {
	this.availableSourceURIs.push(uri);
	if (editable) {
		this.editableSourceURIs.push(uri);
	}
}


GVSBrowser.prototype.filterSources = function() {
	var pattern = new RegExp(document.getElementById("sourceFilter").value);
	var rows = getChildrenByClass(document,"sourceRow");
	for (var i = 0; i < rows.length; i++) {
		if (rows[i].getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "td")[1].childNodes[0].nodeValue.match(pattern)) {
			rows[i].style.display ="";
		} else {
			rows[i].style.display ="none";
		}
	}
}

GVSBrowser.prototype.selectSources = function(sourceURIs) {
	var origAutoRefreshState = this.autoRefreshCheckbox.checked;
	this.autoRefreshCheckbox.checked = false;
	var rows = getChildrenByClass(document,"sourceRow");
	for (var i = 0; i < rows.length; i++) {
		if (sourceURIs.contains(rows[i].getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "td")[1].childNodes[0].nodeValue)) {
			var checkbox = getChildrenByClass(rows[i],"sourceCB")[0];
			checkbox.checked = true;
			checkbox.onclick();
		}
	}
	this.autoRefreshCheckbox.checked = origAutoRefreshState;
	this.sourceDefinitionChanged();
}

GVSBrowser.prototype.selectAllSources = function() {
	this.suspendRefresh = true;
	var origAutoRefreshState = this.autoRefreshCheckbox.checked;
	this.autoRefreshCheckbox.checked = false;
	var checkboxes = getChildrenByClass(document,"sourceCB");
	for (var i = 0; i < checkboxes.length; i++) {
		checkboxes[i].checked = true;
		checkboxes[i].onclick();
	}
	this.autoRefreshCheckbox.checked = origAutoRefreshState;
	this.suspendRefresh = false;
	this.sourceDefinitionChanged();
}

GVSBrowser.prototype.selectEditableSources = function() {
	this.suspendRefresh = true;
	var origAutoRefreshState = this.autoRefreshCheckbox.checked;
	this.autoRefreshCheckbox.checked = false;
	var checkboxes = getChildrenByClass(document,"editableCB");
	var countSelections = 0;
	for (var i = 0; i < checkboxes.length; i++) {
		if (!checkboxes[i].checked) {
			countSelections++;
		}
		checkboxes[i].checked = true;
		checkboxes[i].onclick();
	}
	this.autoRefreshCheckbox.checked = origAutoRefreshState;
	this.suspendRefresh = false;
	if (countSelections > 0)  {
		this.sourceDefinitionChanged();
	}
	return countSelections;
}

GVSBrowser.prototype.deselectAllSources = function() {
	this.suspendRefresh = true;
	var origAutoRefreshState = this.autoRefreshCheckbox.checked;
	this.autoRefreshCheckbox.checked = false;
	var checkboxes = getChildrenByClass(document,"sourceCB");
	for (var i = 0; i < checkboxes.length; i++) {
		checkboxes[i].checked = false;
		checkboxes[i].onclick();
	}
	this.autoRefreshCheckbox.checked = origAutoRefreshState;
	this.suspendRefresh = false;
	this.sourceDefinitionChanged();
}

GVSBrowser.prototype.addResourceRow = function() {
	var resourceRows = document.getElementById('resourceRows');
	var existingRow = getChildrenByClass(resourceRows, "resourceRow")[0];
	var newResourceRow = existingRow.cloneNode(true);
	var resourceField = getChildrenByClass(newResourceRow, "resourceField")[0];
	if (resourceField.disabled) {
		return;
	}
	/*
	Should be part of a more generic back-mechanism
	if (this.lastRemovedResource) {
		resourceField.value = this.lastRemovedResource;
		this.lastRemovedResource = null;
	} else {*/
		resourceField.value = "";
	//}
	resourceRows.appendChild(newResourceRow);
	this.resourceRowCount++;
	this.removeResourceRowLink.style.display ="";
	return newResourceRow;
}

GVSBrowser.prototype.removeResourceRow = function() {
	var resourceRowsDiv = document.getElementById('resourceRows');
	var resourceRows = getChildrenByClass(resourceRowsDiv, "resourceRow");
	var lastRow = resourceRows[resourceRows.length -1];
	var resourceField = getChildrenByClass(lastRow, "resourceField")[0];
	if (resourceField.disabled) {
		return;
	}
	resourceRowsDiv.removeChild(lastRow);
	if (resourceField.value != "") {
		this.lastRemovedResource = resourceField.value;
		this.sourceDefinitionChanged();
	}
	this.resourceRowCount--;
	if (this.resourceRowCount == 1) {
		this.removeResourceRowLink.style.display ="none";
	}
	
}

GVSBrowser.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

GVSBrowser.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

GVSBrowser.prototype.setResourceFieldsDisabled = function(value) {
	var resourceFields = getChildrenByClass(document.getElementById("resourceRows"), "resourceField");
	for (var i = 0; i < resourceFields.length; i++) {
		resourceFields[i].disabled = value;
	}
}

GVSBrowser.prototype.logout = function() {
	GVSBrowser.createCookie("login","",-1);
	//history.go();
	location.reload(false);
} 

GVSBrowser.prototype.addSourceURL = function(sourceURL) {
	this.sourceURIs.push(sourceURL);
	this.sourceDefinitionChanged();
}

GVSBrowser.prototype.removeSourceURL = function(sourceURL) {
	var currentURLs = this.sourceURIs;
	this.sourceURIs = new Array();
	for (var i = 0; i < currentURLs.length; i++) {
		if (currentURLs[i] != sourceURL) {
			this.sourceURIs.push(currentURLs[i]);
		}
	}
	this.sourceDefinitionChanged();
} 

GVSBrowser.prototype.previous = function() {
	this.followingDates.unshift(this.currentRelevantDate);
	this.currentRelevantDate = this.previousDates.shift()
	this.moment = this.currentRelevantDate;
	document.getElementById("dateField").value = this.moment.toISO8601String();
	this.sourceDefinitionChanged();
	this.refreshDateButtons();
}

GVSBrowser.prototype.following = function() {
	this.previousDates.unshift(this.currentRelevantDate);
	this.currentRelevantDate = this.followingDates.shift()
	this.moment = this.currentRelevantDate;
	document.getElementById("dateField").value = this.moment.toISO8601String();
	this.sourceDefinitionChanged();
	this.refreshDateButtons();
}

GVSBrowser.prototype.refreshDateButtons = function() {
	if (this.followingDates.length > 0) {
		document.getElementById("followingButton").disabled = false;
	} else {
		document.getElementById("followingButton").disabled = true;
	}
	if (this.previousDates.length > 0) {
		document.getElementById("previousButton").disabled = false;
	} else {
		document.getElementById("previousButton").disabled = true;
	}
}
GVSBrowser.prototype.selectResource = function(uri) {
	this.setResourceFieldsDisabled(false);
	while (this.resourceRowCount > 1) {
		this.removeResourceRow();
	}
	getChildrenByClass(document.getElementById("resourceRows"), "resourceField")[0].value = uri;
	document.getElementById("resourceSelected").checked = true;
	this.sourceDefinitionChanged();
}

GVSBrowser.prototype.addSelectedResource = function(uri) {
	var fields = getChildrenByClass(document.getElementById("resourceRows"), "resourceField");
	for (var i = 0; i < fields.length; i++) {
		if (fields[i].value == uri) {
			return;
		}
	}
	this.setResourceFieldsDisabled(false);
	var newResourceRow = this.addResourceRow();
	getChildrenByClass(newResourceRow, "resourceField")[0].value = uri;
/*	alert("adding "+ (fields.length-1));
	fields[fields.length-1].value = uri;*/
	document.getElementById("resourceSelected").checked = true;
	this.sourceDefinitionChanged();
}

/* invoked when list of source or the date changed
*/
GVSBrowser.prototype.sourceDefinitionChanged = function() {
	//alert("source def changed: "+GVSBrowser.prototype.sourceDefinitionChanged.caller+","+stacktrace());
	if (this.autoRefreshCheckbox.checked) {
		this.refresh();
	}
}

GVSBrowser.prototype.refresh = function() {
	
	if (this.suspendRefresh) {
		return;
	}	
	var resourceSelectedRadio = document.getElementById("resourceSelected");
	if (resourceSelectedRadio.checked) {
		var resourceFields = getChildrenByClass(document.getElementById("resourceRows"), "resourceField");
		this.resourceURIs = new Array();
		var j = 0;
		for (var i = 0; i < resourceFields.length; i++) {
			if (resourceFields[i].value != "") {
				this.resourceURIs[j++] = resourceFields[i].value;
			}
		}
		this.data = GVS.getGVSurl(this.sourceURIs, this.moment, this.resourceURIs);
	} else {
    	this.data = GVS.getGVSurl(this.sourceURIs, this.moment);
    }
    this.currentVersion = GVS.loadVersion(this.data);
    var stylesheetPath = "/application/stylesheets/"+this.template;
	GVS.presentDOMWithStyles(document.getElementById("dataArea"), 
		this.currentVersion.xmlContent,
		stylesheetPath);
	var link;
	if (this.template != "show-xml-source") {
		link = this.data+"&stylesheet="+stylesheetPath;
	} else {
		link = this.data;
	}
	document.getElementById("dataLink").href = link;
	this.currentRelevantDate = this.currentVersion.previousDates.shift();
	this.previousDates = this.currentVersion.previousDates;
	this.followingDates = this.currentVersion.followingDates;
	this.refreshDateButtons();
	this.initOverview();
	this.initActions();
}

GVSBrowser.prototype.setTemplate = function(template) {
	this.template = template;
	var stylesheetPath = "/application/stylesheets/"+this.template;
    GVS.presentDOMWithStyles(document.getElementById("dataArea"), 
		this.currentVersion.xmlContent,
		stylesheetPath);
	document.getElementById("dataLink").href = this.data+"&stylesheet="+stylesheetPath;
	this.initOverview();
}

GVSBrowser.prototype.setDateString = function(dateString) {
	this.moment.setISO8601(dateString);
	//next line error? maybe: https://bugzilla.mozilla.org/show_bug.cgi?id=236791
	//alert(this.moment);
	this.sourceDefinitionChanged();
}
function  getChildrenByClass(field, name) {
	var result = new Array();
	if (field.childNodes) {
		var children = field.childNodes;
		for(var i = 0; i < children.length; i++) {
			var current = children[i];
			/*if (current.className == name) {				
				result.push(current);
			} */
			if (current.className) {
			//alert(current.className);
			var classNames = current.className.split(' ');
			//alert(current.classNames);
			for (var j = 0; j < classNames.length; j++) {
				//alert(classNames[j]+" "+(classNames[j] == name));
				if (classNames[j] == name) {
					result.push(current);
				}
			}
		}
			result = result.concat(getChildrenByClass(current, name));

		}
	}
	return result;
}

// This function returns the name of a given function. It does this by
// converting the function to a string, then using a regular expression
// to extract the function name from the resulting code.
function funcname(f) {
    var s = f.toString().match(/function (\w*)/)[1];
    if ((s == null) || (s.length == 0)) return "anonymous";
    return s;
}

// This function returns a string that contains a "stack trace."
function stacktrace() {
    var s = "";  // This is the string we'll return.
    // Loop through the stack of functions, using the caller property of
    // one arguments object to refer to the next arguments object on the
    // stack.
    for(var a = arguments.caller; a != null; a = a.caller) {
        // Add the name of the current function to the return value.
        s += funcname(a.callee) + "\n";

        // Because of a bug in Navigator 4.0, we need this line to break.
        // a.caller will equal a rather than null when we reach the end 
        // of the stack. The following line works around this.
        if (a.caller == a) break;
    }
    return s;
}


Array.prototype.contains = function(element, comparator) {
	for (var i = 0; i < this.length; i++) {
		if ((this[i] == element) || (comparator && (comparator(this[i], element)))) {
			return true;
		}
	}
	return false;
}