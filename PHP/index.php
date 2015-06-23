<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>JSOPN Editor - MagneticDB</title>
		<link rel="stylesheet" href="style.css">
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.js"></script>
		<script src="script.js"></script>
	</head>
	<body>
		<section>
			<textarea id="before" placeholder="Text to modify" title="Text to modify"></textarea>
			<div id="form">
				<button id="send">▲</button>
				<input value="" placeholder="ID" title="ID" id="id" />
				<select id="type" title="Type of Building to use">
					<option value="0">none</option>
					<option value="1">(W1) Wood, Light Frame (<= 5,000 sq. ft.)</option>
					<option value="2">(W2) Wood, Commercial and Industrial (>5,000 sq. ft.)</option>
					<option value="3">(S1) Steel Moment Frame</option>
					<option value="4">(S2) Steel Braced Frame</option>
					<option value="5">(S3) Steel Light Frame</option>
					<option value="6">(S4) Steel Frame with Cast-in-Place Concrete Shear Walls</option>
					<option value="7">(S5) Steel Frame with Unreinforced Masonry Infill Walls</option>
					<option value="8">(C1) Concrete Moment Frame</option>
					<option value="9">(C2) Concrete Shear Walls</option>
					<option value="10">(C3) Concrete Frame with Unreinforced Masonry Infill Walls</option>
					<option value="11">(PC1) Precast Concrete Tilt-Up Walls</option>
					<option value="12">(PC2) Precast Concrete Frames with Concrete Shear Walls</option>
					<option value="13">(RM1) Reinforced Masonry Bearing Walls with Wood or Metal Deck Diaphragms</option>
					<option value="14">(RM2) Reinforced Masonry Bearing Walls with Precast Concrete Diaphragms</option>
					<option value="15">(URM) Unreinforced Masonry Bearing Walls</option>
					<option value="16">(MH) Mobile Homes</option>
				</select>
				<button id="modify">Modify</button>
				<p id="return">Choose an ID, a type and click on "Modify"...</p>
			</div>
			<textarea id="after" placeholder="Text modified" title="Text modified"></textarea>
		</section>
	</body>
</html>