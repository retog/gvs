xhtmlNS  = "http://www.w3.org/1999/xhtml";

function PrintLinks() {
	var links = document.getElementsByTagNameNS(xhtmlNS, "a");
	//TODO skip links in non-print/navigation sections
	var endNotesTable = document.createElementNS(xhtmlNS, "table");
	endNotesTable.className = "endNotes print";
	var footer = document.getElementById('footer');
	footer.parentNode.insertBefore(endNotesTable, footer);
	this.counter = 0;
	this.linksMap = new Object();
	for (var i = 0; i < links.length; i++) {
		if (links[i].childNodes.length == 1) {
			if (links[i].childNodes[0].tagName == 'img') {
				continue;
			}
			if ((links[i].childNodes[0].nodeType == 3) && (links[i].childNodes[0].nodeValue == links[i].href)){
				continue;
			}
		} 
	
		var footNodeLabel = this.getLabel(links[i], endNotesTable);
		PrintLinks.insertRef(footNodeLabel, links[i]);
		
	}
}
 

PrintLinks.prototype.getLabel = function(link, endNotesTable) {
	if (this.linksMap[link.href]) {
		return this.linksMap[link.href];
	} else {
		this.counter += 1;
		var label = "link"+(this.counter);
		PrintLinks.addEndNote(label, link, endNotesTable);
		this.linksMap[link.href] = label;
		return label;
	}
}

PrintLinks.insertRef = function(footNodeLabel, after) {
	var supElem = document.createElementNS(xhtmlNS, "sup");
	supElem.className = "print";
	supElem.appendChild(document.createTextNode(footNodeLabel));
	after.parentNode.insertBefore(supElem, after);
	after.parentNode.insertBefore(after, supElem);
} 

PrintLinks.addEndNote = function(footNodeLabel, link, endNotesTable) {
	var endNoteRow = document.createElementNS(xhtmlNS, "tr");
	var refCell = document.createElementNS(xhtmlNS, "td");
	endNoteRow.appendChild(refCell);
	refCell.appendChild(document.createTextNode(footNodeLabel));
	var valueCell = document.createElementNS(xhtmlNS, "td");
	valueCell.appendChild(document.createTextNode(link.href));
	endNoteRow.appendChild(valueCell);
	endNotesTable.appendChild(endNoteRow);
} 