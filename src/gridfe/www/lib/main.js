/* $Id$ */

var MENU_INIT_INTV = 40
var MENU_INIT_SHIFT = 5

function moveMenu() {
	var s, x
	for (var i in menus) {
		var o = objGet(menus[i])
		if (o != null && o.style != null) {
			var y = objGetTop(o) + MENU_INIT_SHIFT
			objSetTop(o, y)
			if (x < 0)
				window.setTimeout('moveMenu()', MENU_INIT_INTV)
			if (x > -80)
				objSetVisibility(o, CSS_VISIBILITY_VISIBLE)
		}
	}
}

window.onload = function() {
	window.setTimeout('moveMenu()', MENU_INIT_INTV)
}
