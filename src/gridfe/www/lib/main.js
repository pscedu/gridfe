/* $Id$ */

var MENU_INIT_INTV = 40
var MENU_INIT_SHIFT = 5
var MI_NAM = 0
var MI_SUB = 1
var MENU_HIDE_TIMO = 500	/* Milliseconds. */
var MENU_SHOW_TIMO = 300	/* Milliseconds. */
var MENU_DISP_INTV = 50		/* Milliseconds. */

function menuMove() {
	var s, y
	for (var i in menus) {
		var o = objGet(menus[i][MI_NAM])
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
	if (document.cookie != null)
		MENU_INIT_INTV = 0
	window.setTimeout('menuMove()', MENU_INIT_INTV)
}

var menutimo = []

function menuShow(o) {
	var m = menuGet(o.id)

	if (m != null) {
		var nam = m[MI_NAM]
		var code = ''

		/* XXX: race condition. */
		if (menutimo[nam] != null)
			window.clearTimeout(menutimo[nam])

		code += 'menuSetDisplay("' + nam + '", "block");'
		menutimo[nam] = window.setTimeout(code, MENU_SHOW_TIMO)
	}
}

function menuHide(o) {
	var m = menuGet(o.id)

	if (m != null) {
		var nam = m[MI_NAM]
		var code = ''

		/* XXX: race condition. */
		if (menutimo[nam] != null)
			window.clearTimeout(menutimo[nam])

		code += 'menuSetDisplay("' + nam + '", "none");'
		menutimo[nam] = window.setTimeout(code, MENU_HIDE_TIMO)
	}
}

function menuSetDisplay(nam, d) {
	var o, m, next = null
	var code = ''

	m = menuGet(nam)
	if (m != null) {
		if (d == 'none')
			m[MI_SUB].reverse()	/* XXX */
		for (var i in m[MI_SUB]) {
			o = objGet(m[MI_SUB][i])
			if (o != null)
				if (objGetDisplay(o) != d) {
					next = o
					break
				}
		}
		if (d == 'none')
			m[MI_SUB].reverse()	/* XXX */
		if (next == null)
			menutimo[nam] = null
		else {
			objSetDisplay(next, d)
			code += 'menuSetDisplay("' + nam + '", "' + d + '")'
			menutimo[nam] = window.setTimeout(code, MENU_DISP_INTV)
		}
	}
}

function menuGet(nam) {
	for (var i in menus)
		if (menus[i][MI_NAM] == nam)
			return (menus[i])
	return (null)
}
