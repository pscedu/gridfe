/* $Id$ */

var MENU_INIT_INTV = 40
var MENU_INIT_SHIFT = 5
var MI_NAME = 0
var MI_SUBMENU = 1
var MENU_HIDE_TIMO = 1000	/* Milliseconds. */

function menuMove() {
	var s, y
	for (var i in menus) {
		var o = objGet(menus[i][MI_NAME])
		if (o != null && o.style != null) {
			y = objGetTop(o) + MENU_INIT_SHIFT
			objSetTop(o, y)
			if (y > -80)
				objSetVisibility(o, CSS_VISIBILITY_VISIBLE)
		}
	}
	if (y < 0)
		window.setTimeout('menuMove()', MENU_INIT_INTV)
}

window.onload = function() {
	window.setTimeout('menuMove()', MENU_INIT_INTV)
}

var menutimo = null

function menuShow(m) {
	menuSetDisplay(m.id, 'normal')
	if (menutimo != null) {
		window.clearTimeout(menutimo)
		menutimo = null
	}
}

function menuHide(m) {
	var code = ''
	
	code += 'menuSetDisplay(m.id, "none");'
	code += 'menutimo = null;'
	menutimo = window.setTimeout(code, MENU_HIDE_TIMO)
}

function menuSetDisplay(m, d) {
	var o
	for (var i in menus[m]) {
		o = objGet(menus[m][i])
		if (o != null)
			objSetDisplay(o, d)
	}
}
