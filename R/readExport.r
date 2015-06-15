#' Read the date in the json file
#' @param fileName string, file name of the JSON file
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return SpatialPointsDataFrame, the data of the JSON in a SPDF ready for the interpolation
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/15/2015
readExport <- function(fileName, EPSG) {
	# Read the data:
	jsonExport <- fromJSON(file=fileName)

	# Convert jsonExport in listPointXYZ [{lat:numeric,lon:numeric,x:numeric,y:numeric,z:numeric}]:
	listPointXYZ <- lapply(jsonExport, convertJsonExport)

	# getExtremPointXYZ:
	extremPointXYZ <- getExtremPointXYZ(listPointXYZ)

	# Convert to data.frame:
	dfPointXYZ <- data.frame(matrix(unlist(listPointXYZ), nrow=length(listPointXYZ), byrow=T))
	colnames(dfPointXYZ) <- c('lat', 'lon', 'x', 'y', 'z')

	# The data is scaled amd centered:
	#dfPointXYZ[,'x'] = scale(dfPointXYZ[,'x'])[,1] NOT with x witch is ~0 and witch is kept just to show error
	dfPointXYZ[,'y'] = scale(dfPointXYZ[,'y'])[,1]
	dfPointXYZ[,'z'] = scale(dfPointXYZ[,'z'])[,1]

	# Convert to a SpatialDataFrame:
	my.df <- SpatialPointsDataFrame(coords=dfPointXYZ[1:2], data=dfPointXYZ[,c(3,4,5)])

	# Change projection of the df:
	proj4string(my.df) <- CRS("+proj=longlat +datum=WGS84") # EPSG-4326
	proj.df <- spTransform(my.df, CRS("+proj=tmerc +lat_0=0 +lon_0=-76.5 +k=0.9999 +x_0=304800 +y_0=0 +ellps=GRS80 +units=m +no_defs"))

	# Return:
	proj.df
}

#' Convert the raw JSON export in a usable listPointXYZ
#' @param x list, the raw JSON export
#' @return list, the usable listPointXYZ
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
convertJsonExport <- function(x){
	list(lon=x[['gps']][['lon']], lat=x[['gps']][['lat']], x=x[['absMagneticField']][['x']], y=x[['absMagneticField']][['y']], z=x[['absMagneticField']][['z']])
}