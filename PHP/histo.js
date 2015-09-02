$(document).ready(function() {

	$('#go').click(function() {
		var json = $('#json').val();
		var data = JSON.parse(json);

		var extremum = getExtremum(data);

		var elements = getClasser(data);

		var txt = getTxt(extremum, elements);
		console.log(txt);
	});
});

function getExtremum(data) {
	var base = {
		x : data[0].absMagneticField.x,
		y : data[0].absMagneticField.y,
		z : data[0].absMagneticField.z
	};
	var extremum = {min : base, max : base};
	for (i = 1; i < data.length; i++) {
		extremum.min = get('min', extremum, data[i].absMagneticField);
		extremum.max = get('max', extremum, data[i].absMagneticField);
	}
	return extremum;
}

function get(nom, extremum, abs) {
	var nom = {
		x : Math[nom](extremum[nom].x, abs.x),
		y : Math[nom](extremum[nom].y, abs.y),
		z : Math[nom](extremum[nom].z, abs.z)
	};
	return nom;
}

function getClasser(data) {
	var elements = {};
	for (i = 1; i < data.length; i++) {
		if (elements[data[i].type]) {
			elements[data[i].type].mesures.push(data[i]);
			elements[data[i].type].extremum.min = get('min', elements[data[i].type].extremum, data[i].absMagneticField);
			elements[data[i].type].extremum.max = get('max', elements[data[i].type].extremum, data[i].absMagneticField);
		} else {
			elements[data[i].type] = {};
			elements[data[i].type].mesures = [data[i]];
			elements[data[i].type].extremum = {};
			elements[data[i].type].extremum.min = data[i].absMagneticField;
			elements[data[i].type].extremum.max = data[i].absMagneticField;
		}
	}
	return elements;
}

function getTxt(extremum, elements) {
	var txt = '';
	$.each(elements, function(i,e) {
		txt += i + '\n';
		$.each(e.mesures, function(k,m) {
			txt += m.absMagneticField.x + '\t' + m.absMagneticField.y + '\t' + m.absMagneticField.z + '\n';
		});
	});
	return txt;
}

function generate(n) {
	var t = [];
	for (i = 0; i < n; i++) {t[i] = 0;}
	return t;
}