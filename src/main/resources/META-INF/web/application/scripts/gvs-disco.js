function GVSDisco() {
}

GVSDisco.source = "http://gvs.hpl.hp.com/welcome";

GVSDisco.createURIderefURL = function(uri) {
	//alert(uri);
	return "/gvs?source="+encodeURIComponent(GVSDisco.source)+"&resource="+uri;
}
GVSDisco.putData = function(rdfSymbol, store, previousStore) {
	var url = rdfSymbol.uri;
	var xhr = Util.XMLHTTPFactory();
	//xhr.open("PUT", GVSDisco.createURIderefURL(url), false);
	//xhr.setRequestHeader("Content-Type", "appication/rdf+xml");
	//xhr.send(new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(store, rdfSymbol.uri)));
	var assertedRDF = new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(store, ""));
	var revokedRDF = new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(previousStore, ""));
	var parameters = "assert="+encodeURIComponent(assertedRDF);
	parameters += "&revoke="+encodeURIComponent(revokedRDF);
	xhr.open('POST', GVSDisco.createURIderefURL(url), false);
	xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("Content-length", parameters.length);
	//xhr.setRequestHeader("Connection", "close");
	xhr.send(parameters);
	if (xhr.status != 200) {
		alert(xhr.status+" " +xhr.statusText);
		throw new Error(xhr.status+" " +xhr.statusText);
	}
	
}

