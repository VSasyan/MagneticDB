#' Read the date in the json file
#' @param filter RegExpr, filter for the name of the folders to use
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return SpatialPointsDataFrame, the data of the JSON in a SPDF ready for the interpolation
#' @author Valentin SASYAN
#' @version 1.1.0
#' @date  06/16/2015
readExport <- function(filter, EPSG) {
	# Get the list of the files:
	liste <- list.files('data', filter, full.names=TRUE)

	# Keep only those with a measurements.json file inside:
	listFiltered <- Filter(function(x) {file.exists(paste(x,'measurements.json',sep='/'))}, liste)

	# Convert to a list of dataFrame:
	listDfPointXYZ <- lapply(listFiltered, function(folder) {

		# Read the data for each:
		jsonExport <- fromJSON(file=paste(folder,'measurements.json',sep='/'))

		# Convert the listJsonExport in list of listPointXYZ [{lat:numeric,lon:numeric,x:numeric,y:numeric,z:numeric}]:
		listPointXYZ <<- lapply(jsonExport, function(x) {convertJsonExport(x,folder)})

		# Convert to a dataFrame:
		dfPointXYZ <<- data.frame(matrix(unlist(listPointXYZ), nrow=length(listPointXYZ), byrow=T))

		# Give a good name to the df's col:
		colnames(dfPointXYZ) <<- c('lat', 'lon', 'x', 'y', 'z', 'id', 'type')

		# The data is scaled and centered:
		#dfPointXYZ[,'x'] = scale(dfPointXYZ[,'x'])[,1] NOT with x witch is ~0 and witch is kept just to show error
		dfPointXYZ[,'y_'] = scale(dfPointXYZ$y)[,1]
		dfPointXYZ[,'z_'] = scale(dfPointXYZ$z)[,1]
		dfPointXYZ[,'folder'] = folder

		# Final colonne name:
		colnames(dfPointXYZ) <- c('lat', 'lon', 'x', 'y', 'z', 'id', 'type', 'y_', 'z_', 'folder')

		# Return
		dfPointXYZ
	})

	# Convert to a list of SpatialDataFrame:
	my.listDf <- lapply(listDfPointXYZ, function(dfPointXYZ) {SpatialPointsDataFrame(coords=dfPointXYZ[1:2], data=dfPointXYZ[,c(3,4,5,6,7,8,9,10)])})

	# Combine all the SpatialDataFrame in one:
	for (i in 1:length(my.listDf)) {
		if (i == 1) {
			my.df <- my.listDf[[i]]
		} else {
			my.df <- rbind(my.df, my.listDf[[i]])
		}
	}

	# Change projection of the df:
	proj4string(my.df) <- CRS("+proj=longlat +datum=WGS84") # EPSG-4326
	proj.df <- spTransform(my.df, CRS(EPSG[['proj4']]))

	# Return:
	proj.df
}

#' Convert the raw JSON export in a usable listPointXYZ
#' @param x list, the raw JSON export
#' @return list, the usable listPointXYZ
#' @author Valentin SASYAN
#' @version 1.2.0
#' @date  06/18/2015
convertJsonExport <- function(x, folder){
	list(
		lon = x[['gps']][['lon']],
		lat = x[['gps']][['lat']],
		x = x[['absMagneticField']][['x']],
		y = x[['absMagneticField']][['y']],
		z = x[['absMagneticField']][['z']],
		id = x[['id']],
		type = getIdType(x[['type']])
	)
}

#' Convert the String type of a building in a integer type
#' @param typeString String, the String type of the building
#' @return idType integer, the integer type of the building
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/18/2015
getIdType <- function(typeString) {
	if (typeString %in% listTypeBuilding) {
		idType <- grep(paste('^',typeString,'$',sep=''), listTypeBuilding)
	} else {
		idType <- 0
	}
	idType
}

#' List of the possible building type
#' @author LAGGISS, uOttawa
#' @version 1.0.0
#' @date  06/18/2015
listTypeBuilding <<- list(
	'Wood',
	'Wood, Light Frame (≤ 5,000 sq. ft.)',
	'Wood, Commercial and Industrial (>5,000 sq. ft.)',
	'Steel',
	'Steel Moment Frame',
	'Steel Braced Frame',
	'Steel Light Frame',
	'Steel Frame with Cast-in-Place Concrete Shear Walls',
	'Steel Frame with Unreinforced Masonry Infill Walls',
	'Concrete',
	'Concrete Moment Frame',
	'Concrete Shear Walls',
	'Concrete Frame with Unreinforced Masonry Infill Walls',
	'Precast Concrete Tilt-Up Walls',
	'Precast Concrete Frames with Concrete Shear Walls',
	'Masonry',
	'Reinforced Masonry Bearing Walls with Wood or Metal Deck Diaphragms',
	'Reinforced Masonry Bearing Walls with Precast Concrete Diaphragms',
	'Unreinforced Masonry Bearing Walls',
	'Mobile Homes'
)