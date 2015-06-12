# setwd("C:/MagneticDB/R/")
# source("processData.r")

# Add some used library:
library('rjson')		# for JSON
library('rgdal')		# for SpatialDataFrame
library('automap')		# for interpolation
library('caret')		# for scaling
library('tictoc')		# for tictoc

# Add some used functions:
source(file='readExport.r', encoding='UTF-8')
source(file='interpolation.r', encoding='UTF-8')
source(file='generate_qgs.r', encoding='UTF-8')

#' Process (interpolation + classification) the date generated by the Android App and exported in the JSON file
#' @param file string, name of the JSON file in the data folder (without the .json extension)
#' @param sizeIGrid integer, size of the grid of interpolation
#' @param export bool, true if the function have to export the used data
#' @param erase bool, true if the function can erase existing used data
#' @return nothing
#' @author Valentin SASYAN
#' @version 0.1.0
#' @date  06/12/2015
#' @examples
#' generate_qgs(file'generated',sizeIGrid=200,erase=FALSE)
processData <- function(file='generated', sizeIGrid=70, export=TRUE, erase=TRUE) {
	tic('processData')

		# 1) We read the exported data:
		tic('readExport')
		proj.df <- readExport(paste('data//', file, '.json',sep=''))
		toc()

		# 2) Interpolation:
		tic('Interpolation')
		proj.dfKri <- interpolation(proj.df, file, sizeIGrid)
		toc()

		# 3) Classification:
		tic('Classification')
		# ...
		toc()

		# 4) Exportation:
		tic('Exportation')
		if (export) {
			writeOGR(proj.df, dsn = paste('data//',file,sep=''), layer = 'savedData', driver = "ESRI Shapefile", overwrite_layer=erase, check_exists=TRUE)
			if (isWritable(erase, paste('data//',file,'//interpolatedData_',sizeIGrid,'.asc',sep=''))) {
				writeGDAL(proj.dfKri, paste('data//',file,'//interpolatedData_',sizeIGrid,'.asc',sep=''))
			}
			if (isWritable(erase, paste('data//',file,'//QGIS_project.qgs',sep=''))) {
				generate_qgs(paste('data',file,sep='/'), 'QGIS_project.qgs')
			}			
		}
		toc()

	toc()
}