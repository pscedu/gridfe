/* $Id$ */

var MENU_INIT_INTV = 40
var MENU_INIT_SHIFT = 5
var MI_NAME = 0
var MI_SUBMENU = 1

function moveMenu() {
	var s, y
	for (var i in menus) {
		var o = objGet(menus[i][MI_NAME])
		if (o != null && o.style != null) {
			y = objGetTop(o) + MENU_INIT_SHIFT
			objSetTop(o, y)
			if (y < 0)
				window.setTimeout('moveMenu()', MENU_INIT_INTV)
			if (y > -80)
				objSetVisibility(o, CSS_VISIBILITY_VISIBLE)
		}
	}
}

window.onload = function() {
	window.setTimeout('moveMenu()', MENU_INIT_INTV)
}
