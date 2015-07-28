# Add some used library:
library('rgdal')		# for SpatialDataFrame
library('automap')		# for interpolation (krige)
library('gstat')		# for interpolation (idw)

#' Interpolate the date in the SpatialPointsDataFrame given for parameter
#' @param proj.df SpatialPointsDataFrame, the data to interpolate
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real)
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @param p real, power p for the interpolation
#' @return SpatialPointsDataFrame, the data interpolated
#' @author
#' Valentin SASYAN, v. 1.4.0, 07/28/2015
interpolation <- function(proj.df, resolution=100, EPSG, p=0.5) {
	# getPointList:
	pointList = getPointList(extent(proj.df), resolution)

	# Create the interpolation mesh:
	proj.new = SpatialPoints(pointList)
	proj4string(proj.new) <- CRS(EPSG[['proj4']])

	# Now, try to interpolate:
	if (interpolation == 'krige') { # scaled data
		proj.intX <- autoKrige(x~1, proj.df, proj.new)
		proj.intY <- autoKrige(y_~1, proj.df, proj.new)
		proj.intZ <- autoKrige(z_~1, proj.df, proj.new)
		kriList = list(x=proj.intX$krige_output['var1.pred'][[1]], y=proj.intY$krige_output['var1.pred'][[1]], z=proj.intZ$krige_output['var1.pred'][[1]])
	} else if (interpolation == 'idw') { # raw data
		proj.intX <- idw(x~1, proj.df, proj.new, idp=p)
		proj.intY <- idw(y~1, proj.df, proj.new, idp=p)
		proj.intZ <- idw(z~1, proj.df, proj.new, idp=p)
		kriList = list(x=proj.intX['var1.pred'][[1]], y=proj.intY['var1.pred'][[1]], z=proj.intZ['var1.pred'][[1]])
	}

	# Then create a SpatialDataFrame with the interpolated data:
	dfInterXYZ <- data.frame(t(matrix(unlist(kriList), nrow=3, byrow=T)))
	colnames(dfInterXYZ) <- c('x', 'y', 'z')
	proj.dfKri <- SpatialPointsDataFrame(coords=pointList, data=dfInterXYZ[,c(1,2,3)])
	gridded(proj.dfKri)<-TRUE

	# Return:
	proj.dfKri

}

#' Generate a list of SpatialPoint
#' @param extent extent, area to cover
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real)
#' @return list, a list of SpatialPoint
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/12/2015
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