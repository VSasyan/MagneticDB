# Add some used library:
library('rgdal')		# for SpatialDataFrame
library('automap')		# for interpolation

#' Interpolate the date in the SpatialPointsDataFrame passed in parameter
#' @param folder string, folder where are the files to use
#' @param proj.df SpatialPointsDataFrame, the data to interpolate
#' @param file string, name of the JSON file in the data folder (without the .json extension)
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real)
#' @return proj.dfKri SpatialPointsDataFrame, the data interpolated
#' @author Valentin SASYAN
#' @version 1.1.1
#' @date  06/15/2015
interpolation <- function(proj.df, file='generated', resolution=100) {
	# getPointList:
	pointList = getPointList(extent(proj.df), resolution)

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

#' Check if the script can write the specified file (erasing or not an eventual existing file)
#' @param erase boolean, the script can erase an eventual existing file 
#' @param file string, path of the file to test
#' @return boolean, true if the file can be written
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @example
#' isWritable(false, 'myNewFile.txt')
isWritable <- function(erase, file) {
	r = TRUE
	if (file.exists(file) && erase) {r = file.remove(file)}
	else {r = !file.exists(file)}
	r
}

#' Generate a list of SpatialPoint
#' @param extent extent, area to cover
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real)
#' @return pointList, a list of a list of SpatialPoint
#' @author Valentin SASYAN
#' @version 1.1.0
#' @date  06/12/2015
getPointList <- function(extent, resolution) {
	deltaX <- abs(xmax(extent) - xmin(extent))
	deltaY <- abs(ymax(extent) - ymin(extent))
	sizeX <- deltaX / resolution
	sizeY <- deltaY / resolution
	byX <- deltaX / sizeX;
	byY <- deltaY / sizeY;

	# Generate Seq :
	seqX <- seq(from=xmin(extent), to=xmax(extent), by=byX)
	seqY <- seq(from=ymin(extent), to=ymax(extent), by=byY)
	pointList <- list(x=rep(seqX[1],length(seqY)), y=seqY)
	for (i in 2:length(seqX)) {
		pointList <- mapply(c, pointList, list(x=rep(seqX[i], length(seqY)), y=seqY), SIMPLIFY=FALSE)
	}
	pointList
}