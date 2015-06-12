# setwd("C:/R/MagneticDB/")
# source("main.r")

# Add some used library:
library('rjson')		# for JSON
library('rgdal')		# for SpatialDataFrame
library('automap')		# for interpolation
library('caret')		# for scaling
library('tictoc')		# for tictoc

# Add some used functions:
source(file='generate_qgs.r', encoding='UTF-8')

#####################################
# Function interpolation: interpolate the date in the json file.
# 	Enter:
# 		- proj.df: SpatialPointsDataFrame, the data to interpolate
# 		- file: string, name of the JSON file in the data folder (without the .json extension)
# 		- sizeIGrid: integer, size of the grid of interpolation
# 	Returns:
# 		- proj.dfKri: SpatialPointsDataFrame, the data interpolated
#####################################
interpolation <- function(proj.df, file='generated', sizeIGrid=70) {
	# getPointList:
	pointList = getPointList(extent(proj.df), sizeIGrid);

	# Create the interpolation mesh:
	proj.new = SpatialPoints(pointList)
	# proj4string(my.new) <- CRS("+proj=longlat +datum=WGS84")
	# proj.new <- spTransform(my.new, CRS("+proj=merc +zone=32s +datum=WGS84"))

	# Now, try to interpolate:
	proj.kriX <- autoKrige(x~1, proj.df, proj.new)
	proj.kriY <- autoKrige(y~1, proj.df, proj.new)
	proj.kriZ <- autoKrige(z~1, proj.df, proj.new)

	# Then create a SpatialDataFrame with the interpolated data:
	kriList = list(x=proj.kriX$krige_output['var1.pred'][[1]], y=proj.kriY$krige_output['var1.pred'][[1]], z=proj.kriZ$krige_output['var1.pred'][[1]])
	dfInterXYZ <- data.frame(t(matrix(unlist(kriList), nrow=3, byrow=T)))
	colnames(dfInterXYZ) <- c('x', 'y', 'z')
	proj.dfKri <- SpatialPointsDataFrame(coords=pointList, data=dfInterXYZ[,c(1,2,3)])
	gridded(proj.dfKri)<-TRUE

	# Return:
	proj.dfKri
}

# USED FUNCTIONS :

isWritable <- function(erase, file) {
	r = TRUE
	if (file.exists(file) && erase) {r = file.remove(file)}
	else {r = !file.exists(file)}
	r
}

convertJsonExport <- function(x){
	list(lat=x[['gps']][['lat']], lon=x[['gps']][['lon']], x=x[['absMagneticField']][['x']], y=x[['absMagneticField']][['y']], z=x[['absMagneticField']][['z']])
}

getExtremPointXYZ <- function(listPointXYZ) {
	extrem = list(minLat = NA, maxLat = NA, minLon = NA, maxLon = NA, minX = NA, maxX = NA, minY = NA, maxY = NA, minZ = NA, maxZ = NA)
	for (i in 1:length(listPointXYZ)) {
		extrem[['minLat']] = min(extrem[['minLat']], listPointXYZ[[i]][['lat']], na.rm=TRUE)
		extrem[['maxLat']] = max(extrem[['maxLat']], listPointXYZ[[i]][['lat']], na.rm=TRUE)
		extrem[['minLon']] = min(extrem[['minLon']], listPointXYZ[[i]][['lon']], na.rm=TRUE)
		extrem[['maxLon']] = max(extrem[['maxLon']], listPointXYZ[[i]][['lon']], na.rm=TRUE)
		extrem[['minX']] = min(extrem[['minX']], listPointXYZ[[i]][['x']], na.rm=TRUE)
		extrem[['maxX']] = max(extrem[['maxX']], listPointXYZ[[i]][['x']], na.rm=TRUE)
		extrem[['minY']] = min(extrem[['minY']], listPointXYZ[[i]][['y']], na.rm=TRUE)
		extrem[['maxY']] = max(extrem[['maxY']], listPointXYZ[[i]][['y']], na.rm=TRUE)
		extrem[['minZ']] = min(extrem[['minZ']], listPointXYZ[[i]][['z']], na.rm=TRUE)
		extrem[['maxZ']] = max(extrem[['maxZ']], listPointXYZ[[i]][['z']], na.rm=TRUE)
	}
	extrem
}

getPointList <- function(extent, size) {
	deltaX = abs(xmax(extent) - xmin(extent))
	deltaY = abs(ymax(extent) - ymin(extent))
	byX = deltaX / size;
	byY = deltaY / size;

	# Generate Seq :
	seqX = seq(from=xmin(extent), to=xmax(extent), by=byX)
	seqY = seq(from=ymin(extent), to=ymax(extent), by=byY)
	pointList = list(x=rep(seqX[1],size+1), y=seqY)
	for (i in 2:length(seqX)) {
		pointList = mapply(c, pointList, list(x=rep(seqX[i], size+1), y=seqY), SIMPLIFY=FALSE)
	}
	pointList
}