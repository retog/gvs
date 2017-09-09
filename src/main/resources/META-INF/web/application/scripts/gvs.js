/** a set of static functions to access the GVS store on the originating server
*/

var xhtmlNS = "http://www.w3.org/1999/xhtml";

function GVS() {
}

/**
* @element the element to which the result should added
* @contentURL the URL from which the content R3X can be downloaded
* @styleSheetURL the URL of the XSLT stylesheet
* @resourceURL (optional) the main resource (passed as param to the stylesheet)
*/

GVS.present = function(element, contentURL, styleSheetURL, resourceURL) {
	var contentDOM = this.loadDOM(contentURL);
	GVS.presentDOMWithStyles(element, contentDOM, styleSheetURL, resourceURL);
	
}
/**
* executes the scripts with scrip-tags in element('s children)
*/
GVS.executeScripts = function(element) {
	if (element.nodeType != 1) {
		return;
	}
	var headElem = document.getElementsByTagNameNS(xhtmlNS,"head")[0];
	var scriptElements = element.getElementsByTagNameNS(xhtmlNS, "script");
	//alert("found "+scriptElements.length+" scripts");
	for (var i = 0; i < scriptElements.length; i++) {
		var scriptElem = scriptElements[i];
		var newScriptElem = document.createElementNS("http://www.w3.org/1999/xhtml","script");
		if (scriptElem.type) {
			newScriptElem.setAttribute("type",scriptElem.type);
		}
		if (scriptElem.src) {
			newScriptElem.setAttribute("src", scriptElem.src);
		}
		if (scriptElem.childNodes[0]) {
			//newScriptElem.appendChild(document.createTextNode(scriptElem.childNodes[0].nodeValue));
			var evalString = scriptElem.childNodes[0].nodeValue;
			for (var j = 1; j < scriptElem.childNodes.length; j++) {
				evalString += scriptElem.childNodes[j].nodeValue;
			}			
			eval(evalString);
		}
		headElem.appendChild(newScriptElem.cloneNode(true));
		//headElem.appendChild(scriptElem.cloneNode(true));
	}
}

GVS.removeScriptTags = function(element) {
	if (element.nodeType != 1) {
		return;
	}
	var scriptElements = element.getElementsByTagNameNS(xhtmlNS, "script");
	//alert("found "+scriptElements.length+" scripts");
	for (var i = scriptElements.length-1; i >= 0; i--) {
		var scriptElem = scriptElements[i];
		scriptElem.parentNode.removeChild(scriptElem);
	}
}

GVS.presentDOMWithStyles = function(element, contentDOM, styleSheetURL, resourceURL) {	
	if (styleSheetURL.match(/show-xml-source/)) {
		//fasttrack
		while (element.childNodes.length > 0) {
			element.removeChild(element.firstChild);
		}
		element.appendChild(LinkingSerializer.serializeToXHTML(contentDOM));
	} else {
		var styleSheetDOM = this.loadDOM(styleSheetURL);
		GVS.presentDOMs(element, contentDOM, styleSheetDOM, resourceURL);
	}
}

GVS.presentDOMs = function(element, contentDOM, styleSheetDOM, resourceURL) {
	//alert("presenting dom");
	var xsltProcessor = new XSLTProcessor()
	var resultDoc = document.implementation.createDocument(xhtmlNS,"span", null);//"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF", null);
	try {
		xsltProcessor.importStylesheet(styleSheetDOM);
		var transformed = xsltProcessor.transformToFragment(contentDOM, resultDoc);
		resultDoc.childNodes[0].appendChild(transformed.cloneNode(true));
		//problems in opera and minefield: var transformed = xsltProcessor.transformToDocument(contentDOM);
	} catch (e) {
		alert("Error applying stylesheet: "+e);
		//alert(new XMLSerializer().serializeToString(styleSheetDOM));
		//alert(new XMLSerializer().serializeToString(contentDOM));
		return;
	}
	while (element.childNodes.length > 0) {
		element.removeChild(element.firstChild);
	}
	//if (transformed.localName == null) {
		var body = resultDoc.getElementById("body");
		if (body) {	
			transformed = body;
		} else {
			var bodyList = resultDoc.getElementsByTagNameNS(xhtmlNS, "body");
			if (bodyList.length > 0) {	
				transformed = bodyList[0];
			}
		}
	//} 
	var childNodes = transformed.childNodes;
	for (var i = 0; i < childNodes.length; i++) {
		//alert(new XMLSerializer().serializeToString(childNodes[i]))
		//TODO this seems to be needed, beacuse firefox occasionally doen't execute on appending, investigate
		var addition = childNodes[i].cloneNode(true);
		this.removeScriptTags(addition);
		element.appendChild(addition);
		this.executeScripts(childNodes[i]);
	}
}

GVS.getGVSurl = function(sources, moment, resources) {
	var gvsURL = "/gvs?";
	var first = true;
	for (var i = 0; i < sources.length; i++) {
		if (!first) {
			gvsURL += '&';
		} else {
			first = false;
		}
		gvsURL += "source="
		gvsURL += encodeURIComponent(sources[i]);
	}
	if (moment) {
		gvsURL += '&moment='+this.encodeDate(moment);;
	}
	if (resources) {
		for (var i = 0; i < resources.length; i++) {
			gvsURL += "&resource=";
			gvsURL += encodeURIComponent(resources[i]);
		}	
	}	
	return gvsURL;
}


GVS.encodeDate = function(date) {
	var result = "";
	result += date.getUTCFullYear();
	
	var month = date.getUTCMonth()+1;
	if (month < 10) {
		result += "0";
	}
	result += month;
	var day = date.getUTCDate();
	if (day < 10) {
		result += "0";
	}
	result += day;
	var hours = date.getUTCHours();
	if (hours < 10) {
		result += "0";
	}
	result += hours;
	var minutes = date.getUTCMinutes();
	if (minutes < 10) {
		result += "0";
	}
	result += minutes;
	var seconds = date.getUTCSeconds();
	if (seconds < 10) {
		result += "0";
	}
	result += seconds;
	var millis = date.getUTCMilliseconds();
	if (millis < 100) {
		result += "0";
		if (millis < 10) {
			result += "0";
		}
	}
	result += millis;
	return result;
}

GVS.loadVersion = function(location) {
	var xmlhttp= this.createXMLHttpRequest();
  	xmlhttp.open("GET", location, false);
  	xmlhttp.setRequestHeader("User-Agent", "Jena - GVS");
  	xmlhttp.send(null);
  	if (xmlhttp.status != 200) {
  		alert("server returned failure: "+xmlhttp.responseText);
  	}
  	var result = new Object();
    result.xmlContent =  xmlhttp.responseXML;
    var store = new RDFIndexedFormula();
    var parser = new RDFParser(store);
	parser.reify = parser.forceRDF = true;
	//problem (alert) when using location as base
	parser.parse(xmlhttp.responseXML.childNodes[0].cloneNode(true), "http://gvs.hpl.hp.com/");
    result.rdfContent = store;
    result.previousDates = GVS.getDataArray(xmlhttp.getResponseHeader("X-GVS-Previous"));
    result.followingDates = GVS.getDataArray(xmlhttp.getResponseHeader("X-GVS-Following"))
    return result;

}

GVS.getDataArray = function(headerString) {
	var result = new Array();
    if  ((headerString != null) && !(headerString == "")) {
    	var dateStrings = headerString.split(",");
    } else {
    	var dateStrings = new Array();
    }
    for (var i = 0; i < dateStrings.length; i++) {
 		var date  = new Date();
 		date.setISO8601(dateStrings[i]);
    	result.push(date);
    }
    return result;
}

GVS.loadDOM = function(location) {
	var xmlhttp= this.createXMLHttpRequest();
  	xmlhttp.open("GET", location, false);
  	xmlhttp.setRequestHeader("User-Agent", "Jena - GVS");
  	xmlhttp.send(null);
    return xmlhttp.responseXML;
}

GVS.createXMLHttpRequest = function() {
	var oXML=typeof XMLHttpRequest!='undefined'?
		new XMLHttpRequest:
		false;
	if(!oXML) {
		try {
			oXML= new ActiveXObject('MSXML2.XMLHTTP.5.0');
		} catch(e) {
			try {
				oXML=new ActiveXObject('MSXML2.XMLHTTP.4.0');
			}catch(e) {
				try {
					oXML=new ActiveXObject('MSXML2.XMLHTTP.3.0');
				} catch(e) {
					try {
						oXML=new ActiveXObject('Microsoft.XMLHTTP');
					} catch(e) {
						throw new Error('XMLHttpRequest object does not exist');
					}
				}
			}
		}
	}
	return oXML;
}


//adding functions for data-parsing/formatting
//copied from http://delete.me.uk/2005/03/iso8601
Date.prototype.setISO8601 = function (string) {
    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
        "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
        "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
    var d = string.match(new RegExp(regexp));

    var offset = 0;
    var date = new Date(d[1], 0, 1);

    if (d[3]) { date.setMonth(d[3] - 1); }
    if (d[5]) { date.setDate(d[5]); }
    if (d[7]) { date.setHours(d[7]); }
    if (d[8]) { date.setMinutes(d[8]); }
    if (d[10]) { date.setSeconds(d[10]); }
    if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
    if (d[14]) {
        offset = (Number(d[16]) * 60) + Number(d[17]);
        offset *= ((d[15] == '-') ? 1 : -1);
    }

    offset -= date.getTimezoneOffset();
    var time = (Number(date) + (offset * 60 * 1000));
    this.setTime(Number(time));
}

/* This function takes two arguments, both optional. 
* The first describes the format the resulting string should take, 
* ie. how many components to include. This is an integer between 1 and 6, 
* with the meanings listed above in the comment block. The second 
* argument is an optional timezone offset. If it is not specified 
* the timezone is set to UTC using the Z character. It takes the form 
* +HH:MM or -HH:MM.*/
Date.prototype.toISO8601String = function (format, offset) {
    /* accepted values for the format [1-6]:
     1 Year:
       YYYY (eg 1997)
     2 Year and month:
       YYYY-MM (eg 1997-07)
     3 Complete date:
       YYYY-MM-DD (eg 1997-07-16)
     4 Complete date plus hours and minutes:
       YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
     5 Complete date plus hours, minutes and seconds:
       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
     6 Complete date plus hours, minutes, seconds and a decimal
       fraction of a second
       YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
    */
    if (!format) { format = 6; }
    if (!offset) {
        offset = 'Z';
        var date = this;
    } else {
        var d = offset.match(/([-+])([0-9]{2}):([0-9]{2})/);
        var offsetnum = (Number(d[2]) * 60) + Number(d[3]);
        offsetnum *= ((d[1] == '-') ? -1 : 1);
        var date = new Date(Number(Number(this) + (offsetnum * 60000)));
    }

    var zeropad = function (num) { return ((num < 10) ? '0' : '') + num; }

    var str = "";
    str += date.getUTCFullYear();
    if (format > 1) { str += "-" + zeropad(date.getUTCMonth() + 1); }
    if (format > 2) { str += "-" + zeropad(date.getUTCDate()); }
    if (format > 3) {
        str += "T" + zeropad(date.getUTCHours()) +
               ":" + zeropad(date.getUTCMinutes());
    }
    if (format > 5) {
        var secs = Number(date.getUTCSeconds() + "." +
                   ((date.getUTCMilliseconds() < 100) ? '0' : '') +
                   zeropad(date.getUTCMilliseconds()));
        str += ":" + zeropad(secs);
    } else if (format > 4) { str += ":" + zeropad(date.getUTCSeconds()); }

    if (format > 3) { str += offset; }
    return str;
}
