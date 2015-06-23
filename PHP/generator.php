<?php

	$tab = array();
	$nbPt = 500;
	$minLat = 39*10000;
	$maxLat = 43*10000;
	$minLon = 39*10000;
	$maxLon = 43*10000;
	$minY = 1000;
	$maxY = 5000;
	$minZ = 10000;
	$maxZ = 40000;
	for ($i=0; $i < $nbPt; $i++) { 
		array_push($tab, array(
			'gps' => gps(rand($minLat, $maxLat)/10000, rand($minLon, $maxLon)/10000),
			'absMagneticField' => magneticField(rand(-100, 100)/10000,rand($minY, $maxY),rand($minZ, $maxZ))
		));
	}

	echo json_encode($tab);

	function gps($lat, $lon) {
		$tab = array(
			'lat' => $lat,
			'lon' => $lon,
			'alt' => 0,
			'acc' => 50
		);
		return $tab;
	}

	function magneticField($x, $y, $z) {
		$tab = array(
			'x' => $x,
			'y' => $y,
			'z' => $z
		);
		return $tab;
	}

?>