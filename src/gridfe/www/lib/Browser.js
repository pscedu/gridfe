/* $Id$ */

function Browser() {
	this.isNN4 = (navigator.appName == 'Netscape' &&
			parseInt(navigator.appVersion) == 4)
	this.hasDOM = document.getElementById
	return this
}
