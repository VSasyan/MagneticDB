#####################################
# Function readExport: read the date in the json file
# 	Enter:
# 		- fileName: string, file name of the JSON file
# 	Returns:
# 		- proj.df: SpatialPointsDataFrame, the data of the JSON in a SPDF ready for the interpolation
#####################################
readExport <- function(fileName) {
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
	proj4string(my.df) <- CRS("+proj=longlat +datum=WGS84")
	proj.df <- spTransform(my.df, CRS("+proj=merc +zone=32s +datum=WGS84"))

	# Return:
	proj.df
}