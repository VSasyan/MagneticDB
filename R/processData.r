# setwd("C:/MagneticDB/")
# source("R/processData.r")
# traceback()

# EPSG for Canada/Ottawa :
EPSG_ <- c(authid='EPSG:2951', srsid='920', proj4='+proj=tmerc +lat_0=0 +lon_0=-76.5 +k=0.9999 +x_0=304800 +y_0=0 +ellps=GRS80 +units=m +no_defs', srid='2951', description='NAD83(CSRS) / MTM zone 9', projectionacronym='tmerc', ellipsoidacronym='GRS80', geographicflag='false')

# Add some used library:
library('rjson')		# for JSON
library('rgdal')		# for SpatialDataFrame
library('automap')		# for interpolation
library('caret')		# for scaling
library('tictoc')		# for tictoc

# Add some used functions:
source(file='R/readExport.r', encoding='UTF-8')
source(file='R/interpolation.r', encoding='UTF-8')
source(file='R/classification.r', encoding='UTF-8')
source(file='R/compileClassif.r', encoding='UTF-8')
source(file='R/generate_qgs.r', encoding='UTF-8')

#' Process (interpolation + classification) the data generated by the Android App and exported in the JSON file
#' @param filter RegExpr, filter for the name of the folders to use
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real), 0 for no interpolation
#' @param export bool, true if the function have to export the used data
#' @param erase bool, true if the function can erase existing used data
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @param p real, power p for the interpolation
#' @param classif boolean, do or not the the classification
#' @param compile boolean, do or not the compilation of the classification
#' @param useX boolean, use axe X for the classification
#' @param lessType integer, 0 : no merge, 1 : merge similar types, 2 : merge "wood / not wood"
#' @param rePredict boolean, the known types can be modified during the classification
#' @param debug boolean, debug mode (more printed information)
#' @return nothing
#' @author
#' Valentin SASYAN, v. 0.9.0, 07/28/2015
#' @examples
#' generate_qgs(filter='uOttawa.*',resolution=1,erase=FALSE)
processData <- function(filter='uOttawa', resolution=0, export=TRUE, erase=TRUE, EPSG=EPSG_, p=0.5, classif=TRUE, compile=FALSE, useX=FALSE, lessType=FALSE, rePredict=FALSE, debug=FALSE) {
	tic('processData')

		# 1) We read the exported data:
		tic('readExport')
		proj.df <- readExport(filter, EPSG)
		toc()

		# 2) Interpolation:
		if (resolution != 0) {
			tic('Interpolation')
			proj.dfKri <- interpolation(proj.df, resolution, EPSG, p)
			toc()
		}

		# 3) Classification:
		if (classif) {
			tic('Classification')
			proj.classif <- classification(proj.df, useX=useX, lessType=lessType, rePredict=rePredict, debug=debug)
			toc()
		}

		# 4) Compile classification:
		if (classif && compile) {
			tic('compileClassif')
			proj.compile <- compileClassif(proj.classif)
			toc()
		}

		# 5) Exportation:
		tic('Exportation')
		folder <- gsub('[^a-zA-Z_0-9-]', '', filter)
		if (export) {
			# Saved data:
			writeOGR(proj.df, dsn = paste('data//',folder,sep=''), layer = 'savedData', driver = "ESRI Shapefile", overwrite_layer=erase, check_exists=TRUE)
			# Interpolated data:
			if (resolution != 0) {
				if (isWritable(erase, paste('data//',folder,'//interpolatedData_',resolution,'.asc',sep=''))) {
					writeGDAL(proj.dfKri, paste('data//',folder,'//interpolatedData_',resolution,'.asc',sep=''))
				}
			}
			# Classified data:
			if (classif) {
				writeOGR(proj.classif, dsn = paste('data//',folder,sep=''), layer = 'classifData', driver = "ESRI Shapefile", overwrite_layer=erase, check_exists=TRUE)
			}
			# Compiled classified data:
			if (classif && compile) {
				writeOGR(proj.compile, dsn = paste('data//',folder,sep=''), layer = 'compiledData', driver = "ESRI Shapefile", overwrite_layer=erase, check_exists=TRUE)
			}
			# QGIS Project:
			if (isWritable(erase, paste('data//',folder,'//QGIS_project.qgs',sep=''))) {
				generate_qgs(paste('data',folder,sep='/'), 'QGIS_project.qgs', EPSG)
			}
		}
		toc()

	toc()
}

#' Calculate the size of the interpolation mesh for the given data and the given interpolation
#' @param file string, name of the JSON file in the data folder (without the .json extension)
#' @param resolution double, resolution of the grid of interpolation (size of 1 px in real)
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return list, the X size and the Y size
#' @author
#' Valentin SASYAN, v. 0.2.0, 06/15/2015
#' @examples
#' sizeData(file'generated',resolution=1)
sizeData <- function(file='generated', resolution=1000, EPSG=EPSG_) {
	# 1) We read the exported data:
	proj.df <- readExport(paste('data//', file, '/measurements.json',sep=''), EPSG)

	# 2) We calculate the size of the interpolation grid:
	extent <- extent(proj.df)
	deltaX <- abs(xmax(extent) - xmin(extent))
	deltaY <- abs(ymax(extent) - ymin(extent))
	sizeX <- deltaX / resolution
	sizeY <- deltaY / resolution

	# 3) return :
	r <- c(X=sizeX,Y=sizeY)
	r
}

#' Check if the script can write the specified file (erasing or not an eventual existing file)
#' @param erase boolean, the script can erase an eventual existing file 
#' @param file string, path of the file to test
#' @return boolean, true if the file can be written
#' @author
#' Valentin SASYAN, v1.0.0, 06/12/2015
isWritable <- function(erase, file) {
	r = TRUE
	if (file.exists(file) && erase) {r = file.remove(file)}
	else {r = !file.exists(file)}
	r
}