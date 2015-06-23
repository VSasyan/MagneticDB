$(document).ready(loadFunctions);

function loadFunctions() {
	$('#send').click(function() {
		$('#before').val($('#after').val());
		$('p#return').html('Text copied!');
	});
	$('#modify').click(function() {modify();});
	$('textarea').click(function() {$(this).select();});	
}

function modify() {
	$('p#return').html('Modification...');
	var measurements = JSON.parse($('#before').val());
	var id = parseInt($('#id').val());
	var type = $('#type option:selected').text();
	var nb = 0;
	$.each(measurements, function (i, measurement) {
		if(measurement.id == id) {
			measurement.type = type;
			nb++;
		}
	});
	$('#after').val(JSON.stringify(measurements, null, 4)).select();
	$('p#return').html('Success: ' + nb + ' modification' + (nb > 1 ? 's' : '') + ' done !');
}