/* $Id$ */

var browser = new Browser()

var CSS_VISIBILITY_HIDDEN, CSS_VISIBILITY_VISIBLE

if (browser.isNN4) {
	CSS_VISIBILITY_VISIBLE = 'show'
	CSS_VISIBILITY_HIDDEN = 'hide'
} else {
	CSS_VISIBILITY_VISIBLE = 'visible'
	CSS_VISIBILITY_HIDDEN = 'hidden'
}

function objGet(name) {
	if (browser.hasDOM)
		return document.getElementById(name)
	else if (document.all)
		return document.all(name)
	else
		return null
}

function objGetTop(obj) {
	/*
	 * If we can't get it, we can't set it,
	 * so it shouldn't entirely matter.
	 */
	return obj.style ? parseInt(obj.style.top) : 0
}

function objSetTop(obj, pos) {
	if (obj.style) {
		obj.style.top = pos + 'px'
	}
}

function objSetVisibility(obj, vis) {
	if (obj.style) {
		obj.style.visibility = vis
	}
}
